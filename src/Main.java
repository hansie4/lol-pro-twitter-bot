import java.io.File;

import com.hansvg.lcstwitterbot.LCSTwitterBot;

public class Main {

    private static final String playerRosterFilePath = "data\\LoL_Roster_Summer_2020 - NA.csv";
    private static final String riotInfoFilePath = "data\\RiotApiInfo.json";

    public static void main(String[] args) {

        try {
            LCSTwitterBot lcsTwitterBot = new LCSTwitterBot(new File(playerRosterFilePath), new File(riotInfoFilePath));
            lcsTwitterBot.start();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}