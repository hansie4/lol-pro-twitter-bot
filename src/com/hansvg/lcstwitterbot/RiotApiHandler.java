package com.hansvg.lcstwitterbot;

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

    private String apiKey;
    private String region;
    private HttpClient httpClient;
    private TwitterBotLogger twitterBotLogger;

    protected RiotApiHandler(String apiKey, String region, TwitterBotLogger twitterBotLogger) {
        this.apiKey = apiKey;
        this.region = region;
        httpClient = HttpClient.newHttpClient();
        this.twitterBotLogger = twitterBotLogger;
    }

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
                    this.twitterBotLogger.log("", "Summoner Name \"" + currentSummonerName + "\" could not be found");
                } else if (response.statusCode() == 429) {
                    // rate limit reached
                    currentSummonerIndex--;
                    Thread.sleep(10000);
                } else {
                    // error with getting information from api
                    currentPlayer.getSummonerIds()[currentSummonerIndex] = null;
                    // LOG
                    this.twitterBotLogger.log("",
                            "Error code " + response.statusCode() + " from riot api when trying to load summonerIds");
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

    protected ArrayList<SoloQueueGame> loadActiveSoloQueueGames(ArrayList<Player> players, League league)
            throws URISyntaxException, IOException, InterruptedException {

        ArrayList<SoloQueueGame> activeSoloQueueGames = new ArrayList<>();
        ArrayList<String> summonerIds = this.getAllSummonerIds(players);
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
                updateIDsToScan(summonerIds, activeGame.getParticipants());
            } else if (response.statusCode() == 404) {
                // summoner id not in active game
                summonerIds.remove(0);
            } else if (response.statusCode() == 429) {
                // rate limit reached
                Thread.sleep(10000);
            } else {
                // error with getting information from api
                summonerIds.remove(0);
                // LOG
                this.twitterBotLogger.log("", "Error code " + response.statusCode()
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

    private String summonerNameNoSpaces(String summonerName) {
        return summonerName.replaceAll(" ", "%20");
    }

    private void updateIDsToScan(ArrayList<String> idList, ArrayList<String[]> participantsToRemove) {

        String[] idsToRemove = new String[participantsToRemove.size()];
        for (int i = 0; i < participantsToRemove.size(); i++) {
            idsToRemove[i] = participantsToRemove.get(i)[1];
        }

        for (int i = 0; i < idsToRemove.length; i++) {
            if (idList.contains(idsToRemove[i])) {
                idList.remove(idsToRemove[i]);
            }
        }

    }

    private ArrayList<String> getAllSummonerIds(ArrayList<Player> players) {
        ArrayList<String> ids = new ArrayList<>();
        for (Player player : players) {
            for (String id : player.getSummonerIds()) {
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

}