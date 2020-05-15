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
    private ArrayList<String[]> participants;

    protected SoloQueueGame(League league, JSONObject gameJSON) {
        this.league = league;
        this.gameId = gameJSON.getLong("gameId");
        this.gameType = gameJSON.getString("gameType");
        this.gameStartTime = gameJSON.getLong("gameStartTime");
        this.mapId = gameJSON.getLong("mapId");
        this.gameLength = gameJSON.getLong("gameLength");
        this.platformId = gameJSON.getString("platformId");
        this.gameMode = gameJSON.getString("gameMode");
        this.participants = new ArrayList<>();

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

    protected long getGameId() {
        return this.gameId;
    }

    protected String getGameType() {
        return this.gameType;
    }

    protected long getGameStartTime() {
        return this.gameStartTime;
    }

    protected long getMapId() {
        return this.mapId;
    }

    protected long getGameLength() {
        return this.gameLength;
    }

    protected String getPlatformId() {
        return this.platformId;
    }

    protected String getGameMode() {
        return this.gameMode;
    }

    protected ArrayList<String[]> getParticipants() {
        return this.participants;
    }

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

    protected void printGameInfo() {
        System.out.println("--------Game Info for Game ID: " + this.gameId + "--------");
        System.out.println("Map ID: " + this.mapId);
        System.out.println("Game Type: " + this.gameType);
        System.out.println("Game Mode: " + this.gameMode);
        System.out.println("Platform ID: " + this.platformId);
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