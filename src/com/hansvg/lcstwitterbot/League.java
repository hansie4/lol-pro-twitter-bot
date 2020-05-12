package com.hansvg.lcstwitterbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class League {

    private ArrayList<Player> players;

    public League(String playerRosterFilePath) throws FileNotFoundException {
        players = new ArrayList<>();
        readInPlayers(playerRosterFilePath);
    }

    public ArrayList<Player> getPlayers() {
        return this.players;
    }

    public void loadPlayerSummonerIDs(RiotApiRequester apiRequester) {
        for (Player player : players) {
            for (int i = 0; i < player.getSummonerNames().length; i++) {
                player.getSummonerIDs()[i] = apiRequester.getSummonerID(player.getSummonerNames()[i]);
            }
        }
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

}