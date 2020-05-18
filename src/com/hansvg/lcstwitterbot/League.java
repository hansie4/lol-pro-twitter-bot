package com.hansvg.lcstwitterbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.json.JSONObject;

class League {

    private ArrayList<Player> players;
    private ArrayList<SoloQueueGame> activeSoloQueueGames;
    private TwitterBotLogger twitterBotLogger;

    League(TwitterBotLogger twitterBotLogger) {
        this.players = new ArrayList<>();
        this.activeSoloQueueGames = new ArrayList<>();
        this.twitterBotLogger = twitterBotLogger;
    }

    protected ArrayList<Player> getPlayers() {
        return this.players;
    }

    protected ArrayList<SoloQueueGame> getActiveSoloQueueGames() {
        return this.activeSoloQueueGames;
    }

    protected void loadPlayers(File playerRosterFile) throws FileNotFoundException {
        // LOG
        this.twitterBotLogger.log("LEAGUE", "Beginning to load players");
        Scanner playerRosterScanner = new Scanner(playerRosterFile);

        if (playerRosterScanner.hasNextLine()) {
            playerRosterScanner.nextLine();

            while (playerRosterScanner.hasNextLine()) {

                String[] playerInfo = playerRosterScanner.nextLine().split(",", 7);

                Player readInPlayer = new Player(playerInfo[0], playerInfo[1], playerInfo[2], playerInfo[3],
                        playerInfo[4], playerInfo[5], playerInfo[6].split(","));

                players.add(readInPlayer);
            }
        }
        playerRosterScanner.close();
        // LOG
        this.twitterBotLogger.log("LEAGUE",
                (players.size() + " players loaded from \"" + playerRosterFile.toPath() + "\""));
        this.twitterBotLogger.log("LEAGUE", "Finished loading players");
    }

    protected void loadPlayerSummonerIds(RiotApiRequester riotApiRequester) {
        // LOG
        this.twitterBotLogger.log("LEAGUE", "Beginning to load summoner IDs");
        System.out.println("-------------Beginning to load summoner IDs--------------");
        double percentComplete = 0.0;
        for (int i = 0; i < this.players.size(); i++) {

            Player currentPlayer = this.players.get(i);
            String[] summonerNames = currentPlayer.getSummonerNames();

            for (int j = 0; j < summonerNames.length; j++) {
                JSONObject summonerJSON = riotApiRequester.getSummoner(summonerNames[j]);

                if (summonerJSON != null) {
                    currentPlayer.getSummonerIds()[j] = summonerJSON.getString("id");
                } else {
                    // LOG
                    this.twitterBotLogger.log("LEAGUE ERROR",
                            "Summoner ID for " + summonerNames[j] + " could not be retrieved from Riot Games API");
                    currentPlayer.getSummonerIds()[j] = null;
                }
            }

            // Loading bar
            percentComplete = ((double) i / (double) (players.size() - 1));
            System.out.print("|");
            for (int c = 0; c < 50; c++) {
                if (c < (int) (percentComplete * 50)) {
                    System.out.print("#");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.print("| " + (int) (percentComplete * 100) + "%\r");

        }
        System.out.println("\n-------------Finished loading summoner IDs---------------\n");
        // LOG
        this.twitterBotLogger.log("LEAGUE", "Finished loading summoner IDs");
    }

    protected void loadSoloQueueGames(RiotApiRequester riotApiRequester) {
        // LOG
        this.twitterBotLogger.log("LEAGUE", "Beginning to load Active Solo Queue Games");
        System.out.println("-----------Beginning to load Solo Queue Games------------");

        double percentComplete = 0.0;
        ArrayList<String> idsToScan = this.getAllSummonerIds();
        int originalNumberOfIdsToScan = idsToScan.size();

        while (idsToScan.size() > 0) {
            JSONObject gameJSON = riotApiRequester.getLiveGameInfo(idsToScan.get(0));

            if (gameJSON != null) {
                SoloQueueGame newSoloQueueGame = new SoloQueueGame(this, gameJSON);
                this.activeSoloQueueGames.add(newSoloQueueGame);
                updateIDsToScan(idsToScan, newSoloQueueGame.getParticipants());
            } else {
                idsToScan.remove(0);
            }

            // Loading bar
            percentComplete = ((double) (originalNumberOfIdsToScan - idsToScan.size())
                    / (double) (originalNumberOfIdsToScan));
            System.out.print("|");
            for (int c = 0; c < 50; c++) {
                if (c < (int) (percentComplete * 50)) {
                    System.out.print("#");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.print("| " + (int) (percentComplete * 100) + "%\r");

        }

        this.activeSoloQueueGames.removeAll(Collections.singleton(null));

        System.out.println("\n------------Finished loading Solo Queue Games------------\n");
        // LOG
        this.twitterBotLogger.log("LEAGUE", "Found " + this.activeSoloQueueGames.size() + " active solo queue games");
        this.twitterBotLogger.log("LEAGUE", "Finished loading Active Solo Queue Games");
    }

    protected Player getPlayerFromSummonerName(String summonerName) {
        for (Player p : this.players) {
            if (p.ownsSummonerName(summonerName)) {
                return p;
            }
        }
        return null;
    }

    protected Player getPlayerFromSummonerId(String summonerId) {
        for (Player p : this.players) {
            if (p.ownsSummonerId(summonerId)) {
                return p;
            }
        }
        return null;
    }

    private ArrayList<String> getAllSummonerIds() {
        ArrayList<String> ids = new ArrayList<>();
        for (Player p : this.players) {
            for (String id : p.getSummonerIds()) {
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
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

}