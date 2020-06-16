/**
 * The RiotApiHandler Class uses java's HttpClient to interact with Riot Game's API to get information for League of Legends Players.
 * 
 * @author Hans Von Gruenigen
 * @version 1.0
 */
package com.hansvg.lolprotwitterbot;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import org.json.JSONObject;

class RiotApiHandler {

    private final int SECONDS_TO_WAIT_BETWEEN_CALLS = 30;
    private HttpClient httpClient;
    private String apiKey;
    private String region;
    private Logger logger;

    /**
     * RiotApiHandler Class Constructor.
     * 
     * @param apiKey The api key for the Riot Games API to authenticate this app
     * @param region The region to make api calls to
     * @param logger The Logger object to log the processes
     */
    protected RiotApiHandler(String apiKey, String region, Logger logger) {
        httpClient = HttpClient.newHttpClient();
        this.apiKey = apiKey;
        this.region = region;
        this.logger = logger;
    }

    /**
     * Checks if the RiotApiHandler is working by making a simple call to the Riot
     * Games API and checking the responce.
     * 
     * @return True if the RiotApiHandler is found to be working and false otherwise
     */
    protected boolean isWorking() {
        try {

            URI requestURI = new URI("https://" + region + ".api.riotgames.com/lol/summoner/v4/summoners/by-name/"
                    + summonerNameNoSpaces("Hansie"));

            HttpRequest request = HttpRequest.newBuilder().uri(requestURI).header("X-Riot-Token", this.apiKey).build();

            HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 404 || response.statusCode() == 429) {
                return true;
            } else {
                return false;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets account information for each player passed in's summoner names and
     * updates the player's summoner ids.
     * 
     * @param players ArrayList of Players to load summoner ids
     * @throws URISyntaxException   If there was a problem with the syntax of the
     *                              uri
     * @throws InterruptedException If there was an exception when using the
     *                              Thread.sleep() function
     * @throws IOException          If an input or output exception occurred
     */
    protected void loadSummonerIds(ArrayList<Player> players)
            throws URISyntaxException, InterruptedException, IOException {

        double percentComplete = 0;
        for (int currentPlayerIndex = 0; currentPlayerIndex < players.size(); currentPlayerIndex++) {
            Player currentPlayer = players.get(currentPlayerIndex);
            for (int currentSummonerIndex = 0; currentSummonerIndex < currentPlayer
                    .getSummonerNames().length; currentSummonerIndex++) {
                String currentSummonerName = (currentPlayer.getSummonerNames())[currentSummonerIndex];

                URI uri = new URI("https://" + region + ".api.riotgames.com/lol/summoner/v4/summoners/by-name/"
                        + summonerNameNoSpaces(currentSummonerName));
                HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).header("X-Riot-Token", this.apiKey)
                        .build();

                HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());

                JSONObject responseBodyJSON = new JSONObject(response.body());

                if (response.statusCode() == 200) {
                    String currentSummonerId = responseBodyJSON.getString("id");
                    currentPlayer.getSummonerIds()[currentSummonerIndex] = currentSummonerId;

                } else if (response.statusCode() == 404) {
                    // summoner name does not exist
                    currentPlayer.getSummonerIds()[currentSummonerIndex] = null;
                    // LOG
                    this.logger.log("",
                            "Summoner Name \"" + currentSummonerName + "\" could not be found on " + region);
                } else if (response.statusCode() == 429) {
                    // rate limit reached
                    // LOG
                    this.logger.log("", "Riot Games API rate limit reached trying to load summoner ids waiting "
                            + SECONDS_TO_WAIT_BETWEEN_CALLS + " seconds then trying again");
                    currentSummonerIndex--;
                    Thread.sleep(1000 * SECONDS_TO_WAIT_BETWEEN_CALLS);
                } else {
                    // error with getting information from api
                    currentPlayer.getSummonerIds()[currentSummonerIndex] = null;
                    // LOG
                    this.logger.log("",
                            "Error code " + response.statusCode() + " from riot api when trying to load summonerId for "
                                    + currentSummonerName + " Response: " + response.body());
                }

            }

            // Loading bar
            percentComplete = ((double) currentPlayerIndex / (double) (players.size() - 1));
            System.out.print("Loading Summoner Ids: |");
            for (int c = 0; c < 50; c++) {
                if (c < (int) (percentComplete * 50)) {
                    System.out.print("#");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.print("| " + (int) (percentComplete * 100) + "%\r");

        }
        System.out.println();
    }

    /**
     * Checks if each player's summoner id is in an active SoloQueueGame and if they
     * are a SoloQueueGame object is created and added to the ArrayList that is
     * returned.
     * 
     * @param players ArrayList of Players that you want to check if they are in a
     *                SoloQueueGame
     * @param league  The League the Players are a part of
     * @return An ArrayList of SoloQueueGame objects representing current games the
     *         Players are in
     * @throws URISyntaxException   If there was a problem with the syntax of the
     *                              uri
     * @throws IOException          If an input or output exception occurred
     * @throws InterruptedException If there was an exception when using the
     *                              Thread.sleep() function
     */
    protected ArrayList<SoloQueueGame> loadActiveSoloQueueGames(ArrayList<Player> players, League league)
            throws URISyntaxException, IOException, InterruptedException {

        ArrayList<SoloQueueGame> activeSoloQueueGames = new ArrayList<>();
        ArrayList<String> summonerIds = league.getAllSummonerIds();
        double percentComplete = 0;
        int initialAmountOfIds = summonerIds.size();

        while (summonerIds.size() > 0) {

            URI uri = new URI("https://" + region + ".api.riotgames.com/lol/spectator/v4/active-games/by-summoner/"
                    + summonerIds.get(0));
            HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).header("X-Riot-Token", this.apiKey).build();

            HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());

            JSONObject responseBodyJSON = new JSONObject(response.body());

            if (response.statusCode() == 200) {
                SoloQueueGame activeGame = new SoloQueueGame(responseBodyJSON, league);
                activeSoloQueueGames.add(activeGame);
                updateIDsToScan(summonerIds, activeGame.getAllPlayersIds());
            } else if (response.statusCode() == 404) {
                // summoner id not in active game
                summonerIds.remove(0);
            } else if (response.statusCode() == 429) {
                // rate limit reached
                // LOG
                this.logger.log("", "Riot Games API rate limit reached trying to load active solo queue games waiting "
                        + SECONDS_TO_WAIT_BETWEEN_CALLS + " seconds then trying again");
                Thread.sleep(1000 * SECONDS_TO_WAIT_BETWEEN_CALLS);
            } else {
                // error with getting information from api
                summonerIds.remove(0);
                // LOG
                this.logger.log("", "Error code " + response.statusCode()
                        + " from riot api when trying to load active solo queue games");
            }

            // Loading bar
            percentComplete = ((double) (initialAmountOfIds - summonerIds.size()) / (double) (initialAmountOfIds));
            System.out.print("Loading Active Games: |");
            for (int c = 0; c < 50; c++) {
                if (c < (int) (percentComplete * 50)) {
                    System.out.print("#");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.print("| " + (int) (percentComplete * 100) + "%\r");
        }
        System.out.println();
        return activeSoloQueueGames;
    }

    /**
     * Gets a version of the passed in string where spaces are replaced by %20 so
     * they can be used in a URI.
     * 
     * @param summonerName String with spaces you want to replace
     * @return A version of the passed in string where spaces are replaced with %20
     */
    private String summonerNameNoSpaces(String summonerName) {
        return summonerName.replaceAll(" ", "%20");
    }

    /**
     * Helper method for the loadActiveSoloQueueGames() function that takes in an
     * ArrayList of Strings that represent players in a game then removes those
     * participant's ids from the other ArrayList passed in of Ids so that no more
     * ids then needed are checked by the RiotApiHandler.
     * 
     * @param idList                 List of summoner ids to be scanned
     * @param participantIdsToRemove ArrayList of id strings to remove from idList
     */
    private void updateIDsToScan(ArrayList<String> idList, ArrayList<String> participantIdsToRemove) {
        for (String participantIdToRemove : participantIdsToRemove) {
            idList.remove(participantIdToRemove);
        }
    }

}