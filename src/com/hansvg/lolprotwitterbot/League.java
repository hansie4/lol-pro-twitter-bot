/**
 * The League class holds all the player objects and loads all player information.
 * 
 * @author Hans Von Gruenigen
 * @version 1.0
 */
package com.hansvg.lolprotwitterbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

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
    protected League(Logger logger) {
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

                    String[] playerInfo = playerRosterScanner.nextLine().split(",", 6);

                    try {
                        String name = playerInfo[0];
                        String position = playerInfo[1];
                        String team = playerInfo[2];
                        String twitterHandle = playerInfo[3];
                        String twitchName = playerInfo[4];
                        String[] summonerNames = playerInfo[5].split(",");

                        Player readInPlayer = new Player(name, position, team, twitterHandle, twitchName,
                                summonerNames);

                        players.add(readInPlayer);
                    } catch (Exception e) {
                        // LOG
                        this.logger.warning("Error reading in player info from \"" + playerRosterFile.getAbsolutePath()
                                + "\". Player Name: " + playerInfo[0]);
                    }
                }
            }
            playerRosterScanner.close();

            return true;
        } catch (FileNotFoundException e) {
            // LOG
            this.logger.severe("FileNotFoundException");
            return false;
        } catch (Exception e) {
            // LOG
            this.logger.severe("Exception " + e.getLocalizedMessage());
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
            // LOG
            this.logger.info("Players successfully loaded");
            return true;
        } catch (URISyntaxException e) {
            // LOG
            this.logger.severe("URISyntaxException");
            return false;
        } catch (InterruptedException e) {
            // LOG
            this.logger.severe("InterruptedException");
            return false;
        } catch (IOException e) {
            // LOG
            this.logger.severe("IOException");
            return false;
        } catch (Exception e) {
            // LOG
            this.logger.severe("Exception " + e.getLocalizedMessage());
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
            // LOG
            this.logger.info("Active Solo Queue Games successfully loaded");
            return true;
        } catch (URISyntaxException e) {
            // LOG
            this.logger.severe("URISyntaxException");
            return false;
        } catch (InterruptedException e) {
            // LOG
            this.logger.severe("InterruptedException");
            return false;
        } catch (IOException e) {
            // LOG
            this.logger.severe("IOException");
            return false;
        } catch (Exception e) {
            // LOG
            this.logger.severe("Exception " + e.getLocalizedMessage());
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
     * Gets a the player that has the passed in twitch name
     * 
     * @param twitchName The username for their twitch account
     * @return The player with the twitch name or null if no player owns it
     */
    protected Player getPlayerFromTwitchName(String twitchName) {
        for (Player p : this.players) {
            if (p.getTwitchName() != null) {
                if (p.getTwitchName().equals(twitchName)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Gets the player that has the passed in twitch user id
     * 
     * @param twitchId The id of the player that is being searched for
     * @return The player with the twitch user id or null if no player owns it
     */
    protected Player getPlayerFromTwitchId(String twitchId) {
        for (Player p : this.players) {
            if (p.getTwitchUserId() != null) {
                if (p.getTwitchUserId().equals(twitchId)) {
                    return p;
                }
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

    /**
     * Prints the values for each player that is part of the league
     */
    protected void printLeaguePlayerInfo() {
        System.out.println("----------League's player information----------");
        for (int pIndex = 0; pIndex < this.players.size(); pIndex++) {
            Player p = this.players.get(pIndex);

            System.out.printf("#%3d | ", pIndex);
            System.out.printf("Name: %15s | ", p.getName());
            System.out.printf("Position: %7s | ", p.getPosition());
            System.out.printf("Team: %25s | ", p.getTeam());
            System.out.printf("Twitch Name: %15s | ", p.getTwitchName());
            if (p.getTwitchUserId() != null) {
                System.out.printf("Twitch Id: %15s | ", p.getTwitchUserId());
            } else {
                System.out.print("Twitch Id: -------- | ");
            }
            if (p.getTwitterHandle() != null) {
                System.out.printf("Twitter: %15s | \n", p.getTwitterHandle());
            } else {
                System.out.print("Twitter: -------- | \n");
            }

            System.out.println("\tLeague Accounts:    " + Arrays.toString(p.getSummonerNames()));
            System.out.println("\tLeague Account Ids: " + Arrays.toString(p.getSummonerIds()));

        }
        System.out.println("-----------------------------------------------");
    }

}