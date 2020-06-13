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

import org.json.JSONException;
import org.json.JSONObject;

public class LoLProTwitterBot {

    private File playerRosterFile;
    private File apiInfoFile;

    private League league;

    private JSONObject apiInfoJSON;

    private RiotApiHandler riotApiHandler;
    private TwitchApiHandler twitchApiHandler;

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
        this.logger = new Logger(logFile);
        this.logger.open();
        this.league = new League(this.logger);
        this.readInApiFile();
        this.riotApiHandler = new RiotApiHandler(getRiotApiKey(), getRiotRegion(), this.logger);
        this.twitchApiHandler = new TwitchApiHandler(getTwitchClientId(), getTwitchClientSecret(), this.logger);
        System.out.println("LCSTwitterBot Created");
    }

    /**
     * The method used for having the twitter bot start running.
     * 
     * @throws IOException If an input or output exception occurred
     */
    public void run() throws IOException {

        boolean runningFlag = true;

        this.logger.open();

        if (runningFlag) {
            if (this.riotApiHandler.isWorking()) {
                System.out.println("RiotApiHandler is Working");
            } else {
                System.out.println("RiotApiHandler not Working");
                runningFlag = false;
            }
        }

        if (runningFlag) {
            if (this.twitchApiHandler.loadToken()) {
                System.out.println("Successfully retrieved token from Twitch Api");
            } else {
                System.out.println("Unsuccessfully retrieved token from Twitch Api");
                runningFlag = false;
            }
        }

        if (runningFlag) {
            if (this.league.loadPlayers(this.playerRosterFile)) {
                System.out.println("Successfully loaded players from file");
            } else {
                System.out.println("Unsuccessfully loaded players from file");
                runningFlag = false;
            }
        }

        if (runningFlag) {
            if (this.league.loadPlayerSummonerIds(this.riotApiHandler)) {
                System.out.println("Successfully loaded player summoner ids from Riot Games Api");
            } else {
                System.out.println("Unsuccessfully loaded player summoner ids from Riot Games Api");
                runningFlag = false;
            }
        }

        if (runningFlag) {
            if (this.twitchApiHandler.loadTwitchUserIds(this.league)) {
                System.out.println("Successfully loaded twitch user ids from Twitch Api");
            } else {
                System.out.println("Unsuccessfully loaded twitch user ids from Twitch Api");
                runningFlag = false;
            }
        }

        if (runningFlag) {
            if (this.league.loadActiveSoloQueueGames(this.riotApiHandler)) {
                System.out.println("Successfully loaded active solo queue games");
            } else {
                System.out.println("Unsuccessfully loaded active solo queue games");
                runningFlag = false;
            }
        }

        for (SoloQueueGame game : this.league.getActiveSoloQueueGames()) {
            game.printGameInfo();
            System.out.println("Streamers: ");

            HashMap<Player, Integer> blueTeam = this.twitchApiHandler.getStreamersOnTeam(game.getBlueTeam(),
                    game.getLeague());
            System.out.println("\tBlue Team Streamers: ");
            for (Player p : blueTeam.keySet()) {
                if (p != null) {
                    if (p.getTwitchUserId() != null) {
                        System.out.println("\t\t" + p.getName() + " | " + p.getTwitchName() + " | "
                                + p.getTwitchUserId() + " | Viewers " + blueTeam.get(p));
                    } else {
                        System.out.println(
                                "\t\t" + p.getName() + " | " + p.getTwitchName() + " | Viewers " + blueTeam.get(p));
                    }
                }
            }

            HashMap<Player, Integer> redTeam = this.twitchApiHandler.getStreamersOnTeam(game.getRedTeam(),
                    game.getLeague());
            System.out.println("\tRed Team Streamers: ");
            for (Player p : redTeam.keySet()) {
                if (p != null) {
                    if (p.getTwitchUserId() != null) {
                        System.out.println("\t\t" + p.getName() + " | " + p.getTwitchName() + " | "
                                + p.getTwitchUserId() + " | Viewers " + redTeam.get(p));
                    } else {
                        System.out.println(
                                "\t\t" + p.getName() + " | " + p.getTwitchName() + " | Viewers " + redTeam.get(p));
                    }
                }
            }

            System.out.println();
        }

        if (runningFlag) {
            if (this.twitchApiHandler.revokeToken()) {
                System.out.println("Successfully revoked twitch api token");
            } else {
                System.out.println("Unsuccessfully revoked twitch api token");
            }
        }

        this.logger.close();
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

}