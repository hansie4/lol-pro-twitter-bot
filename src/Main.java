import com.hansvg.lolprotwitterbot.LoLProTwitterBot;

public class Main {

    public static void main(String[] args) {

        try {
            LoLProTwitterBot lolProTwitterBot = new LoLProTwitterBot(args[0]);
            lolProTwitterBot.run(Integer.parseInt(args[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}