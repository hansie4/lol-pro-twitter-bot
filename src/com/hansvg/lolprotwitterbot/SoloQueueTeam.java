/**
 * The Team class to represent a team in a solo queue match in League of Legends.
 * 
 * @author Hans Von Gruenigen
 * @version 1.0
 */
package com.hansvg.lolprotwitterbot;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

class SoloQueueTeam {

    private HashMap<Player, String[]> players;
    private boolean blueSide;

    /**
     * SoloQueueTeam Class Constructor.
     * 
     * @param participants JSONArray of participants in the solo queue game gotten
     *                     from the Riot Games API
     * @param blueSide     If the team is on the blue side
     * @param league       The league that the participants of the game are a part
     *                     of
     */
    protected SoloQueueTeam(JSONArray participants, boolean blueSide, League league) {
        this.players = new HashMap<>();
        this.blueSide = blueSide;

        if (blueSide) {
            for (int i = 0; i < (participants.length() / 2); i++) {
                JSONObject playerInfoJSON = participants.getJSONObject(i);

                String[] playerInfo = new String[] { playerInfoJSON.getString("summonerName"),
                        playerInfoJSON.getString("summonerId"), Long.toString(playerInfoJSON.getLong("teamId")),
                        Long.toString(playerInfoJSON.getLong("championId")) };

                Player player = league.getPlayerFromSummonerId(playerInfo[1]);

                if (player != null) {
                    players.put(player, playerInfo);
                }
            }
        } else {
            for (int i = (participants.length() / 2); i < participants.length(); i++) {
                JSONObject playerInfoJSON = participants.getJSONObject(i);

                String[] playerInfo = new String[] { playerInfoJSON.getString("summonerName"),
                        playerInfoJSON.getString("summonerId"), Long.toString(playerInfoJSON.getLong("teamId")),
                        Long.toString(playerInfoJSON.getLong("championId")) };

                Player player = league.getPlayerFromSummonerId(playerInfo[1]);

                if (player != null) {
                    players.put(player, playerInfo);
                }
            }
        }
    }

    /**
     * Getter for the player and player information hashmap.
     * 
     * @return HashMap of Players mapped to their info in a string array
     */
    protected HashMap<Player, String[]> getPlayers() {
        return this.players;
    }

    /**
     * Getter for the blueSide boolean.
     * 
     * @return True if the team is on the blue side and false otherwise
     */
    protected boolean isBlueSide() {
        return this.blueSide;
    }

}