package com.hansvg.lcstwitterbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.json.JSONObject;

public class League {

    private ArrayList<Player> players;
    private ArrayList<Game> activeGames;

    public League(String playerRosterFilePath) throws FileNotFoundException {
        players = new ArrayList<>();
        activeGames = new ArrayList<>();
        readInPlayers(playerRosterFilePath);
    }

    public ArrayList<Player> getPlayers() {
        return this.players;
    }

    public ArrayList<Game> getActiveGames() {
        return this.activeGames;
    }

    private void readInPlayers(String playerRosterFilePath) throws FileNotFoundException {
        File playerRosterFile = new File(playerRosterFilePath);
        Scanner playerScanner = new Scanner(playerRosterFile);
        String[] playerInfoValues;

        for (int i = 0; playerScanner.hasNextLine(); i++) {
            if (i == 0) {
                playerScanner.nextLine();
            } else {
                playerInfoValues = playerScanner.nextLine().split(",", 6);

                if (!playerInfoValues[0].equals("UNKNOWN")) {
                    Player newPlayer = new Player(playerInfoValues[0], playerInfoValues[1], playerInfoValues[2],
                            playerInfoValues[3], playerInfoValues[4], playerInfoValues[5].split(","));
                    players.add(newPlayer);
                }
            }
        }
        playerScanner.close();
    }

    public void loadPlayerSummonerIDs(RiotApiRequester apiRequester) {
        System.out.println("-------------Beginning to load summoner IDs--------------");
        double percentComplete = 0.0;
        for (int i = 0; i < players.size(); i++) {
            Player currentPlayer = players.get(i);
            for (int j = 0; j < currentPlayer.getSummonerNames().length; j++) {
                currentPlayer.getSummonerIDs()[j] = apiRequester.getSummonerID(currentPlayer.getSummonerNames()[j]);
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

    public void loadActiveSoloQueueGames(RiotApiRequester apiRequester) {
        System.out.println("-------------Beginning to load Active Games--------------");
        double percentComplete = 0.0;
        for (int i = 0; i < players.size(); i++) {
            Player currentPlayer = players.get(i);
            for (String summonerID : currentPlayer.getSummonerIDs()) {
                JSONObject gameObjJSON = apiRequester.getLiveGameInfo(summonerID);

                if (gameObjJSON != null) {
                    Game scannedGame = new Game(gameObjJSON, false);
                    activeGames.add(scannedGame);
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
        System.out.println("\n---------------Finish loding Active Games----------------");
        mergeGames();
    }

    private void mergeGames() {
        this.activeGames.removeAll(Collections.singleton(null));

        ArrayList<String> uniqueGameIDs = new ArrayList<>();
        ArrayList<Game> uniqueGames = new ArrayList<>();

        for (Game game : activeGames) {
            if (!uniqueGameIDs.contains(game.getGameID())) {
                uniqueGameIDs.add(game.getGameID());
                uniqueGames.add(game);
            }
        }

        this.activeGames = uniqueGames;
    }

}