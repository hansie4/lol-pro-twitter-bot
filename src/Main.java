import java.io.File;

import com.hansvg.lolprotwitterbot.LoLProTwitterBot;

public class Main {

    private static final String playerRosterFilePath = "data\\LoL_Pro_Roster - NA_Summer_2020.csv";
    private static final String riotInfoFilePath = "data\\ApiInfo.json";
    private static final String logFilePath = "data\\LolProTwitterBot.log";

    public static void main(String[] args) {

        try {
            LoLProTwitterBot lolProTwitterBot = new LoLProTwitterBot(new File(playerRosterFilePath),
                    new File(riotInfoFilePath), new File(logFilePath));
            lolProTwitterBot.run(420);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}