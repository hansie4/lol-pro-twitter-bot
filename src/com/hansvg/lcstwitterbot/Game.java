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
    private ArrayList<String[]> participantInfo;
    private int team1Score;
    private int team2Score;
    private int gameScore;
    private boolean isLcsGame;

    public Game(JSONObject gameJSON, boolean isLcsGame) {

        this.gameID = gameJSON.get("gameId").toString();
        this.gameType = gameJSON.get("gameType").toString();
        this.mapID = gameJSON.getInt("mapId");
        this.gameStartTime = gameJSON.getLong("gameStartTime");
        this.gameLength = gameJSON.getLong("gameLength");
        participantInfo = new ArrayList<>();

        JSONArray participantArray = gameJSON.getJSONArray("participants");

        for (int i = 0; i < participantArray.length(); i++) {
            JSONObject partcipant = participantArray.getJSONObject(i);
            String[] values = new String[] { partcipant.getString("summonerId"), partcipant.getString("summonerName"),
                    Long.toString(partcipant.getLong("teamId")), Long.toString(partcipant.getLong("championId")) };

            participantInfo.add(values);
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

    public ArrayList<String[]> getParticipantInfo() {
        return this.participantInfo;
    }

    public boolean getIsLcsGame() {
        return this.isLcsGame;
    }

    public int getGameScore() {
        return this.gameScore;
    }

    public void setGameScore(int gameScore) {
        this.gameScore = gameScore;
    }

    public int getTeam1Score() {
        return this.team1Score;
    }

    public void setTeam1Score(int team1Score) {
        this.team1Score = team1Score;
    }

    public int getTeam2Score() {
        return this.team2Score;
    }

    public void setTeam2Score(int team2Score) {
        this.team2Score = team2Score;
    }

    public ArrayList<ArrayList<String[]>> getTeams() {

        ArrayList<ArrayList<String[]>> teamList = new ArrayList<>();
        ArrayList<String[]> team1 = new ArrayList<>();
        ArrayList<String[]> team2 = new ArrayList<>();

        long team1Id = Long.valueOf(participantInfo.get(0)[2]);

        for (int i = 0; i < this.participantInfo.size(); i++) {
            if (Long.valueOf(participantInfo.get(i)[2]) == team1Id) {
                team1.add(this.participantInfo.get(i));
            } else {
                team2.add(this.participantInfo.get(i));
            }
        }

        teamList.add(team1);
        teamList.add(team2);

        return teamList;
    }

    public void printGameInfo() {

        System.out.println("----Game Info for GameID: " + this.gameID + "----");
        System.out.println("MapID: " + this.mapID);
        System.out.println("Game Type: " + this.gameType);
        System.out.println("Game Start Time: " + this.gameStartTime);
        System.out.println("Game Length: " + this.gameLength);
        System.out.println("Game Score: " + this.gameScore);
        System.out.println("-------------------------------------------------");
        System.out.println("         Team 1         |         Team 2         ");

        ArrayList<ArrayList<String[]>> teams = getTeams();
        ArrayList<String[]> team1 = teams.get(0);
        ArrayList<String[]> team2 = teams.get(1);

        for (int i = 0; i < team1.size(); i++) {
            System.out.printf("%24s|", team1.get(i)[1]);
            System.out.printf("%24s\n", team2.get(i)[1]);
        }

        System.out.println("-------------------------------------------------");
    }

}