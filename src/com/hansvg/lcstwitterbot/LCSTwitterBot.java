package com.hansvg.lcstwitterbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class LCSTwitterBot {

    private File playerRosterFile;
    private File riotApiInfoFile;

    private League league;

    private JSONObject riotApiInfoJSON;
    private RiotApiRequester riotApiRequester;

    public LCSTwitterBot(File playerRosterFile, File riotApiInfoFile) throws JSONException, FileNotFoundException {
        this.playerRosterFile = playerRosterFile;
        this.riotApiInfoFile = riotApiInfoFile;
        this.league = new League();
        this.riotApiInfoJSON = new JSONObject(readInRiotApiFile());
        this.riotApiRequester = new RiotApiRequester(getRiotApiKey(riotApiInfoJSON), getRiotRegion(riotApiInfoJSON),
                getRiotRequestsPerSecond(riotApiInfoJSON));
    }

    public void start() throws FileNotFoundException {

        league.loadPlayers(this.playerRosterFile);
        league.loadPlayerSummonerIds(riotApiRequester);
        league.loadSoloQueueGames(riotApiRequester);

        System.out.println("Unique Solo Queue Games Found: " + league.getActiveSoloQueueGames().size());

        for (SoloQueueGame g : league.getActiveSoloQueueGames()) {
            g.printGameInfo();
        }
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