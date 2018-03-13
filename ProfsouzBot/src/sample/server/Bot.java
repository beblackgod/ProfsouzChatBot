package sample.server;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created by pc on 07.06.2017.
 */
public class Bot extends TelegramLongPollingBot {
    private SQLliteDateBase dateBase;

    @Override
    public void onUpdateReceived(Update update) {

        String msg = update.getMessage().getText();
        SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
        String botAnswer = "-";
        String url = "";
        addQueries(update);
        if (msg.startsWith("?")) {
            botAnswer = help();
        } else if (msg.startsWith("/start") || msg.startsWith("/Start") || msg.startsWith("start")) {
            addContact(update);
            botAnswer = help();
        } else if (msg.startsWith("новости") || msg.startsWith("новости") || msg.startsWith("Новости")|| msg.startsWith("⚽️\uD83E\uDD47\uD83C\uDFAD")) {
            botAnswer = showNews();
            url = showNewsImage();
        } else if (msg.startsWith("идея") || msg.startsWith("Идея") || msg.startsWith("\uD83D\uDCA1")) {
            botAnswer = getIdea(update);
        } else if (msg.startsWith("связь") || msg.startsWith("Связь") || msg.startsWith("\uD83D\uDC4D")) {
            botAnswer = getFeedback(update);
        } else if ((msg.startsWith("справка") || msg.startsWith("Справка") || msg.startsWith("\uD83D\uDCCC"))) {
            botAnswer = getInfo();
        } else if(msg.startsWith("спасибо") || msg.startsWith("Спасибо")){
            botAnswer = "Был рад Вам помочь!";
        }else {
            botAnswer = "\uD83D\uDC40Бот-помощник не может распознать команду, но очень хочет Вам помочь.\nДля справки наберите \"?\"";
        }
        sendMessage.setText(botAnswer);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        if(msg.startsWith("новости") ||msg.startsWith("Новости") ) {
            if(url==null){

            }else {
                try {
                    SendPhoto sendPhoto = new SendPhoto().setChatId(update.getMessage().getChatId());
                    sendPhoto.setNewPhoto(new File(url));
                    sendPhoto(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return null;
    }

    @Override
    public String getBotToken() {

        return "*************************";

    }

    //рассылка всем контактам
    public void broadcastMessage(String text, String filepath) {
        dateBase = new SQLliteDateBase();
        try {
            dateBase.connect();
            ArrayList<Long> contactList = dateBase.getContactList();
            for (Long o : contactList) {
                SendMessage sendMessage = new SendMessage().setChatId(o);
                sendMessage.setText(text);
                SendPhoto sendPhoto = new SendPhoto().setChatId(o);
                try {
                    sendMessage(sendMessage);
                } catch (TelegramApiException e) {
                    showAlert("Не удалось отправить текст,либо текст не был прикреплен.");
                }
                try {
                    if (!filepath.equals("")) {
                        sendPhoto.setNewPhoto(new File(filepath));
                        sendPhoto(sendPhoto);
                    }
                } catch (TelegramApiException | NullPointerException e) {
                    showAlert("Не удалось отправить изображение,либо изображение не было прикреплено.");
                }
            }
            if (filepath.equals("")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                dateBase.addNews(LocalDate.now().format(formatter), text);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                byte[] imageByte = null;
                dateBase.addNewsWithImage(LocalDate.now().format(formatter), text, imageByte, filepath);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            dateBase.disconnect();
        }
    }
    //рассылка конкретному пользователю
    public void sendMsg(String text, String id) {
        SendMessage sendMessage = new SendMessage().setChatId(id);
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            showAlert("Не удалось отправить текст,либо текст не был прикреплен.");
        }
    }

    //показать информацию в всплывающем окне
    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информационное сообщение");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    //команда ?
    public String help() {
        String s = "Здравствуйте!\nЯ бот-помощник профсоюза в Вашем подразделении.\nВы можете использовать следующие команды:\n\n⚽️\uD83E\uDD47\uD83C\uDFAD\n" + "новости - расскажу о прошедших и будущих событиях, доступных билетах в театр и на спортивные соревнования, приглашу поучаствовать в интересных мероприятиях.\n\n\uD83D\uDCA1\n" + "идея - поделитесь со мной своей идеей по улучшению социальной жизни нашего предприятия, предложением по организации мероприятия или любой другой идеей, и мы попробуем ее осуществить (пример команды: идея я предлагаю собраться...)\n\n\uD83D\uDC4D\n" + "связь - оставьте свою обратную связь по проведению мероприятия, поделитесь впечатлениями и мы учтем Ваши оценки в будущем (пример команды: связь мне понравилось, как прошла...)\n\n\uD83D\uDCCC\n" + "справка - покажу полезные справочные материалы по социальной политике нашей компании\n\n" + "\"?\" - отправьте для получения списка команд\n\n"+"Я доступен с 08:00 до 17:00 по московскому времени. Буду рад Вам помочь!";
        return s;
    }

    //команда start
    public void addContact(Update update) {
        dateBase = new SQLliteDateBase();
        try {
            dateBase.connect();
            Long id = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String userName = update.getMessage().getFrom().getUserName();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String date = LocalDate.now().format(formatter);
            dateBase.setAddContact(id, firstName, lastName, userName, date);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //команда идея
    public String getIdea(Update update) {
        dateBase = new SQLliteDateBase();
        try {
            dateBase.connect();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String date = LocalDate.now().format(formatter);
            String text = update.getMessage().getText();
            Long id = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getUserName();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            dateBase.addIdea(date, text, id, userName, firstName, lastName);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String s = "Мы рады, что Вы решили поделиться с мной идеей! Я получил ее и уже совсем скоро ее посмотрят!\uD83D\uDC4D\nНа всякий случай, убедитесь, что правильно использовали команду. Сообщение должно начинаться со слово идея, далее в нем же текст.";
        return s;
    }

    //команда связь
    public String getFeedback(Update update) {
        dateBase = new SQLliteDateBase();
        try {
            dateBase.connect();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String date = LocalDate.now().format(formatter);
            String text = update.getMessage().getText();
            Long id = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getUserName();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            dateBase.addFeedback(date, text, id, userName, firstName, lastName);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String s = "Вы оставили обратную связь. Благодаря Вашему участию мы создадим лучшие условия труда и отдыха!\uD83D\uDC4C\nНа всякий случай, убедитесь, что правильно использовали команду. Сообщение должно начинаться со слово связь, далее в нем же текст.";
        return s;
    }

    //команда справка
    public String getInfo() {
        String s = "Сейчас мы занимаемся заполнением данного раздела. Здесь будет самая полезная информация. Может быть у Вас есть идеи, что бы Вы хотели здесь видеть? Воспользуйтесь командой \"идея\".\uD83D\uDCD4";
        return s;
    }

    //команда новости
    public String showNews() {
        dateBase = new SQLliteDateBase();
        String s = "-";
        try {
            dateBase.connect();
            s = dateBase.getLastNews();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return s;
    }
    //url картинки для команды новости
    public String showNewsImage() {
        dateBase = new SQLliteDateBase();
        String s = "-";
        try {
            dateBase.connect();
            s = dateBase.getLastImageUrl();
            //System.out.println(s);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return s;
    }

    //запись запроса для дальнейшего анализа
    public void addQueries(Update update) {
        dateBase = new SQLliteDateBase();
        try {
            dateBase.connect();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String date = LocalDate.now().format(formatter);
            String text = update.getMessage().getText();
            dateBase.addQueries(date, text);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
