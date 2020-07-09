/**
 * The LCSTwitterBot class is the main class for the LCSTwitterBot API.
 * It creates the League, Logger, and Handlers as well as calls all the
 * functions/methods to have the twitter bot run.
 * 
 * @author Hans Von Gruenigen
 * @version 1.0
 */

package com.hansvg.lolprotwitterbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.json.JSONException;
import org.json.JSONObject;

public class LoLProTwitterBot {

    private final int MINIMUM_GAMESCORE_TO_TWEET = 5000;
    private final int INTERVAL_TO_SCAN_ACTIVE_GAMES = 480;

    private File playerRosterFile;
    private File apiInfoFile;

    private League league;

    private JSONObject apiInfoJSON;

    private RiotApiHandler riotApiHandler;
    private TwitchApiHandler twitchApiHandler;
    private TwitterApiHandler twitterApiHandler;

    private HashMap<SoloQueueGame, JSONObject> tweetedGames;

    private Logger logger;

    /**
     * LCSTwitterBot Class Constructor.
     * 
     * @param playerRosterFile .csv file containg the players to track and their
     *                         information
     * @param apiInfoFile      .json file containing information needed to access
     *                         the Riot Games, Twitter, and Twitch API's
     * @param logFile          .log file the user wants to have the logs of the
     *                         twitter bot written too
     * @throws JSONException If an exception occured reading .json
     * @throws IOException   If an input or output exception occurred
     */
    public LoLProTwitterBot(File playerRosterFile, File apiInfoFile, File logFile) throws JSONException, IOException {
        this.playerRosterFile = playerRosterFile;
        this.apiInfoFile = apiInfoFile;

        this.logger = Logger.getLogger("Logger");
        FileHandler loggerFileHandler = new FileHandler(logFile.getAbsolutePath(), true);
        this.logger.addHandler(loggerFileHandler);
        loggerFileHandler.setFormatter(new SimpleFormatter());

        this.logger.info("LoLProTwitterBot Created");

        this.league = new League(this.logger);

        this.readInApiFile();

        this.logger.info("Api Info File read in successfully");

        this.riotApiHandler = new RiotApiHandler(this.getRiotApiKey(), this.getRiotRegion(), this.logger);
        this.logger.info("RiotApiHandler Created");

        this.twitchApiHandler = new TwitchApiHandler(this.getTwitchClientId(), this.getTwitchClientSecret(),
                this.logger);
        this.logger.info("TwitchApiHandler Created");

        this.twitterApiHandler = new TwitterApiHandler(this.getTwitterConsumerKey(), this.getTwitterConsumerSecret(),
                this.getTwitterToken(), this.getTwitterTokenSecret(), this.logger);
        this.logger.info("TwitterApiHandler Created");

        this.tweetedGames = new HashMap<>();
    }

    /**
     * The method used for having the twitter bot start running.
     * 
     * @param secondsToRun The amount of time in seconds you want the Twitter bot to
     *                     run
     * @throws IOException If an input or output exception occurred
     */
    public void run(long secondsToRun) throws IOException {

        boolean runningFlag = true;
        long startTime = (System.currentTimeMillis() / 1000);
        long secondsRunning = 0;

        // Setup Tasks
        if (!preformSetupTasks()) {
            runningFlag = false;
        }

        // main loop
        while (runningFlag && (secondsRunning < secondsToRun)) {

            this.logger.info("Scanning for active solo queue games");

            if (this.league.loadActiveSoloQueueGames(this.riotApiHandler)) {
                for (SoloQueueGame game : this.league.getActiveSoloQueueGames()) {

                    SoloQueueTeam blueTeam = game.getBlueTeam();
                    SoloQueueTeam redTeam = game.getRedTeam();
                    HashMap<Player, Integer> blueTeamStreamers;
                    HashMap<Player, Integer> redTeamStreamers;
                    if (!blueTeam.getPlayers().isEmpty()) {
                        blueTeamStreamers = this.twitchApiHandler.getStreamersOnTeam(game.getBlueTeam(),
                                game.getLeague());
                    } else {
                        blueTeamStreamers = new HashMap<>();
                    }
                    if (!redTeam.getPlayers().isEmpty()) {
                        redTeamStreamers = this.twitchApiHandler.getStreamersOnTeam(game.getRedTeam(),
                                game.getLeague());
                    } else {
                        redTeamStreamers = new HashMap<>();
                    }

                    int gameScore = this.calculateGameScore(game, blueTeamStreamers, redTeamStreamers);

                    if (gameScore >= MINIMUM_GAMESCORE_TO_TWEET && !gameAlreadyTweeted(game)) {
                        JSONObject tweet = this.twitterApiHandler
                                .tweet(createTweet(game, blueTeamStreamers, redTeamStreamers, gameScore));
                        this.tweetedGames.put(game, tweet);
                    }
                }
            }

            this.logger.info("Waiting " + (INTERVAL_TO_SCAN_ACTIVE_GAMES) + " seconds till next scan");

            try {
                Thread.sleep(INTERVAL_TO_SCAN_ACTIVE_GAMES * 1000);
            } catch (InterruptedException e) {
                this.logger.severe("InterruptedException");
                this.preformClosingTasks();
                System.exit(1);
            }

            secondsRunning = (System.currentTimeMillis() / 1000) - startTime;
        }

        this.logger.info("LoLProTwitterBot finished running after " + secondsRunning + " seconds");

        // closing tasks
        this.preformClosingTasks();

    }

    /**
     * Reads in the json from the apiFile that was passed in to the constructor and
     * sets the apiInfoJSON object to a JSONObject of the contents of the file.
     * 
     * @throws FileNotFoundException If the apiInfoFile could not be found
     */
    private void readInApiFile() throws FileNotFoundException {
        Scanner fileScanner = new Scanner(this.apiInfoFile);
        String fileContents = "";

        while (fileScanner.hasNext()) {
            fileContents += fileScanner.next();
        }

        fileScanner.close();
        this.apiInfoJSON = new JSONObject(fileContents);
    }

    /**
     * Returns the value attached to the "riotApiKey" key in apiInfoJSON
     * 
     * @return The api key for the Riot Games API
     */
    private String getRiotApiKey() {
        return this.apiInfoJSON.get("riotApiKey").toString();
    }

    /**
     * Returns the value attached to the "riotRegion" key in apiInfoJSON
     * 
     * @return The region that the Riot Games API should make calls to
     */
    private String getRiotRegion() {
        return this.apiInfoJSON.get("riotRegion").toString();
    }

    /**
     * Returns the value attached to the "twitchClientId" key in apiInfoJSON
     * 
     * @return The twitch client id for this app
     */
    private String getTwitchClientId() {
        return this.apiInfoJSON.get("twitchClientId").toString();
    }

    /**
     * Returns the value attached to the "twitchClientSecret" key in apiInfoJSON
     * 
     * @return The twitch client secret for authentication
     */
    private String getTwitchClientSecret() {
        return this.apiInfoJSON.get("twitchClientSecret").toString();
    }

    /**
     * Returns the value attached to the "twitterConsumerKey" key in apiInfoJSON
     * 
     * @return The twitter consumer api key
     */
    private String getTwitterConsumerKey() {
        return this.apiInfoJSON.getString("twitterConsumerKey").toString();
    }

    /**
     * Returns the value attached to the "twitterConsumerSecret" key in apiInfoJSON
     * 
     * @return The twitter consumer secret
     */
    private String getTwitterConsumerSecret() {
        return this.apiInfoJSON.getString("twitterConsumerSecret").toString();
    }

    /**
     * Returns the value attached to the "twitterToken" key in apiInfoJSON
     * 
     * @return The twitter access token
     */
    private String getTwitterToken() {
        return this.apiInfoJSON.getString("twitterToken").toString();
    }

    /**
     * Returns the value attached to the "twitterTokenSecret" key in apiInfoJSON
     * 
     * @return The twitter access token secret
     */
    private String getTwitterTokenSecret() {
        return this.apiInfoJSON.getString("twitterTokenSecret").toString();
    }

    /**
     * Method to calculate a "gamescore" value for the passed in game to determine
     * how entertaining the game would be to watch
     * 
     * @param gameToScore The game to score
     * @return An integer representing the "gamescore" value
     */
    private int calculateGameScore(SoloQueueGame gameToScore, HashMap<Player, Integer> blueTeamStreamers,
            HashMap<Player, Integer> redTeamStreamers) {
        SoloQueueTeam blueTeam = gameToScore.getBlueTeam();
        SoloQueueTeam redTeam = gameToScore.getRedTeam();

        int gameScore = 0;
        int numberOfMainTeamPlayers = 0;
        int numberOfAcademyPlayers = 0;
        int heighestViewCount = 0;

        for (Player player : blueTeamStreamers.keySet()) {
            if (player != null) {
                if (blueTeamStreamers.get(player) > 0) {
                    if (blueTeamStreamers.get(player) > heighestViewCount) {
                        heighestViewCount = blueTeamStreamers.get(player);
                    }
                }
            }
        }
        for (Player player : redTeamStreamers.keySet()) {
            if (player != null) {
                if (redTeamStreamers.get(player) > 0) {
                    if (redTeamStreamers.get(player) > heighestViewCount) {
                        heighestViewCount = redTeamStreamers.get(player);
                    }
                }
            }
        }

        if (heighestViewCount > 0) {
            for (Player player : blueTeam.getPlayers().keySet()) {
                if (player != null) {
                    if (player.getTeam().contains("Academy") || player.getTeam().contains("academy")) {
                        numberOfAcademyPlayers++;
                    } else {
                        numberOfMainTeamPlayers++;
                    }
                }
            }
            for (Player player : redTeam.getPlayers().keySet()) {
                if (player != null) {
                    if (player.getTeam().contains("Academy") || player.getTeam().contains("academy")) {
                        numberOfAcademyPlayers++;
                    } else {
                        numberOfMainTeamPlayers++;
                    }
                }
            }

            gameScore = (1000 * numberOfMainTeamPlayers) + (500 * numberOfAcademyPlayers)
                    + (int) (8.5 * Math.pow(heighestViewCount, 0.6));
        }

        return gameScore;
    }

    /**
     * Method to form a string containing information about the desired game to
     * tweet about
     * 
     * @param gameToTweet       The game to tweet about
     * @param blueTeamStreamers A HashMap with Players as the key and Integers and
     *                          the value to represent streamers on the blue team
     *                          and their viewcounts
     * @param redTeamStreamers  A HashMap with Players as the key and Integers and
     *                          the value to represent streamers on the red team and
     *                          their viewcounts
     * @return A String to be tweeted
     */
    private String createTweet(SoloQueueGame gameToTweet, HashMap<Player, Integer> blueTeamStreamers,
            HashMap<Player, Integer> redTeamStreamers, int gameScore) {

        SoloQueueTeam blueTeam = gameToTweet.getBlueTeam();
        SoloQueueTeam redTeam = gameToTweet.getRedTeam();

        String tweet = null;

        Entry<Player, Integer> heighestViewCountStreamer = null;

        for (Entry<Player, Integer> entry : blueTeamStreamers.entrySet()) {
            if (entry.getKey() != null) {
                if (heighestViewCountStreamer == null) {
                    heighestViewCountStreamer = entry;
                } else if (entry.getValue() > heighestViewCountStreamer.getValue()) {
                    heighestViewCountStreamer = entry;
                }
            }
        }

        for (Entry<Player, Integer> entry : redTeamStreamers.entrySet()) {
            if (entry.getKey() != null) {
                if (heighestViewCountStreamer == null) {
                    heighestViewCountStreamer = entry;
                } else if (entry.getValue() > heighestViewCountStreamer.getValue()) {
                    heighestViewCountStreamer = entry;
                }
            }
        }

        if (heighestViewCountStreamer != null) {
            if (!heighestViewCountStreamer.getKey().getTwitterHandle().equals("")) {
                tweet = "(" + gameScore + ") Watch @" + heighestViewCountStreamer.getKey().getTwitterHandle() + " ";
            } else {
                tweet = "(" + gameScore + ") Watch " + heighestViewCountStreamer.getKey().getName() + " ";
            }

            if (blueTeam.getPlayers().containsKey(heighestViewCountStreamer.getKey())) {

                if (blueTeam.getPlayers().size() > 1) {
                    tweet += "playing with ";
                    for (Player player : blueTeam.getPlayers().keySet()) {
                        if (player != null && player != heighestViewCountStreamer.getKey()) {
                            if (!player.getTwitterHandle().equals("")) {
                                tweet += "@" + player.getTwitterHandle() + ", ";
                            } else {
                                tweet += player.getName() + ", ";
                            }
                        }
                    }
                }

                if (redTeam.getPlayers().size() > 0) {
                    tweet += "against ";
                    for (Player player : redTeam.getPlayers().keySet()) {
                        if (player != null) {
                            if (!player.getTwitterHandle().equals("")) {
                                tweet += "@" + player.getTwitterHandle() + ", ";
                            } else {
                                tweet += player.getName() + ", ";
                            }
                        }
                    }
                }
            } else {

                if (redTeam.getPlayers().size() > 1) {
                    tweet += "playing with ";
                    for (Player player : redTeam.getPlayers().keySet()) {
                        if (player != null && player != heighestViewCountStreamer.getKey()) {
                            if (!player.getTwitterHandle().equals("")) {
                                tweet += "@" + player.getTwitterHandle() + ", ";
                            } else {
                                tweet += player.getName() + ", ";
                            }
                        }
                    }
                }

                if (blueTeam.getPlayers().size() > 0) {
                    tweet += "against ";
                    for (Player player : blueTeam.getPlayers().keySet()) {
                        if (player != null) {
                            if (!player.getTwitterHandle().equals("")) {
                                tweet += "@" + player.getTwitterHandle() + ", ";
                            } else {
                                tweet += player.getName() + ", ";
                            }
                        }
                    }
                }
            }

            tweet += "here: https://www.twitch.tv/" + heighestViewCountStreamer.getKey().getTwitchName();
        }

        return tweet;
    }

    /**
     * Function to check if a game has already been tweeted so duplicate tweets are
     * not made.
     * 
     * @param gameToCheck The game to check if it has been tweeted
     * @return True if the game has been tweeted and false otherwise
     */
    private boolean gameAlreadyTweeted(SoloQueueGame gameToCheck) {
        for (SoloQueueGame game : this.tweetedGames.keySet()) {
            if (game != null) {
                if (game.getGameId() == gameToCheck.getGameId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean preformSetupTasks() {
        if (!this.riotApiHandler.isWorking()) {
            return false;
        }
        if (!this.twitchApiHandler.loadToken()) {
            return false;
        }
        if (!this.league.loadPlayers(this.playerRosterFile)) {
            return false;
        }
        if (!this.league.loadPlayerSummonerIds(this.riotApiHandler)) {
            return false;
        }
        if (!this.twitchApiHandler.loadTwitchUserIds(this.league)) {
            return false;
        }
        return true;
    }

    /**
     * Method to close the logger and revoke the twitch authentication token when
     * closing the program.
     * 
     * @return True if closing tasks preformed successfully and false otherwise
     */
    private boolean preformClosingTasks() {
        try {
            this.logger.info("Closing Logger");
            for (Handler handler : this.logger.getHandlers()) {
                handler.close();
            }
            this.twitchApiHandler.revokeToken();
            return true;
        } catch (Exception e) {
            return false;
        }

    }

}