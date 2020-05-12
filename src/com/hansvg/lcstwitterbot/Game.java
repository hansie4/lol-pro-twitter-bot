package com.hansvg.lcstwitterbot;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Game {

    private String gameID;
    private String gameType;
    private int mapID;
    private long gameStartTime;
    private long gameLength;
    private ArrayList<JSONObject> participants;
    private boolean isLcsGame;

    public Game(JSONObject gameJSON, boolean isLcsGame) {

        this.gameID = gameJSON.get("gameId").toString();
        this.gameType = gameJSON.get("gameType").toString();
        this.mapID = gameJSON.getInt("mapId");
        this.gameStartTime = gameJSON.getLong("gameStartTime");
        this.gameLength = gameJSON.getLong("gameLength");
        participants = new ArrayList<>();

        JSONArray participantArray = gameJSON.getJSONArray("participants");

        for (int i = 0; i < participantArray.length(); i++) {
            participants.add(participantArray.getJSONObject(i));
        }

        this.isLcsGame = isLcsGame;
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

    public ArrayList<JSONObject> getParticipants() {
        return this.participants;
    }

    public boolean getIsLcsGame() {
        return this.isLcsGame;
    }

}