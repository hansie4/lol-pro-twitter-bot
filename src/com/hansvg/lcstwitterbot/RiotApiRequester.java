package com.hansvg.lcstwitterbot;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.JSONObject;

class RiotApiRequester {

    private String ApiKey;
    private String region;
    private double requestsASecond;
    private HttpClient httpClient;
    private TwitterBotLogger twitterBotLogger;

    protected RiotApiRequester(String ApiKey, String region, double requestsASecond,
            TwitterBotLogger twitterBotLogger) {
        this.ApiKey = ApiKey;
        this.region = region;
        this.requestsASecond = requestsASecond;
        httpClient = HttpClient.newHttpClient();
        this.twitterBotLogger = twitterBotLogger;
    }

    protected boolean isWorking() {
        try {

            URI requestURI = new URI("https://" + region + ".api.riotgames.com/lol/summoner/v4/summoners/by-name/"
                    + summonerNameNoSpaces("Hansie") + "?api_key=" + ApiKey);

            HttpRequest request = HttpRequest.newBuilder().uri(requestURI).build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            Thread.sleep((long) (requestsASecond * 1000));

            if (response.statusCode() == 200) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    protected JSONObject getSummoner(String summonerName) {
        try {
            URI requestURI = new URI("https://" + region + ".api.riotgames.com/lol/summoner/v4/summoners/by-name/"
                    + summonerNameNoSpaces(summonerName) + "?api_key=" + ApiKey);

            HttpRequest request = HttpRequest.newBuilder().uri(requestURI).build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            Thread.sleep((long) (requestsASecond * 1000));

            JSONObject responseJSON = new JSONObject(response.body());

            if (response.statusCode() == 200) {
                return responseJSON;
            } else {
                if (response.statusCode() == 400) {
                    // LOG
                    this.twitterBotLogger.log("400: BAD REQUEST",
                            "From Riot Games API for requesting Summoner Name: " + summonerName);
                } else if (response.statusCode() == 401) {
                    // LOG
                    this.twitterBotLogger.log("401: UNAUTHERIZED",
                            "From Riot Games API for requesting Summoner Name: " + summonerName);
                } else if (response.statusCode() == 403) {
                    // LOG
                    this.twitterBotLogger.log("403: FORBIDDEN",
                            "From Riot Games API for requesting Summoner Name: " + summonerName);
                } else if (response.statusCode() == 404) {
                    // LOG
                    this.twitterBotLogger.log("404: DATA NOT FOUND",
                            "From Riot Games API for requesting Summoner Name: " + summonerName);
                } else if (response.statusCode() == 405) {
                    // LOG
                    this.twitterBotLogger.log("405: METHOD NOT ALLOWED",
                            "From Riot Games API for requesting Summoner Name: " + summonerName);
                } else if (response.statusCode() == 415) {
                    // LOG
                    this.twitterBotLogger.log("415: UNSUPPORTED MEDIA TYPE",
                            "From Riot Games API for requesting Summoner Name: " + summonerName);
                } else if (response.statusCode() == 429) {
                    // LOG
                    this.twitterBotLogger.log("429: RATE LIMIT EXCEEDED",
                            "From Riot Games API for requesting Summoner Name: " + summonerName);
                } else if (response.statusCode() == 500) {
                    // LOG
                    this.twitterBotLogger.log("500: INTERNAL SERVER ERROR",
                            "From Riot Games API for requesting Summoner Name: " + summonerName);
                } else if (response.statusCode() == 503) {
                    // LOG
                    this.twitterBotLogger.log("503: SERVICE UNAVAILABLE",
                            "From Riot Games API for requesting Summoner Name: " + summonerName);
                }
                return null;
            }
        } catch (

        URISyntaxException exception) {
            return null;
        } catch (IOException exception) {
            return null;
        } catch (InterruptedException exception) {
            return null;
        } catch (Exception exception) {
            return null;
        }
    }

    protected JSONObject getLiveGameInfo(String summonerID) {
        try {
            if (summonerID != null) {
                URI requestURI = new URI(
                        "https://" + region + ".api.riotgames.com/lol/spectator/v4/active-games/by-summoner/"
                                + summonerID + "?api_key=" + ApiKey);

                HttpRequest request = HttpRequest.newBuilder().uri(requestURI).build();

                HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

                Thread.sleep((long) (requestsASecond * 1000));

                JSONObject responseJSON = new JSONObject(response.body());

                if (response.statusCode() == 200) {
                    return responseJSON;
                } else {
                    if (response.statusCode() == 400) {
                        // LOG
                        this.twitterBotLogger.log("400: BAD REQUEST",
                                "From Riot Games API for requesting live game for: " + summonerID);
                    } else if (response.statusCode() == 401) {
                        // LOG
                        this.twitterBotLogger.log("401: UNAUTHERIZED",
                                "From Riot Games API for requesting live game for: " + summonerID);
                    } else if (response.statusCode() == 403) {
                        // LOG
                        this.twitterBotLogger.log("403: FORBIDDEN",
                                "From Riot Games API for requesting live game for: " + summonerID);
                    } else if (response.statusCode() == 404) {
                        // LOG
                        // this.twitterBotLogger.log("404: DATA NOT FOUND", "From Riot Games API for
                        // requesting live game for: " + summonerID);
                    } else if (response.statusCode() == 405) {
                        // LOG
                        this.twitterBotLogger.log("405: METHOD NOT ALLOWED",
                                "From Riot Games API for requesting live game for: " + summonerID);
                    } else if (response.statusCode() == 415) {
                        // LOG
                        this.twitterBotLogger.log("415: UNSUPPORTED MEDIA TYPE",
                                "From Riot Games API for requesting live game for: " + summonerID);
                    } else if (response.statusCode() == 429) {
                        // LOG
                        this.twitterBotLogger.log("429: RATE LIMIT EXCEEDED",
                                "From Riot Games API for requesting live game for: " + summonerID);
                    } else if (response.statusCode() == 500) {
                        // LOG
                        this.twitterBotLogger.log("500: INTERNAL SERVER ERROR",
                                "From Riot Games API for requesting live game for: " + summonerID);
                    } else if (response.statusCode() == 503) {
                        // LOG
                        this.twitterBotLogger.log("503: SERVICE UNAVAILABLE",
                                "From Riot Games API for requesting live game for: " + summonerID);
                    }
                    return null;
                }
            } else {
                return null;
            }
        } catch (URISyntaxException exception) {
            return null;
        } catch (IOException exception) {
            return null;
        } catch (InterruptedException exception) {
            return null;
        } catch (Exception exception) {
            return null;
        }
    }

    private String summonerNameNoSpaces(String summonerName) {
        return summonerName.replaceAll(" ", "%20");
    }
}