package com.hansvg.lcstwitterbot;

class Player {

    protected enum Position {
        TOP, JUNGLE, MID, ADC, SUPPORT;
    }

    private String name;
    private int watchValue;
    private Position position;
    private String team;
    private String twitterHandle;
    private String twitchUsername;
    private String[] summonerNames;
    private String[] summonerIds;

    protected Player(String name, String watchValue, String position, String team, String twitterHandle,
            String twitchUsername, String[] summonerNames) {

        this.name = name;
        this.watchValue = Integer.parseInt(watchValue);
        this.position = getPositionFromString(position);
        this.team = team;
        this.twitterHandle = twitterHandle;
        this.twitchUsername = twitchUsername;
        this.summonerNames = summonerNames;

        summonerIds = new String[summonerNames.length];
    }

    protected String getName() {
        return this.name;
    }

    protected int getWatchValue() {
        return this.watchValue;
    }

    protected Position getPosition() {
        return this.position;
    }

    protected String getTeam() {
        return this.team;
    }

    protected String getTwitterHandle() {
        return this.twitterHandle;
    }

    protected String getTwitchUsername() {
        return this.twitchUsername;
    }

    protected String[] getSummonerNames() {
        return this.summonerNames;
    }

    protected String[] getSummonerIds() {
        return this.summonerIds;
    }

    protected boolean ownsSummonerName(String summonerName) {
        for (int i = 0; i < this.summonerNames.length; i++) {
            if (summonerName.equals(this.summonerNames[i])) {
                return true;
            }
        }
        return false;
    }

    protected boolean ownsSummonerId(String summonerId) {
        for (int i = 0; i < this.summonerIds.length; i++) {
            if (summonerId.equals(this.summonerIds[i])) {
                return true;
            }
        }
        return false;
    }

    private Position getPositionFromString(String position) {
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