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
                getRiotRequestsPerSecond(riotApiInfoJSON), twitterBotLogger);
    }

    public void start() throws IOException {
        this.twitterBotLogger.open();
        this.twitterBotLogger.log("START", "LOL PRO TWITTER BOT START");

        league.loadPlayers(this.playerRosterFile);
        league.loadPlayerSummonerIds(riotApiRequester);
        league.loadSoloQueueGames(riotApiRequester);

        System.out.println("Unique Solo Queue Games Found: " + league.getActiveSoloQueueGames().size());

        for (SoloQueueGame g : league.getActiveSoloQueueGames()) {
            g.printGameInfo();
        }

        this.twitterBotLogger.log("END", "LOL PRO TWITTER BOT END");
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

    private static double getRiotRequestsPerSecond(JSONObject riotJSON) {
        return riotJSON.getDouble("requestsPerSecond");
    }

}