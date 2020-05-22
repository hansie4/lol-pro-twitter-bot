/**
 * The LCSTwitterBot class is the main class for the LCSTwitterBot API.
 * It creates the League, Logger, and Handlers as well as calls all the
 * functions/methods to have the twitter bot run.
 * 
 * @author Hans Von Gruenigen
 * @version 1.0
 */

package com.hansvg.lcstwitterbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class LCSTwitterBot {

    private File playerRosterFile;
    private File apiInfoFile;

    private League league;

    private JSONObject apiInfoJSON;
    private RiotApiHandler riotApiHandler;

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
    public LCSTwitterBot(File playerRosterFile, File apiInfoFile, File logFile) throws JSONException, IOException {
        this.playerRosterFile = playerRosterFile;
        this.apiInfoFile = apiInfoFile;
        this.logger = new Logger(logFile);
        this.league = new League(this.logger);
        readInApiFile();
        this.riotApiHandler = new RiotApiHandler(getRiotApiKey(), getRiotRegion(), this.logger);
        System.out.println("LCSTwitterBot Created");
    }

    /**
     * The method used for having the twitter bot start running.
     * 
     * @throws IOException If an input or output exception occurred
     */
    public void start() throws IOException {

        this.logger.open();

        boolean runningFlag = true;

        if (this.riotApiHandler.isWorking()) {
            System.out.println("RiotApiRequester Tested and Working");
            // LOG
            this.logger.log("", "RiotApiRequester Tested and Working");
            if (this.league.loadPlayers(this.playerRosterFile)) {
                System.out.println("Successfully Loaded Players");
                // LOG
                this.logger.log("", "Successfully Loaded Players");
                if (this.league.loadPlayerSummonerIds(this.riotApiHandler)) {
                    System.out.println("Successfully Loaded Summoner Ids");
                    // LOG
                    this.logger.log("", "Successfully Loaded Summoner Ids");
                } else {
                    System.out.println("Unsuccessfully Loaded Summoner Ids");
                    // LOG
                    this.logger.log("", "Unsuccessfully Loaded Summoner Ids");
                    runningFlag = false;
                }
            } else {
                System.out.println("Unsuccessfully Loaded Players");
                // LOG
                this.logger.log("", "Unsuccessfully Loaded Players");
                runningFlag = false;
            }
        } else {
            System.out.println("RiotApiRequester Not Working");
            // LOG
            this.logger.log("", "RiotApiRequester Not Working");
            runningFlag = false;
        }

        if (runningFlag) {
            this.league.loadActiveSoloQueueGames(this.riotApiHandler);

            System.out.println("Found " + this.league.getActiveSoloQueueGames().size() + " active solo queue game(s).");
            for (SoloQueueGame game : this.league.getActiveSoloQueueGames()) {
                game.printGameInfo();
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
     * @return The api key for the Riot Games api
     */
    private String getRiotApiKey() {
        return this.apiInfoJSON.get("riotApiKey").toString();
    }

    /**
     * Returns the value attached to the "riotRegion" key in apiInfoJSON
     * 
     * @return
     */
    private String getRiotRegion() {
        return this.apiInfoJSON.get("riotRegion").toString();
    }

}