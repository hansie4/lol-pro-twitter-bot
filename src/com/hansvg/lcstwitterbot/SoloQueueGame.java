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
    private ArrayList<String[]> participants;

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
        this.participants = new ArrayList<>();

        if (!this.gameType.equals("CUSTOM_GAME")) {
            this.gameQueueConfigId = gameJSON.getLong("gameQueueConfigId");
        }

        JSONArray gameParticipants = gameJSON.getJSONArray("participants");

        for (int i = 0; i < gameParticipants.length(); i++) {
            JSONObject participantObjJSON = gameParticipants.getJSONObject(i);

            String[] playerInfo = new String[] { participantObjJSON.getString("summonerName"),
                    participantObjJSON.getString("summonerId"), Long.toString(participantObjJSON.getLong("teamId")),
                    Long.toString(participantObjJSON.getLong("championId")),
                    Boolean.toString(participantObjJSON.getBoolean("bot")) };

            participants.add(playerInfo);
        }
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
     * Getter for the game's participants
     * 
     * @return The games participants represented by and array of strings in an
     *         ArrayList
     */
    protected ArrayList<String[]> getParticipants() {
        return this.participants;
    }

    /**
     * Computes the two teams of the game and puts them in an ArrayList containing
     * ArrayList of Players represented by arrays of strings.
     * 
     * @return The ArrayList containing the game's teams
     */
    protected ArrayList<ArrayList<String[]>> getTeams() {
        ArrayList<ArrayList<String[]>> teams = new ArrayList<>();
        ArrayList<String[]> team1 = new ArrayList<>();
        ArrayList<String[]> team2 = new ArrayList<>();

        for (String[] participant : participants) {
            if (team1.isEmpty()) {
                team1.add(participant);
            } else if (participant[2].equals(team1.get(0)[2])) {
                team1.add(participant);
            } else {
                team2.add(participant);
            }
        }

        teams.add(team1);
        teams.add(team2);

        return teams;
    }

    /**
     * Gets all the watch values of the players of the game and returns the sum.
     * 
     * @return The total watch value of the game
     */
    protected int getGameWatchValue() {
        int watchValue = 0;
        for (String[] participant : this.participants) {
            Player player = this.league.getPlayerFromSummonerName(participant[0]);

            if (player != null) {
                watchValue += player.getWatchValue();
            }
        }
        return watchValue;
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

        ArrayList<ArrayList<String[]>> teams = this.getTeams();
        ArrayList<String[]> team1 = teams.get(0);
        ArrayList<String[]> team2 = teams.get(1);

        if (team1.size() == team2.size()) {
            System.out.println("         Team 1         |         Team 2         ");
            for (int i = 0; i < team1.size(); i++) {
                System.out.printf("%24s|", team1.get(i)[0]);
                System.out.printf("%24s\n", team2.get(i)[0]);
            }
        } else {
            System.out.println("Teams of unequal size");
            System.out.println("Team 1: " + team1);
            System.out.println("Team 2: " + team2);
        }

        System.out.println("-------------------------------------------------");
        System.out.println("Game Watch Value: " + this.getGameWatchValue());
        System.out.println("-------------------------------------------------");
    }

}