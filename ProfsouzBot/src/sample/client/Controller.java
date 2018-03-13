package sample.client;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import sample.server.Bot;
import sample.server.SQLliteDateBase;
import sample.server.Server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextField tfUrl;
    @FXML
    Button btnStartServer;
    @FXML
    Button btnBroadcast;
    @FXML
    Button btnUpload;
    @FXML
    Button btnNews;
    @FXML
    Button btnIdeas;
    @FXML
    Button btnFeedback;
    @FXML
    Button btnContacts;
    @FXML
    TableView tvTable;
    @FXML
    TextArea taText;
    @FXML
    HBox authPanel;
    @FXML
    HBox mainPanel;
    @FXML
    Button btnAuth;
    @FXML
    TextField tfLogin;
    @FXML
    PasswordField pfPass;
    @FXML
    HBox broadcastPanel;
    @FXML
    HBox sendMsgPanel;

    @FXML
    TextField tfMsg;
    @FXML
    TextField tfID;

    private Bot bot;
    private String urlPhoto;
    private boolean isServerStarted = false;
    private boolean isMainPage;
    private boolean authorized;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
    }

    //КНОПКИ
    //запустить сервер, который вызывает бота
    public void startServer() {
        if (!isServerStarted) {
            try {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Server s = new Server();
                        bot = s.getBot();
                    }
                });
                t.setDaemon(true);
                t.start();
                isServerStarted = true;
                showAlert("Сервер запущен. Чат-бот доступен в сети Интернет.");
                showNewsTable();
            } catch (Exception e) {
                showAlert("Не удалось подключиться к серверу.");
            }
        } else {
            showAlert("Сервер уже запущен.");
        }
    }

    //направить рассылку всем контактам
    public void broadcastMessage() {
        if (isServerStarted) {
            try {
                urlPhoto = tfUrl.getText();
                String text = taText.getText();
                if (taText.getText().equals("")) {
                    showAlert("Отправка изображения без текста не возможна. Добавьте краткий текст.");
                } else {
                    bot.broadcastMessage(text, urlPhoto);
                    showNewsTable();
                    tfUrl.clear();
                    taText.clear();
                }
            } catch (Exception e) {
                showAlert("Не удалось совершить отправку, проверьте соединение с сервером.");
                e.printStackTrace();
            }
        } else {
            showAlert("Не удалось совершить отправку, сервер не запущен. Чат бот не в сети.");
        }
    }

    //отправить сообщение конкретному пользователю
    public void sendMessage() {
        if (isServerStarted) {
            try {
                String id = tfID.getText();
                String text = tfMsg.getText();
                bot.sendMsg(text, id);
                tfMsg.clear();
                tfID.clear();
            } catch (Exception e) {
                showAlert("Не удалось совершить отправку, проверьте соединение с сервером.");
                e.printStackTrace();
            }
        } else {
            showAlert("Не удалось совершить отправку, сервер не запущен. Чат бот не в сети.");
        }
    }

    //выбрать изображение для отправки
    public void chooseFile() {
        urlPhoto = null;
        Platform.runLater(() -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Открыть файл с изображением");
                fileChooser.getExtensionFilters().addAll(
                        //new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
                //new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
                //new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedFile = fileChooser.showOpenDialog(null);
                if (selectedFile != null) {
                    tfUrl.clear();
                    urlPhoto = selectedFile.getPath();
                    tfUrl.setText(urlPhoto);
                }
            } catch (Exception e) {
            }
        });
    }

    //показать таблицу idea
    public void showIdeasTable() {
        setMainPage(false);
        loadTable("ideas", new String[]{"Номер", "Дата", "Описание идеи", "id пользователя", "username", "Имя", "Фамилия"});
    }

    //показать таблицу news
    public void showNewsTable() {
        setMainPage(true);
        loadTable("news", new String[]{"Номер", "Дата публикации", "Текст новости"});
    }

    //показать таблицу feedback
    public void showFeedbackTable() {
        setMainPage(false);
        loadTable("feedback", new String[]{"Номер", "Дата", "Текст сообщения", "id пользователя", "username", "Имя", "Фамилия"});
    }

    //показать таблицу contacst
    public void showContactsTable() {
        setMainPage(false);
        loadTable("contacts", new String[]{"id", "Имя", "Фамилия", "username", "Дата добавления"});
    }

    //показать таблицу queries
    public void showQueriesTable() {
        setMainPage(false);
        loadTable("queries", new String[]{"id", "Дата", "Запрос"});
    }

    //ФУНКЦИИ
    //загрузка таблицы из базы данных
    public void loadTable(String table, String array[]) {
        taText.clear();
        SQLliteDateBase dateBase = new SQLliteDateBase();;
        Platform.runLater(() -> {
            try {
                ObservableList<ObservableList> tableList;
                tableList = FXCollections.observableArrayList();//инициализировали пустую коллекцию
                tableList = dateBase.getAllInfo(tableList, tvTable, array, dateBase.getSelectAllTable(table));
                tvTable.getItems().clear();
                tvTable.setItems(tableList);//привязали элемент управления к коллекции

            } catch (Exception e) {
                showAlert("Не удалось отобразить таблицу.");
                e.printStackTrace();
            }finally {
                dateBase.disconnect();
            }

        });
    }

    //отображение новости из таблицы в textarea
    public void selectRowinTable() {
        taText.clear();
        try {
            String s = (tvTable.getSelectionModel().getSelectedItem()).toString();
            String array[] = s.split(",");
            taText.appendText(array[1] + "\n");
            for (int i = 2; i < array.length; i++) {
                taText.appendText(array[i]);
            }
        } catch (Exception e) {
        }
    }

    //метод, позволяющий показывать всплывающие окна
    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информационное сообщение");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    //управление панелью
    public void setMainPage(boolean isMainPage) {
        this.isMainPage = isMainPage;
        if (isMainPage) {
            broadcastPanel.setVisible(true);
            broadcastPanel.setManaged(true);
            sendMsgPanel.setVisible(false);
            sendMsgPanel.setManaged(false);

        } else {
            broadcastPanel.setVisible(false);
            broadcastPanel.setManaged(false);
            sendMsgPanel.setVisible(true);
            sendMsgPanel.setManaged(true);
        }
    }

    //управление панелью 2
    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (authorized) {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            mainPanel.setVisible(true);
            mainPanel.setManaged(true);
        } else {
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            mainPanel.setVisible(false);
            mainPanel.setManaged(false);
        }

    }

    //авторизация
    public void authorize() {
        if (tfLogin.getText().equals("profsouzTCFTO") & pfPass.getText().equals("placebo")) {
            setAuthorized(true);
            setMainPage(true);
        } else {
            showAlert("Не верные логин, пароль. Проверьте правильность ввода данных.");
        }
    }
}
