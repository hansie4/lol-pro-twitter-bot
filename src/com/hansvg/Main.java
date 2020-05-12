package com.hansvg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.hansvg.lcstwitterbot.Game;
import com.hansvg.lcstwitterbot.League;
import com.hansvg.lcstwitterbot.RiotApiRequester;

import org.json.JSONObject;

public class Main {

    private static final String riotInfoFilePath = "data\\RiotApiInfo.json";
    private static final String playerRosterFilePath = "data\\LoL_Roster_Summer_2020 - NALCS.csv";

    public static void main(String[] args) {

        try {

            JSONObject riotApiJSON = new JSONObject(readInRiotFile(riotInfoFilePath));

            RiotApiRequester riotApiRequester = new RiotApiRequester(getRiotApiKey(riotApiJSON),
                    getRiotRegion(riotApiJSON), getRiotRequestsPerSecond(riotApiJSON));

            League lcs = new League(playerRosterFilePath);

            lcs.loadPlayerSummonerIDs(riotApiRequester);
            lcs.loadActiveSoloQueueGames(riotApiRequester);

            for (Game g : lcs.getActiveGames()) {
                System.out.println("GameID: " + g.getGameID() + "| Participants: ");

                for (JSONObject o : g.getParticipants()) {
                    System.out.print(o.getString("summonerName") + " | ");
                }
                System.out.println();
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private static String readInRiotFile(String riotFileName) throws FileNotFoundException {

        File riotFile = new File(riotFileName);
        Scanner fileScanner = new Scanner(riotFile);
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