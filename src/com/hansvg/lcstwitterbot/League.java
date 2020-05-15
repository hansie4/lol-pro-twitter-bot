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

    League() {
        this.players = new ArrayList<>();
        this.activeSoloQueueGames = new ArrayList<>();
    }

    protected ArrayList<Player> getPlayers() {
        return this.players;
    }

    protected ArrayList<SoloQueueGame> getActiveSoloQueueGames() {
        return this.activeSoloQueueGames;
    }

    protected void loadPlayers(File playerRosterFile) throws FileNotFoundException {
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
    }

    protected void loadPlayerSummonerIds(RiotApiRequester riotApiRequester) {
        System.out.println("-------------Beginning to load summoner IDs--------------");
        double percentComplete = 0.0;
        for (int i = 0; i < this.players.size(); i++) {

            Player currentPlayer = this.players.get(i);
            String[] summonerNames = currentPlayer.getSummonerNames();

            for (int j = 0; j < summonerNames.length; j++) {

                String summonerIdForSummonerName = riotApiRequester.getSummoner(summonerNames[j]).getString("id");

                if (summonerIdForSummonerName != null) {
                    currentPlayer.getSummonerIds()[j] = summonerIdForSummonerName;
                } else {
                    System.out.println("Null summoner ID for summoner name: " + summonerNames[j]);
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
        System.out.println("\n-------------Finished loading summoner IDs---------------");
    }

    protected void loadSoloQueueGames(RiotApiRequester riotApiRequester) {
        System.out.println("------------Beginning to load Solo Queue Games-----------");
        double percentComplete = 0.0;
        for (int i = 0; i < this.players.size(); i++) {
            Player currentPlayer = this.players.get(i);
            for (int j = 0; j < currentPlayer.getSummonerIds().length; j++) {

                String summonerID = currentPlayer.getSummonerIds()[j];

                if (summonerID != null) {
                    JSONObject gameJSON = riotApiRequester.getLiveGameInfo(summonerID);

                    if (gameJSON != null) {
                        SoloQueueGame newSoloQueueGame = new SoloQueueGame(this, gameJSON);
                        this.activeSoloQueueGames.add(newSoloQueueGame);
                    }
                } else {
                    System.out.println("Null SummonerID: " + summonerID);
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
        System.out.println("\n-------------Finish loding Solo Queue Games--------------");
        this.mergeSoloQueueGames();
    }

    protected void mergeSoloQueueGames() {
        this.activeSoloQueueGames.removeAll(Collections.singleton(null));

        ArrayList<Long> uniqueGameIDs = new ArrayList<>();
        ArrayList<SoloQueueGame> uniqueGames = new ArrayList<>();

        for (SoloQueueGame soloQueueGame : activeSoloQueueGames) {
            if (!uniqueGameIDs.contains(soloQueueGame.getGameId())) {
                uniqueGameIDs.add(soloQueueGame.getGameId());
                uniqueGames.add(soloQueueGame);
            }
        }

        this.activeSoloQueueGames = uniqueGames;
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

}