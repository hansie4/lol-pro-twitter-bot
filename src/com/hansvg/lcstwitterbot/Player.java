package com.hansvg.lcstwitterbot;

public class Player {

    public enum Position {
        TOP, JUNGLE, MID, ADC, SUPPORT;
    }

    private String playerName;
    private Position position;
    private String team;
    private String twitterHandle;
    private String twitchUsername;
    private String[] summonerNames;
    private String[] summonerIDs;

    public Player(String playerName, String position, String team, String twitterHandle, String twitchUsername,
            String[] accounts) {

        this.playerName = playerName;
        this.position = stringToPosition(position);
        this.team = team;
        this.twitterHandle = twitterHandle;
        this.twitchUsername = twitchUsername;
        this.summonerNames = accounts;
        summonerIDs = new String[accounts.length];
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public Position getPosition() {
        return this.position;
    }

    public String getTeam() {
        return this.team;
    }

    public String getTwitterHandle() {
        return this.twitterHandle;
    }

    public String getTwitchUsername() {
        return this.twitchUsername;
    }

    public String[] getSummonerNames() {
        return this.summonerNames;
    }

    public String[] getSummonerIDs() {
        return this.summonerIDs;
    }

    public void setSummonerIDs(String[] summonerIDs) {
        this.summonerIDs = summonerIDs;
    }

    private Position stringToPosition(String position) {
        if (position.equals("TOP")) {
            return Position.TOP;
        } else if (position.equals("JUNGLE")) {
            return Position.JUNGLE;
        } else if (position.equals("MID")) {
            return Position.MID;
        } else if (position.equals("ADC")) {
            return Position.ADC;
        } else if (position.equals("SUPPORT")) {
            return Position.SUPPORT;
        } else {
            System.out.println("Invalid position in player roster file found. ");
            return null;
        }
    }

}