package com.hansvg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.hansvg.lcstwitterbot.RiotApiRequester;

import org.json.JSONObject;

public class Main {

    private static final String riotInfoFilePath = "data\\RiotApiInfo.json";

    public static void main(String[] args) {

        try {

            JSONObject riotJSON = new JSONObject(readInRiotFile(riotInfoFilePath));

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getStackTrace());
        }

    }

    private static String readInRiotFile(String riotFileName) throws FileNotFoundException {

        File riotFile = new File(riotFileName);
        Scanner fileScanner = new Scanner(riotFile);
        String fileContents = "";

        while (fileScanner.hasNext()) {
            fileContents += fileScanner.next();
        }

        fileScanner.close();
        return fileContents;
    }

    private static String getRiotApiKey(JSONObject riotJSON) {
        return riotJSON.get("apiKey").toString();
    }

    private static String getRiotRegion(JSONObject riotJSON) {
        return riotJSON.get("region").toString();
    }

    private static int getRiotRequestsPerSecond(JSONObject riotJSON) {
        return riotJSON.getInt("requestsPerSecond");
    }

}