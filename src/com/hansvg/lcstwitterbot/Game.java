package com.hansvg.lcstwitterbot;

import java.util.ArrayList;

import org.json.JSONObject;

public class Game {

    private String gameID;
    private String gameType;
    private int mapID;
    private long gameStartTime;
    private long gameLength;
    private ArrayList<String> participantSummonerIDs;

    public Game(JSONObject gameJSON) {

        this.gameID = gameJSON.get("gameId").toString();
        this.gameType = gameJSON.get("gameType").toString();
        this.mapID = gameJSON.getInt("mapId");
        this.gameStartTime = gameJSON.getLong("gameStartTime");
        this.gameLength = gameJSON.getLong("gameLength");
        participantSummonerIDs = new ArrayList<>();

    }

    public String getGameID() {
        return this.gameID;
    }

    public String getGameType() {
        return this.gameType;
    }

    public int getMapID() {
        return this.mapID;
    }

    public long getGameStartTime() {
        return this.gameStartTime;
    }

    public long getGameLength() {
        return this.gameLength;
    }

    public ArrayList<String> getParticipantSummonerIDs() {
        return this.participantSummonerIDs;
    }

}