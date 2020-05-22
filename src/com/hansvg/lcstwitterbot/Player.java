/**
 * The Player class to represent each of the professional League of Legends players to track.
 * 
 * @author Hans Von Gruenigen
 * @version 1.0
 */
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

    /**
     * Player Class Constructor.
     * 
     * @param name           Player name
     * @param watchValue     Player watch value
     * @param position       Player position
     * @param team           Player team
     * @param twitterHandle  Player twitter handle
     * @param twitchUsername Player twitch channel username
     * @param summonerNames  Array of Player summoner names
     */
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

    /**
     * Getter for the Player's name.
     * 
     * @return The Player's name
     */
    protected String getName() {
        return this.name;
    }

    /**
     * Getter for the Player's watch value.
     * 
     * @return The Player's watch value
     */
    protected int getWatchValue() {
        return this.watchValue;
    }

    /**
     * Getter for the Player's position.
     * 
     * @return The Player's position
     */
    protected Position getPosition() {
        return this.position;
    }

    /**
     * Getter for the Player's team.
     * 
     * @return The Player's team
     */
    protected String getTeam() {
        return this.team;
    }

    /**
     * Getter for the Player's twitter handle.
     * 
     * @return The Player's twitter handle
     */
    protected String getTwitterHandle() {
        return this.twitterHandle;
    }

    /**
     * Getter for the Player's twitch username.
     * 
     * @return The Player's twitch username
     */
    protected String getTwitchUsername() {
        return this.twitchUsername;
    }

    /**
     * Getter for the Player's summoner names.
     * 
     * @return Array of Player's summoner names
     */
    protected String[] getSummonerNames() {
        return this.summonerNames;
    }

    /**
     * Getter for the Player's summoner ids.
     * 
     * @return Array of the Player's summoner ids.
     */
    protected String[] getSummonerIds() {
        return this.summonerIds;
    }

    /**
     * Checks if the Player object owns the passed in summoner name.
     * 
     * @param summonerName The summoner name you want to check if the Player owns
     * @return True if the Player owns the summoner name and false otherwise
     */
    protected boolean ownsSummonerName(String summonerName) {
        for (int i = 0; i < this.summonerNames.length; i++) {
            if (summonerName.equals(this.summonerNames[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the Player object owns the passed in summoner id.
     * 
     * @param summonerId The summoner id you want to check if the Player owns
     * @return True if the Player owns the summoner id and false otherwise
     */
    protected boolean ownsSummonerId(String summonerId) {
        for (int i = 0; i < this.summonerIds.length; i++) {
            if (summonerId.equals(this.summonerIds[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the corresponding position enumeration to the string passed in.
     * 
     * @param position String representation of the position
     * @return Position enumeration corresponding to the passed in string. If passed
     *         in string does not correspond to Position enumeration then the
     *         SUPPORT enumeration is returned as a default
     */
    private Position getPositionFromString(String position) {
        if (position.equals("TOP")) {
            return Position.TOP;
        } else if (position.equals("JUNGLE")) {
            return Position.JUNGLE;
        } else if (position.equals("MID")) {
            return Position.MID;
        } else if (position.equals("ADC")) {
            return Position.ADC;
        } else {
            return Position.SUPPORT;
        }
    }

}