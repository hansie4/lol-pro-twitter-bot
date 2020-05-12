package com.hansvg.lcstwitterbot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.JSONObject;

public class RiotApiRequester {

    private String ApiKey;
    private String region;
    private double requestsASecond;
    private HttpClient httpClient;

    public RiotApiRequester(String ApiKey, String region, double requestsASecond) {
        this.ApiKey = ApiKey;
        this.region = region;
        this.requestsASecond = requestsASecond;
        httpClient = HttpClient.newHttpClient();
    }

    public String getSummonerID(String summonerName) {
        try {
            URI requestURI = new URI("https://" + region + ".api.riotgames.com/lol/summoner/v4/summoners/by-name/"
                    + summonerNameNoSpaces(summonerName) + "?api_key=" + ApiKey);

            HttpRequest request = HttpRequest.newBuilder().uri(requestURI).build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            Thread.sleep((long) (requestsASecond * 1000));

            JSONObject responseJSON = new JSONObject(response.body());

            if (response.statusCode() == 200) {
                return responseJSON.get("id").toString();
            } else {
                if (response.statusCode() == 400) {
                    System.out.println("400: Bad Request from: getSummonerID(" + summonerName + ")");
                } else if (response.statusCode() == 401) {
                    System.out.println("401: Unautherized from: getSummonerID(" + summonerName + ")");
                } else if (response.statusCode() == 403) {
                    System.out.println("403: Forbidden from: getSummonerID(" + summonerName + ")");
                } else if (response.statusCode() == 404) {
                    System.out.println("404: Data Not Found from: getSummonerID(" + summonerName + ")");
                } else if (response.statusCode() == 405) {
                    System.out.println("405: Method Not Allowed from: getSummonerID(" + summonerName + ")");
                } else if (response.statusCode() == 415) {
                    System.out.println("415: Unsupported Media Type from: getSummonerID(" + summonerName + ")");
                } else if (response.statusCode() == 429) {
                    System.out.println("429: Rate Limit Exceeded from: getSummonerID(" + summonerName + ")");
                    return getSummonerID(summonerName);
                } else if (response.statusCode() == 500) {
                    System.out.println("500: Internal Server Error from: getSummonerID(" + summonerName + ")");
                } else if (response.statusCode() == 502) {
                    System.out.println("502: Bad Gateway from: getSummonerID(" + summonerName + ")");
                } else if (response.statusCode() == 503) {
                    System.out.println("503: Service Unavailable from: getSummonerID(" + summonerName + ")");
                } else if (response.statusCode() == 504) {
                    System.out.println("504: Gateway Timeout from: getSummonerID(" + summonerName + ")");
                }

                return null;
            }
        } catch (Exception exception) {
            System.out.println("Exception: " + exception.getLocalizedMessage());
            return null;
        }
    }

    public JSONObject getLiveGameInfo(String summonerID) {
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
                        System.out.println("400: Bad Request from: getLiveGameInfo(" + summonerID + ")");
                    } else if (response.statusCode() == 401) {
                        System.out.println("401: Unautherized from: getLiveGameInfo(" + summonerID + ")");
                    } else if (response.statusCode() == 403) {
                        System.out.println("403: Forbidden from: getLiveGameInfo(" + summonerID + ")");
                    } else if (response.statusCode() == 404) {
                        // System.out.println("404: Data Not Found from: getLiveGameInfo(" + summonerID
                        // + ")");
                    } else if (response.statusCode() == 405) {
                        System.out.println("405: Method Not Allowed from: getLiveGameInfo(" + summonerID + ")");
                    } else if (response.statusCode() == 415) {
                        System.out.println("415: Unsupported Media Type from: getLiveGameInfo(" + summonerID + ")");
                    } else if (response.statusCode() == 429) {
                        System.out.println("429: Rate Limit Exceeded from: getLiveGameInfo(" + summonerID + ")");
                        return getLiveGameInfo(summonerID);
                    } else if (response.statusCode() == 500) {
                        System.out.println("500: Internal Server Error from: getLiveGameInfo(" + summonerID + ")");
                    } else if (response.statusCode() == 502) {
                        System.out.println("502: Bad Gateway from: getLiveGameInfo(" + summonerID + ")");
                    } else if (response.statusCode() == 503) {
                        System.out.println("503: Service Unavailable from: getLiveGameInfo(" + summonerID + ")");
                    } else if (response.statusCode() == 504) {
                        System.out.println("504: Gateway Timeout from: getLiveGameInfo(" + summonerID + ")");
                    }

                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception exception) {
            System.out.println("Exception: " + exception.getLocalizedMessage());
            return null;
        }
    }

    private String summonerNameNoSpaces(String summonerName) {
        return summonerName.replaceAll(" ", "%20");
    }
}