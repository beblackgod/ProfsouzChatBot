package sample.server;

import com.google.inject.Inject;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;


/**
 * Created by pc on 07.06.2017.
 */
public class Server {
    private Bot bot;
    @Inject
    public Server(){
        startServer();
    }
    private void startServer(){
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        bot = new Bot();
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
    public Bot getBot(){
        return bot;
    }
}
