import java.io.File;

import com.hansvg.lcstwitterbot.LCSTwitterBot;

public class Main {

    private static final String playerRosterFilePath = "data\\LoL_Roster_Summer_2020 - NA.csv";
    private static final String riotInfoFilePath = "data\\ApiInfo.json";
    private static final String logFilePath = "data\\LolProTwitterBot.log";

    public static void main(String[] args) {

        try {
            LCSTwitterBot lcsTwitterBot = new LCSTwitterBot(new File(playerRosterFilePath), new File(riotInfoFilePath),
                    new File(logFilePath));
            lcsTwitterBot.start();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}