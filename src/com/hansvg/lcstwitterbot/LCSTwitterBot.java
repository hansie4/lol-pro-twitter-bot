package com.hansvg.lcstwitterbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class LCSTwitterBot {

    private File playerRosterFile;
    private League league;

    private File riotApiInfoFile;
    private JSONObject riotApiInfoJSON;
    private RiotApiRequester riotApiRequester;

    private TwitterBotLogger twitterBotLogger;

    public LCSTwitterBot(File playerRosterFile, File riotApiInfoFile, File twitterbotLogFile)
            throws JSONException, IOException {
        this.playerRosterFile = playerRosterFile;
        this.riotApiInfoFile = riotApiInfoFile;
        this.twitterBotLogger = new TwitterBotLogger(twitterbotLogFile);
        this.league = new League(twitterBotLogger);
        this.riotApiInfoJSON = new JSONObject(readInRiotApiFile());
        this.riotApiRequester = new RiotApiRequester(getRiotApiKey(riotApiInfoJSON), getRiotRegion(riotApiInfoJSON),
                getRiotRequestRate(riotApiInfoJSON), twitterBotLogger);
        System.out.println("LCSTwitterBot Created");
    }

    public void start() throws IOException {

        this.twitterBotLogger.open();

        boolean runningFlag = true;

        if (this.riotApiRequester.isWorking()) {
            System.out.println("RiotApiRequester Working");
            if (this.league.loadPlayers(this.playerRosterFile)) {
                System.out.println("Successfully Loaded Players");
                if (this.league.loadPlayerSummonerIds(this.riotApiRequester)) {
                    System.out.println("Successfully Summoner Ids");
                } else {
                    System.out.println("Unsuccessfully Summoner Ids");
                    runningFlag = false;
                }
            } else {
                System.out.println("Unsuccessfully Loaded Players");
                runningFlag = false;
            }
        } else {
            System.out.println("RiotApiRequester Not Working");
            runningFlag = false;
        }

        if (runningFlag) {
            this.league.loadActiveSoloQueueGames(riotApiRequester);
        }

        System.out.println("Found " + this.league.getActiveSoloQueueGames().size() + " active solo queue games.");
        for (SoloQueueGame game : this.league.getActiveSoloQueueGames()) {
            game.printGameInfo();
        }

        this.twitterBotLogger.close();
    }

    private String readInRiotApiFile() throws FileNotFoundException {
        Scanner fileScanner = new Scanner(this.riotApiInfoFile);
        String fileContents = "";

        while (fileScanner.hasNext()) {
            fileContents += fileScanner.next();
        }

        fileScanner.close();
        return fileContents;
    }

    private static String getRiotApiKey(JSONObject riotJSON) {
        return riotJSON.get("apiKey").toString();
    }

    private static String getRiotRegion(JSONObject riotJSON) {
        return riotJSON.get("region").toString();
    }

    private static double getRiotRequestRate(JSONObject riotJSON) {
        return riotJSON.getDouble("requestRate");
    }

}