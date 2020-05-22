/**
 * The League class holds all the player objects and loads all player information.
 * 
 * @author Hans Von Gruenigen
 * @version 1.0
 */
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
    private Logger logger;

    /**
     * League Class Constructor.
     * 
     * @param logger The Logger object to log the processes withen this League
     *               object
     */
    League(Logger logger) {
        this.players = new ArrayList<>();
        this.activeSoloQueueGames = new ArrayList<>();
        this.logger = logger;
    }

    /**
     * Getter for all the players this league object has.
     * 
     * @return ArrayList of Player objects that the League object has
     */
    protected ArrayList<Player> getPlayers() {
        return this.players;
    }

    /**
     * Getter for all active SoloQueueGame objects that this League object has
     * 
     * @return ArrayList of SoloQueueGame objects that the League object has
     */
    protected ArrayList<SoloQueueGame> getActiveSoloQueueGames() {
        return this.activeSoloQueueGames;
    }

    /**
     * Reads in players from the passed in file, creates Player objects for each,
     * and adds each Player to the Player ArrayList the League has.
     * 
     * @param playerRosterFile The file containing the players to track and their
     *                         information
     * @return True if players loaded successfully and false if there was an error
     *         reading in the players
     */
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
            this.logger.log("", "Error when loading in players from \"" + playerRosterFile + "\" " + e.getStackTrace());
            return false;
        } catch (Exception e) {
            // LOG
            this.logger.log("", "Error when loading in players from \"" + playerRosterFile + "\" " + e.getStackTrace());
            return false;
        }

    }

    /**
     * Loads a summoner id for each summoner name each Player object in the league
     * has.
     * 
     * @param riotApiHandler The RiotApiHandler object to handle all the calls to
     *                       the Riot Games API
     * @return True if summoner ids were loaded successfully
     */
    protected boolean loadPlayerSummonerIds(RiotApiHandler riotApiHandler) {
        try {
            riotApiHandler.loadSummonerIds(this.players);
            return true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            // LOG
            this.logger.log("",
                    "URISyntaxException from riotApiHandler.loadSummonerIds() " + e.getStackTrace().toString());
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            // LOG
            this.logger.log("",
                    "InterruptedException from riotApiHandler.loadSummonerIds() " + e.getStackTrace().toString());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            // LOG
            this.logger.log("", "IOException from riotApiHandler.loadSummonerIds() " + e.getStackTrace().toString());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            // LOG
            this.logger.log("", "Exception from riotApiHandler.loadSummonerIds() " + e.getStackTrace().toString());
            return false;
        }
    }

    /**
     * Loads active SoloQueueGames for each summoner id for Players in the League.
     * 
     * @param riotApiHandler The RiotApiHandler object to handle all the calls to
     *                       the Riot Games API
     * @return True if active SoloQueueGames were loaded successfully
     */
    protected boolean loadActiveSoloQueueGames(RiotApiHandler riotApiHandler) {
        try {
            this.activeSoloQueueGames = riotApiHandler.loadActiveSoloQueueGames(this.players, this);
            return true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            // LOG
            this.logger.log("", "URISyntaxException from riotApiHandler.loadActiveSoloQueueGames() "
                    + e.getStackTrace().toString());
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            // LOG
            this.logger.log("", "InterruptedException from riotApiHandler.loadActiveSoloQueueGames() "
                    + e.getStackTrace().toString());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            // LOG
            this.logger.log("",
                    "IOException from riotApiHandler.loadActiveSoloQueueGames() " + e.getStackTrace().toString());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            // LOG
            this.logger.log("",
                    "Exception from riotApiHandler.loadActiveSoloQueueGames() " + e.getStackTrace().toString());
            return false;
        }
    }

    /**
     * Gets a Player object that owns the passed in summoner name.
     * 
     * @param summonerName The summoner name that the Player object you want owns
     * @return The Player object that owns the passed in summoner name or null if no
     *         Player in the league owns the summoner name
     */
    protected Player getPlayerFromSummonerName(String summonerName) {
        for (Player p : this.players) {
            if (p.ownsSummonerName(summonerName)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Gets a Player object that owns the passed in summoner id.
     * 
     * @param summonerId The summoner id that the Player object you want owns
     * @return The Player object that owns the passed in summoner id or null if no
     *         Player in the league owns the summoner id
     */
    protected Player getPlayerFromSummonerId(String summonerId) {
        for (Player p : this.players) {
            if (p.ownsSummonerId(summonerId)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Goes through each Player in the players ArrayList in the League and adds
     * their summoner ids to an ArrayList that the function then returns.
     * 
     * @return ArrayList of all the summoner ids of the Players in this League
     */
    protected ArrayList<String> getAllSummonerIds() {
        ArrayList<String> ids = new ArrayList<>();
        for (Player player : this.players) {
            for (String id : player.getSummonerIds()) {
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

}