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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.json.JSONObject;

public class LoLProTwitterBot {

    private Properties configs;

    private int MINIMUM_GAMESCORE_TO_TWEET;
    private int INTERVAL_TO_SCAN_ACTIVE_GAMES_IN_SECONDS;

    private File playerRosterFile;

    private League league;

    private RiotApiHandler riotApiHandler;
    private TwitchApiHandler twitchApiHandler;
    private TwitterApiHandler twitterApiHandler;

    private Logger logger;

    private HashMap<SoloQueueGame, JSONObject> tweetedGames;

    /**
     * LCSTwitterBot Class Constructor.
     * 
     * @param configFileLocation The location of the file containing the configs for
     *                           the twitter bot
     * @throws Exception
     * @throws NumberFormatException
     */
    public LoLProTwitterBot(String configFileLocation) throws NumberFormatException, Exception {
        if (loadConfigs(configFileLocation)) {
            this.logger = Logger.getLogger("Logger");
            FileHandler loggerFileHandler = new FileHandler(this.configs.getProperty("LOCATION_FOR_LOG_FILE"), true);
            this.logger.addHandler(loggerFileHandler);
            loggerFileHandler.setFormatter(new SimpleFormatter());

            this.MINIMUM_GAMESCORE_TO_TWEET = Integer.parseInt(this.configs.getProperty("MINIMUM_GAMESCORE_TO_TWEET"));
            this.INTERVAL_TO_SCAN_ACTIVE_GAMES_IN_SECONDS = Integer
                    .parseInt(this.configs.getProperty("INTERVAL_TO_SCAN_ACTIVE_GAMES_IN_SECONDS"));

            this.playerRosterFile = new File(this.configs.getProperty("PLAYER_ROSTER_FILE_LOCATION"));

            this.league = new League(this.logger);

            this.riotApiHandler = new RiotApiHandler(this.configs, this.logger);
            this.logger.info("RiotApiHandler Created");

            this.twitchApiHandler = new TwitchApiHandler(this.configs, this.logger);
            this.logger.info("TwitchApiHandler Created");

            this.twitterApiHandler = new TwitterApiHandler(this.configs, this.logger);
            this.logger.info("TwitterApiHandler Created");

            this.tweetedGames = new HashMap<>();
        } else {
            System.out.println("Configs could not be loaded. Closing LoLProTwitterBot.");
            System.exit(1);
        }
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

        // checking if to run for a set amount of time or to run continuously
        if (secondsToRun >= 0) {
            while (runningFlag && (secondsRunning < secondsToRun)) {

                this.logger.info("Scanning for active solo queue games for " + secondsToRun + " seconds");

                if (this.league.loadActiveSoloQueueGames(this.riotApiHandler)) {
                    for (SoloQueueGame game : this.league.getActiveSoloQueueGames()) {

                        SoloQueueTeam blueTeam = game.getBlueTeam();
                        SoloQueueTeam redTeam = game.getRedTeam();
                        HashMap<Player, Integer> blueTeamStreamers;
                        HashMap<Player, Integer> redTeamStreamers;
                        if (!blueTeam.getPlayers().isEmpty() && blueTeam.hasStreamers()) {
                            blueTeamStreamers = this.twitchApiHandler.getStreamersOnTeam(game.getBlueTeam(),
                                    game.getLeague());
                            if (blueTeamStreamers != null) {
                                blueTeamStreamers = new HashMap<>();
                            }
                        } else {
                            blueTeamStreamers = new HashMap<>();
                        }
                        if (!redTeam.getPlayers().isEmpty() && redTeam.hasStreamers()) {
                            redTeamStreamers = this.twitchApiHandler.getStreamersOnTeam(game.getRedTeam(),
                                    game.getLeague());
                            if (redTeamStreamers != null) {
                                redTeamStreamers = new HashMap<>();
                            }
                        } else {
                            redTeamStreamers = new HashMap<>();
                        }

                        int gameScore = this.calculateGameScore(game, blueTeamStreamers, redTeamStreamers);
                        System.out
                                .println("-----------------------GameScore: " + gameScore + "-----------------------");
                        game.printGameInfo(blueTeamStreamers, redTeamStreamers);

                        if (gameScore >= MINIMUM_GAMESCORE_TO_TWEET && !gameAlreadyTweeted(game)) {
                            JSONObject tweet = this.twitterApiHandler
                                    .tweet(createTweet(game, blueTeamStreamers, redTeamStreamers, gameScore));
                            this.tweetedGames.put(game, tweet);
                        }
                    }
                }

                this.logger.info("Waiting " + (INTERVAL_TO_SCAN_ACTIVE_GAMES_IN_SECONDS) + " seconds till next scan");

                try {
                    Thread.sleep(INTERVAL_TO_SCAN_ACTIVE_GAMES_IN_SECONDS * 1000);
                } catch (InterruptedException e) {
                    this.logger.severe("InterruptedException");
                    this.preformClosingTasks();
                    System.exit(1);
                }

                secondsRunning = (System.currentTimeMillis() / 1000) - startTime;
            }
        } else {
            while (runningFlag) {
                this.logger.info("Scanning for active solo queue games continuously");

                if (this.league.loadActiveSoloQueueGames(this.riotApiHandler)) {
                    for (SoloQueueGame game : this.league.getActiveSoloQueueGames()) {

                        SoloQueueTeam blueTeam = game.getBlueTeam();
                        SoloQueueTeam redTeam = game.getRedTeam();
                        HashMap<Player, Integer> blueTeamStreamers;
                        HashMap<Player, Integer> redTeamStreamers;
                        if (!blueTeam.getPlayers().isEmpty() && blueTeam.hasStreamers()) {
                            blueTeamStreamers = this.twitchApiHandler.getStreamersOnTeam(game.getBlueTeam(),
                                    game.getLeague());
                            if (blueTeamStreamers != null) {
                                blueTeamStreamers = new HashMap<>();
                            }
                        } else {
                            blueTeamStreamers = new HashMap<>();
                        }
                        if (!redTeam.getPlayers().isEmpty() && redTeam.hasStreamers()) {
                            redTeamStreamers = this.twitchApiHandler.getStreamersOnTeam(game.getRedTeam(),
                                    game.getLeague());
                            if (redTeamStreamers != null) {
                                redTeamStreamers = new HashMap<>();
                            }
                        } else {
                            redTeamStreamers = new HashMap<>();
                        }

                        int gameScore = this.calculateGameScore(game, blueTeamStreamers, redTeamStreamers);
                        System.out
                                .println("-----------------------GameScore: " + gameScore + "-----------------------");
                        game.printGameInfo(blueTeamStreamers, redTeamStreamers);

                        if (gameScore >= MINIMUM_GAMESCORE_TO_TWEET && !gameAlreadyTweeted(game)) {
                            JSONObject tweet = this.twitterApiHandler
                                    .tweet(createTweet(game, blueTeamStreamers, redTeamStreamers, gameScore));
                            this.tweetedGames.put(game, tweet);
                        }
                    }
                }

                this.logger.info("Waiting " + (INTERVAL_TO_SCAN_ACTIVE_GAMES_IN_SECONDS) + " seconds till next scan");

                try {
                    Thread.sleep(INTERVAL_TO_SCAN_ACTIVE_GAMES_IN_SECONDS * 1000);
                } catch (InterruptedException e) {
                    this.logger.severe("InterruptedException");
                    this.preformClosingTasks();
                    System.exit(1);
                }

                secondsRunning = (System.currentTimeMillis() / 1000) - startTime;
            }
        }

        this.logger.info("LoLProTwitterBot finished running after " + secondsRunning + " seconds");

        // closing tasks
        this.preformClosingTasks();

    }

    /**
     * Function to load the configs for the twitter bot.
     * 
     * @param configFileLocation The location of the file containing the configs
     * @return True if configs were successfully retrieved.
     */
    private boolean loadConfigs(String configFileLocation) {
        try {
            Properties properties = new Properties();
            FileInputStream fileInputStream = new FileInputStream(configFileLocation);

            properties.load(fileInputStream);

            if (properties.containsKey("LOCATION_FOR_LOG_FILE") && properties.containsKey("PLAYER_ROSTER_FILE_LOCATION")
                    && properties.containsKey("RIOT_API_KEY") && properties.containsKey("RIOT_API_REGION")
                    && properties.containsKey("TWITCH_CLIENT_ID") && properties.containsKey("TWITCH_CLIENT_SECRET")
                    && properties.containsKey("TWITTER_CONSUMER_KEY")
                    && properties.containsKey("TWITTER_CONSUMER_SECRET")
                    && properties.containsKey("TWITTER_ACCESS_TOKEN")
                    && properties.containsKey("TWITTER_ACCESS_TOKEN_SECRET")
                    && properties.containsKey("MINIMUM_GAMESCORE_TO_TWEET")
                    && properties.containsKey("INTERVAL_TO_SCAN_ACTIVE_GAMES_IN_SECONDS")
                    && properties.containsKey("SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED_RIOT_API")
                    && properties.containsKey("SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED_TWITCH_API")
                    && properties.containsKey("SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED_TWITTER_API")) {
                this.configs = properties;
                return true;
            } else {
                System.out.println("Invalid config file.");
                return false;
            }
        } catch (FileNotFoundException e) {
            System.out.println("The file \"" + configFileLocation + "\" could not be found.");
            return false;
        } catch (IOException e) {
            System.out.println("Error reading the file.");
            return false;
        }
    }

    /**
     * Method to calculate a "gamescore" value for the passed in game to determine
     * how entertaining the game would be to watch
     * 
     * @param gameToScore       The game to score
     * @param blueTeamStreamers HashMap of streamers and view counts on blue team
     * @param redTeamStreamers  HashMap of streamers and view counts on red team
     * @return An integer representing the "gamescore" value
     */
    private int calculateGameScore(SoloQueueGame gameToScore, HashMap<Player, Integer> blueTeamStreamers,
            HashMap<Player, Integer> redTeamStreamers) {
        SoloQueueTeam blueTeam = gameToScore.getBlueTeam();
        SoloQueueTeam redTeam = gameToScore.getRedTeam();

        int gameScore;

        if ((blueTeam.getPlayers().size() + redTeam.getPlayers().size()) > 1) {
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
            } else {
                gameScore = 0;
            }
        } else {
            gameScore = 0;
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
     * @param gameScore         The "gameScore" of the game being tweeted
     * @return A String to be tweeted
     */
    private String createTweet(SoloQueueGame gameToTweet, HashMap<Player, Integer> blueTeamStreamers,
            HashMap<Player, Integer> redTeamStreamers, int gameScore) {

        SoloQueueTeam blueTeam = gameToTweet.getBlueTeam();
        SoloQueueTeam redTeam = gameToTweet.getRedTeam();
        ArrayList<Player> blueTeamPlayers = new ArrayList<>(blueTeam.getPlayers().keySet());
        ArrayList<Player> redTeamPlayers = new ArrayList<>(redTeam.getPlayers().keySet());

        StringBuilder tweetString = new StringBuilder("(" + gameScore + ")\n");

        // getting the heigest viewed streamer
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

        tweetString.append("Blue Side: \n");
        if (!blueTeamPlayers.isEmpty()) {
            for (Player player : blueTeamPlayers) {
                if (!player.getTwitterHandle().equals("")) {
                    tweetString.append("    @" + player.getTwitterHandle());
                } else {
                    tweetString.append("    " + player.getTeam() + " " + player.getName());
                }
                tweetString.append("\n");
            }
        } else {
            tweetString.append("\n");
        }

        tweetString.append("Red Side: \n");
        if (!redTeamPlayers.isEmpty()) {
            for (Player player : redTeamPlayers) {
                if (!player.getTwitterHandle().equals("")) {
                    tweetString.append("    @" + player.getTwitterHandle());
                } else {
                    tweetString.append("    " + player.getTeam() + " " + player.getName());
                }
                tweetString.append("\n");
            }
        } else {
            tweetString.append("\n");
        }

        tweetString.append("Watch here: https://www.twitch.tv/" + heighestViewCountStreamer.getKey().getTwitchName());

        return tweetString.toString();
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

    /**
     * Method to check to make sure that all is loaded and all Api handlers are
     * working.
     * 
     * @return True if all tasks were preformed successfully and false otherwises
     */
    private boolean preformSetupTasks() {
        if (!this.riotApiHandler.isWorking()) {
            return false;
        }
        if (!this.twitchApiHandler.loadToken()) {
            this.twitchApiHandler.revokeToken();
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
     * Method to close the logger when closing the program.
     * 
     * @return True if closing tasks preformed successfully and false otherwise
     */
    private boolean preformClosingTasks() {
        try {
            this.logger.info("Closing Logger");
            for (Handler handler : this.logger.getHandlers()) {
                handler.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

}