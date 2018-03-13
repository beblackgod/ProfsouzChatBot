package sample.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Профсоюзный помощник");
        Image image = new Image("sample/client/icons/bot.png");
        primaryStage.getIcons().add(image);
        primaryStage.setScene(new Scene(root, 600, 650));
        primaryStage.getScene().getStylesheets().add("sample/client/styles.css");
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
