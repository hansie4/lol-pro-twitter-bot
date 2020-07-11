import com.hansvg.lolprotwitterbot.LoLProTwitterBot;

public class Main {

    public static void main(String[] args) {

        try {
            LoLProTwitterBot lolProTwitterBot = new LoLProTwitterBot("data\\config.properties");
            lolProTwitterBot.run(3600);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}