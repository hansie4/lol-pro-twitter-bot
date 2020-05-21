package com.hansvg.lcstwitterbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;

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

    protected boolean loadPlayers(File playerRosterFile) {
        try {
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

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // LOG
            this.twitterBotLogger.log("",
                    "Error when loading in players from \"" + playerRosterFile + "\" " + e.getStackTrace());
            return false;
        }

    }

    protected boolean loadPlayerSummonerIds(RiotApiHandler riotApiHandler) {
        try {
            riotApiHandler.loadSummonerIds(this.players);
            return true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            // LOG
            this.twitterBotLogger.log("",
                    "URISyntaxException from riotApiHandler.loadSummonerIds() " + e.getStackTrace().toString());
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            // LOG
            this.twitterBotLogger.log("",
                    "InterruptedException from riotApiHandler.loadSummonerIds() " + e.getStackTrace().toString());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            // LOG
            this.twitterBotLogger.log("",
                    "IOException from riotApiHandler.loadSummonerIds() " + e.getStackTrace().toString());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            // LOG
            this.twitterBotLogger.log("",
                    "Exception from riotApiHandler.loadSummonerIds() " + e.getStackTrace().toString());
            return false;
        }
    }

    protected boolean loadActiveSoloQueueGames(RiotApiHandler riotApiHandler) {
        try {
            this.activeSoloQueueGames = riotApiHandler.loadActiveSoloQueueGames(this.players, this);
            return true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            // LOG
            this.twitterBotLogger.log("", "URISyntaxException from riotApiHandler.loadActiveSoloQueueGames() "
                    + e.getStackTrace().toString());
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            // LOG
            this.twitterBotLogger.log("", "InterruptedException from riotApiHandler.loadActiveSoloQueueGames() "
                    + e.getStackTrace().toString());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            // LOG
            this.twitterBotLogger.log("",
                    "IOException from riotApiHandler.loadActiveSoloQueueGames() " + e.getStackTrace().toString());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            // LOG
            this.twitterBotLogger.log("",
                    "Exception from riotApiHandler.loadActiveSoloQueueGames() " + e.getStackTrace().toString());
            return false;
        }
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