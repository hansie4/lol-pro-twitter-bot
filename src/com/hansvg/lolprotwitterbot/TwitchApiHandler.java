/**
 * The TwitchApiHandler class handles all the transfer of information between the LoLProTwitterBot and the Twitch.tv Api.
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
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

class TwitchApiHandler {

    private final int MAX_DISPLAYNAMES_PER_CALL = 100;

    private HttpClient httpClient;
    private Logger logger;

    private int SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED;
    private String TWITCH_CLIENT_ID;
    private String TWITCH_CLIENT_SECRET;

    private String authToken = null;

    /**
     * TwitchApiHandler class constructor.
     * 
     * @param configs The configs for the twitter bot
     * @param logger  The logger object to log what happens in the program
     * @throws Exception
     */
    protected TwitchApiHandler(Properties configs, Logger logger) throws NumberFormatException, Exception {
        this.httpClient = HttpClient.newHttpClient();
        this.logger = logger;

        this.SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED = Integer
                .parseInt(configs.getProperty("SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED_TWITCH_API"));
        if (this.SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED < 5) {
            this.logger
                    .severe("Invalid Integer for SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED_TWITCH_API in config file.");
            throw new Exception();
        }

        this.TWITCH_CLIENT_ID = configs.getProperty("TWITCH_CLIENT_ID");
        if (this.TWITCH_CLIENT_ID == null) {
            this.logger.severe("NULL Twitch client id in config file.");
            throw new Exception();
        }

        this.TWITCH_CLIENT_SECRET = configs.getProperty("TWITCH_CLIENT_SECRET");
        if (this.TWITCH_CLIENT_SECRET == null) {
            this.logger.severe("NULL Twitch client secret in config file.");
            throw new Exception();
        }
    }

    /**
     * Function to get the authentication token for the app from the Twitch api.
     * 
     * @return true if the authentication token was successfully retrieved and false
     *         otherwise
     */
    protected boolean loadToken() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://id.twitch.tv/oauth2/token?client_id=" + this.TWITCH_CLIENT_ID
                            + "&client_secret=" + this.TWITCH_CLIENT_SECRET + "&grant_type=client_credentials"))
                    .POST(BodyPublishers.ofString("")).build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject responseJSON = new JSONObject(response.body());

                this.authToken = responseJSON.getString("access_token");

                // LOG
                this.logger.info("Twitch Api Authentication token successfully retrieved");

                return true;
            } else if (response.statusCode() == 429) {
                // LOG
                this.logger.warning("Twitch Api Rate Limit reached. Retrying after "
                        + (SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED) + " seconds");
                Thread.sleep(1000 * SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED);
                return this.loadToken();
            } else {
                // LOG
                this.logger.warning(
                        "Error getting authentication token from Twitch Api. Status Code: " + response.statusCode());
                return false;
            }
        } catch (URISyntaxException e) {
            // LOG
            this.logger.severe("URISyntaxException");
            return false;
        } catch (IOException e) {
            // LOG
            this.logger.severe("IOException");
            return false;
        } catch (InterruptedException e) {
            // LOG
            this.logger.severe("InterruptedException");
            return false;
        }
    }

    /**
     * Function to revoke the authentication token.
     * 
     * @return true if the token was successfully revoked from the twitch api
     */
    protected boolean revokeToken() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI("https://id.twitch.tv/oauth2/revoke?client_id="
                    + this.TWITCH_CLIENT_ID + "&token=" + this.authToken)).POST(BodyPublishers.ofString("")).build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                this.authToken = null;

                // LOG
                this.logger.info("Twitch Api Authentication token successfully revoked");
                return true;
            } else if (response.statusCode() == 429) {
                // LOG
                this.logger.warning("Twitch Api Rate Limit reached. Retrying after "
                        + (SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED) + " seconds");
                Thread.sleep(1000 * SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED);
                return this.revokeToken();
            } else {
                // LOG
                this.logger.warning(
                        "Error revoking authentication token from Twitch Api. Status Code: " + response.statusCode());
                return false;
            }
        } catch (URISyntaxException e) {
            this.logger.severe("URISyntaxException");
            return false;
        } catch (IOException e) {
            this.logger.severe("IOException");
            return false;
        } catch (InterruptedException e) {
            this.logger.severe("InterruptedException");
            return false;
        }
    }

    /**
     * Goes through each player from the passed in League and if they have a twitch
     * username, their twitch user id is loaded.
     * 
     * @param league The league that holds the players that you want to load ids for
     * @return true if the ids are loaded successfully and false otherwise
     */
    protected boolean loadTwitchUserIds(League league) {
        this.loadToken();
        try {
            ArrayList<Player> playersToLoadIdsFor = getPlayersWithTwtichAccounts(league.getPlayers());

            ArrayList<ArrayList<Player>> blocksOfPlayers = getBlocksOfPlayers(playersToLoadIdsFor);

            for (int currentBlockOfPlayersIndex = 0; currentBlockOfPlayersIndex < blocksOfPlayers
                    .size(); currentBlockOfPlayersIndex++) {

                HttpRequest request = HttpRequest.newBuilder().GET()
                        .uri(new URI(createGetUserURI(blocksOfPlayers.get(currentBlockOfPlayersIndex))))
                        .header("Client-ID", this.TWITCH_CLIENT_ID).header("Authorization", "Bearer " + this.authToken)
                        .build();
                HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

                if (response.statusCode() == 200 || response.statusCode() == 404) {
                    if (response.statusCode() == 200) {
                        JSONObject responseJSON = new JSONObject(response.body());
                        JSONArray userInfoJSONArray = responseJSON.getJSONArray("data");

                        for (int i = 0; i < userInfoJSONArray.length(); i++) {

                            Player currentPlayer = league
                                    .getPlayerFromTwitchName(userInfoJSONArray.getJSONObject(i).getString("login"));

                            if (currentPlayer != null) {
                                currentPlayer.setTwitchUserId(userInfoJSONArray.getJSONObject(i).getString("id"));
                            }

                        }
                    }
                } else if (response.statusCode() == 429) {
                    // LOG
                    this.logger.warning("Twitch Api Rate Limit reached. Retrying after "
                            + (SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED) + " seconds");
                    currentBlockOfPlayersIndex--;
                    Thread.sleep(1000 * SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED);
                } else {
                    // LOG
                    this.logger.warning(
                            "Error loading twitch user ids from Twitch Api. Status Code: " + response.statusCode());
                }
            }
            this.revokeToken();
            return true;
        } catch (URISyntaxException e) {
            // LOG
            this.logger.severe("URISyntaxException");
            this.revokeToken();
            return false;
        } catch (IOException e) {
            // LOG
            this.logger.severe("IOException");
            this.revokeToken();
            return false;
        } catch (InterruptedException e) {
            // LOG
            this.logger.severe("InterruptedException");
            this.revokeToken();
            return false;
        }
    }

    /**
     * Gets the stream objects for each of the players in the passed in team.
     * 
     * @param team   The team containing the players to scan streams for
     * @param league The league that the players being scanned for belong to
     * @return A HashMap with players objects as the keys and an integer
     *         representing the players view count as the value
     */
    protected HashMap<Player, Integer> getStreamersOnTeam(SoloQueueTeam team, League league) {
        this.loadToken();
        try {

            HashMap<Player, Integer> streamers = new HashMap<>();

            URI requestURI = new URI(createTeamStreamRequestURI(team));

            HttpRequest request = HttpRequest.newBuilder().GET().uri(requestURI)
                    .header("Client-ID", this.TWITCH_CLIENT_ID).header("Authorization", "Bearer " + this.authToken)
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 404) {
                if (response.statusCode() == 200) {
                    JSONObject responseJSON = new JSONObject(response.body());
                    JSONArray streamsInfoJSONArray = responseJSON.getJSONArray("data");

                    for (int i = 0; i < streamsInfoJSONArray.length(); i++) {
                        streamers.put(
                                league.getPlayerFromTwitchId(
                                        streamsInfoJSONArray.getJSONObject(i).getString("user_id")),
                                streamsInfoJSONArray.getJSONObject(i).getInt("viewer_count"));
                    }

                    for (Player player : team.getPlayers().keySet()) {
                        if (!streamers.containsKey(player)) {
                            streamers.put(player, 0);
                        }
                    }
                }
            } else if (response.statusCode() == 429) {
                // LOG
                this.logger.warning("Twitch Api Rate Limit reached. Retrying after "
                        + (SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED) + " seconds");
                Thread.sleep(1000 * SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED);
                return getStreamersOnTeam(team, league);
            } else {
                // LOG
                this.logger.warning(
                        "Error loading stream information from Twitch Api. Status Code: " + response.statusCode());
            }
            this.revokeToken();
            return streamers;
        } catch (URISyntaxException e) {
            this.logger.severe("URISyntaxException");
            this.revokeToken();
            return null;
        } catch (IOException e) {
            this.logger.severe("IOException");
            this.revokeToken();
            return null;
        } catch (InterruptedException e) {
            this.logger.severe("InterruptedException");
            this.revokeToken();
            return null;
        }
    }

    /**
     * Helper function to seperate the list of players to scan for into array lists
     * of size MAX_DISPLAYNAMES_PER_CALL so the calls can be made in blocks instead
     * of a call for each individual player.
     * 
     * @param players The complete list of players you want to partition
     * @return An ArrayList of ArraysList of Players containing at most
     *         MAX_DISPLAYNAMES_PER_CALL players
     */
    private ArrayList<ArrayList<Player>> getBlocksOfPlayers(ArrayList<Player> players) {
        ArrayList<ArrayList<Player>> blocksOfUserNames = new ArrayList<>();
        int numberOfBlocks = (int) Math.ceil((double) players.size() / (double) MAX_DISPLAYNAMES_PER_CALL);
        int remainderOfNames = players.size() % MAX_DISPLAYNAMES_PER_CALL;

        for (int currentBlock = 0; currentBlock < numberOfBlocks; currentBlock++) {
            if (currentBlock == (numberOfBlocks - 1)) {
                blocksOfUserNames.add(new ArrayList<>(players.subList(MAX_DISPLAYNAMES_PER_CALL * currentBlock,
                        (MAX_DISPLAYNAMES_PER_CALL * currentBlock) + remainderOfNames)));
            } else {
                blocksOfUserNames.add(new ArrayList<>(players.subList(MAX_DISPLAYNAMES_PER_CALL * currentBlock,
                        MAX_DISPLAYNAMES_PER_CALL * (currentBlock + 1))));
            }
        }

        return blocksOfUserNames;
    }

    /**
     * Helper function that takes in a list of players and returns a new list with
     * all player without twitch usernames removed.
     * 
     * @param originalList List of Player objects
     * @return List of players with twitch usernames
     */
    private ArrayList<Player> getPlayersWithTwtichAccounts(ArrayList<Player> originalList) {
        ArrayList<Player> newList = new ArrayList<>();

        for (Player player : originalList) {
            if (player.getTwitchName() != null) {
                if (!player.getTwitchName().equals("")) {
                    newList.add(player);
                }
            }
        }
        return newList;
    }

    /**
     * Funtion to make the string that will turn into the URI for the call to load
     * the user objects for each Player in the passed in list.
     * 
     * @param players The players whose twitch names are to be included in the URI
     * @return A string representation of the URI to make the call for the user ids
     */
    private String createGetUserURI(ArrayList<Player> players) {
        String uri = "https://api.twitch.tv/helix/users?";
        for (int i = 0; i < players.size(); i++) {
            uri += "login=" + players.get(i).getTwitchName();
            if (i != (players.size() - 1)) {
                uri += "&";
            }
        }
        return uri;
    }

    /**
     * Function to make the string that will turn into the URI for the call to load
     * the user objects for each Player in the passed in team.
     * 
     * @param team The team containing the players whose twitch names are to be
     *             included in the URI
     * @return A string representation of the URI to make the call for the user
     *         streams
     */
    private String createTeamStreamRequestURI(SoloQueueTeam team) {
        String uri = "https://api.twitch.tv/helix/streams?";
        ArrayList<Player> players = this.getPlayersWithTwtichAccounts(new ArrayList<>(team.getPlayers().keySet()));
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getTwitchUserId() != null && !players.get(i).getTwitchUserId().equals("")) {
                uri += "user_id=" + players.get(i).getTwitchUserId();
                if (i != (players.size() - 1)) {
                    uri += "&";
                }
            }
        }
        return uri;
    }

}