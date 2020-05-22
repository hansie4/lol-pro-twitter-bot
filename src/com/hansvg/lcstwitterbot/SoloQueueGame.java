/**
 * The SoloQueueGame Class represents a game of solo queue in the game League of Legends.
 * 
 * @author Hans Von Gruenigen
 * @version 1.0
 */
package com.hansvg.lcstwitterbot;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

class SoloQueueGame {

    private League league;
    private long gameId;
    private String gameType;
    private long gameStartTime;
    private long mapId;
    private long gameLength;
    private String platformId;
    private String gameMode;
    private Long gameQueueConfigId;
    private SoloQueueTeam team1;
    private SoloQueueTeam team2;

    /**
     * SoloQueueGame Class Constructor.
     * 
     * @param gameJSON JSONObject representing the game gotten from the Riot Games
     *                 API
     * @param league   The League that this game belongs to
     */
    protected SoloQueueGame(JSONObject gameJSON, League league) {
        this.league = league;
        this.gameId = gameJSON.getLong("gameId");
        this.gameType = gameJSON.getString("gameType");
        this.gameStartTime = gameJSON.getLong("gameStartTime");
        this.mapId = gameJSON.getLong("mapId");
        this.gameLength = gameJSON.getLong("gameLength");
        this.platformId = gameJSON.getString("platformId");
        this.gameMode = gameJSON.getString("gameMode");

        if (!this.gameType.equals("CUSTOM_GAME")) {
            this.gameQueueConfigId = gameJSON.getLong("gameQueueConfigId");
        }

        JSONArray gameParticipants = gameJSON.getJSONArray("participants");

        this.team1 = new SoloQueueTeam(gameParticipants, true, this.league);
        this.team2 = new SoloQueueTeam(gameParticipants, false, this.league);
    }

    /**
     * Getter for the Game's id.
     * 
     * @return The game id
     */
    protected long getGameId() {
        return this.gameId;
    }

    /**
     * Getter for the Game's type.
     * 
     * @return The game type
     */
    protected String getGameType() {
        return this.gameType;
    }

    /**
     * Getter for the game start time.
     * 
     * @return The game start time
     */
    protected long getGameStartTime() {
        return this.gameStartTime;
    }

    /**
     * Getter for the map id of the game.
     * 
     * @return The map id
     */
    protected long getMapId() {
        return this.mapId;
    }

    /**
     * Getter for the game length.
     * 
     * @return The game length
     */
    protected long getGameLength() {
        return this.gameLength;
    }

    /**
     * Getter for the game's platform id.
     * 
     * @return The platform id
     */
    protected String getPlatformId() {
        return this.platformId;
    }

    /**
     * Getter for the game's mode.
     * 
     * @return The game mode
     */
    protected String getGameMode() {
        return this.gameMode;
    }

    /**
     * Getter for the game's queue config id.
     * 
     * @return The game queue config id
     */
    protected long getGameQueueConfigId() {
        return this.gameQueueConfigId;
    }

    /**
     * Getter for SoloQueueTeam team1.
     * 
     * @return SoloQueueTeam on blue side
     */
    protected SoloQueueTeam getTeam1() {
        return this.team1;
    }

    /**
     * Getter for SoloQueueTeam team2.
     * 
     * @return SoloQueueTeam on red side
     */
    protected SoloQueueTeam getTeam2() {
        return this.team2;
    }

    /**
     * Function for getting all the ids of the pro players in this active
     * SoloQueueGame.
     * 
     * @return ArrayList containing all the player ids or pro players in this active
     *         SoloQueueGame
     */
    protected ArrayList<String> getAllPlayersIds() {
        ArrayList<String> playerIds = new ArrayList<>();

        for (String[] playerInfo : this.team1.getPlayers().values()) {
            playerIds.add(playerInfo[1]);
        }
        for (String[] playerInfo : this.team2.getPlayers().values()) {
            playerIds.add(playerInfo[1]);
        }

        return playerIds;
    }

    /**
     * Utility function to print out the values of the game.
     */
    protected void printGameInfo() {
        System.out.println("--------Game Info for Game ID: " + this.gameId + "--------");
        System.out.println("Map ID: " + this.mapId);
        System.out.println("Game Type: " + this.gameType);
        System.out.println("Game Mode: " + this.gameMode);
        System.out.println("Platform ID: " + this.platformId);
        System.out.println("Game Queue Config ID: " + this.gameQueueConfigId);
        System.out.println("Game Start Time: " + this.gameStartTime);
        System.out.println("Game Length: " + this.gameLength);
        System.out.println("-------------------------------------------------");

        System.out.println("Blue Side:");
        for (Player player : this.team1.getPlayers().keySet()) {
            System.out.println("\t" + player.getName());
        }

        System.out.println("Red Side:");
        for (Player player : this.team2.getPlayers().keySet()) {
            System.out.println("\t" + player.getName());
        }

        System.out.println("-------------------------------------------------");
    }

}