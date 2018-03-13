package sample.server;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by pc on 11.06.2017.
 */
public class SQLliteDateBase {
    private Connection connection;//позволяет подключаться к базе
    private PreparedStatement addNews;
    private PreparedStatement addNewsWithImage;
    private PreparedStatement addContact;
    private PreparedStatement getContactList;
    private PreparedStatement getLastNews;
    private PreparedStatement addIdea;
    private PreparedStatement addFeedback;
    private PreparedStatement getLastImageUrl;
    private PreparedStatement addQueries;
    private Statement stm;

    //получение данных для таблиц
    public String getSelectAllTable(String table) {
        String s = "";
        if (table.equals("news")) s = "SELECT id, date, newstext from news";
        if (table.equals("ideas")) s = "SELECT id, date,textIdea,userId,username,firstName,lastName from ideas";
        if (table.equals("feedback")) s = "SELECT id, date,textFeedback,userId,username,firstName,lastName from feedback";
        if (table.equals("contacts")) s = "SELECT id,firstName,lastName,userName,date from contacts";
        if (table.equals("queries")) s = "SELECT id,date,text from queries";
        return s;
    }

    //подключиться к базе данных
    public void connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:profsouz.db");
        addContact = connection.prepareStatement("INSERT INTO contacts (id,firstName,lastName,userName,date) VALUES (?,?,?,?,?)");
        getContactList = connection.prepareStatement("SELECT id from contacts");
        getLastNews = connection.prepareStatement("SELECT * from news");
        getLastImageUrl = connection.prepareStatement("SELECT id,url from news");
        addIdea = connection.prepareStatement("INSERT INTO ideas (date,textIdea,userId,username,firstName,lastName) VALUES (?,?,?,?,?,?)");
        addFeedback = connection.prepareStatement("INSERT INTO feedback (date,textFeedback,userId,username,firstName,lastName) VALUES (?,?,?,?,?,?)");
        addNews = connection.prepareStatement("INSERT INTO news (date,newstext) VALUES (?,?);");
        addNewsWithImage = connection.prepareStatement("INSERT INTO news (date,newstext,url) VALUES (?,?,?);");
        addQueries = connection.prepareStatement("INSERT into queries (date,text) VALUES (?,?)");
    }

    //закрыть подключение
    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //добавить запрос
    public void addQueries(String date,String text){

        String array[] ={date,text};
        for(int i=0;i<array.length;i++){
            if(array[i]== null||array[i].equals("")){
                array[i] = "-";
            }
        }
        try {
            addQueries.setString(1, array[0]);
            addQueries.setString(2, array[1]);
            addQueries.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //добавить новость date,newstext
    public void addNews(String date,String text){
        String array[] ={date,text};
        for(int i=0;i<array.length;i++){
            if(array[i]== null||array[i].equals("")){
                array[i] = "-";
            }
        }
        try {
            addNews.setString(1, array[0]);
            addNews.setString(2, array[1]);
            addNews.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //добавить новость ,image
    public void addNewsWithImage(String date,String text,byte[] imageByte, String imagePath){
        //imageByte = fileToBytes(imageByte, imagePath);
        try {
            addNewsWithImage.setString(1, date);
            addNewsWithImage.setString(2, text);
           // addNewsWithImage.setBytes(3, imageByte);
            addNewsWithImage.setString(3,imagePath);
            addNewsWithImage.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //поместить контакт в БД
    public void setAddContact(Long id, String firstName, String lastName, String userName, String date) {
        String array[] ={firstName,lastName,userName,date};
        for(int i=0;i<array.length;i++){
            if(array[i]== null||array[i].equals("")){
                array[i] = "-";
            }
        }
        try {
            addContact.setLong(1, id);
            addContact.setString(2, array[0]);
            addContact.setString(3, array[1]);
            addContact.setString(4, array[2]);
            addContact.setString(5, array[3]);
            addContact.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //выбрать все контакты из БД
    public ArrayList<Long> getContactList() {
        ArrayList<Long> contactlist = new ArrayList<>();
        try {
            ResultSet rs = getContactList.executeQuery();
            //System.out.println(rs);
            while (rs.next()) {
                Long id = rs.getLong(1);
                contactlist.add(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //System.out.println(contactlist);
        return contactlist;
    }

    //последняя новость
    public String getLastNews() {
        TreeMap<Integer, String> news = new TreeMap<>();
        String s = "-";
        try {
            ResultSet rs = getLastNews.executeQuery();
            //System.out.println(rs);
            while (rs.next()) {
                int id = rs.getInt("id");
                String newsText = rs.getString("newstext");
                news.put(id, newsText);
            }
            //System.out.println(news);
            if(news.size()!=0) {
                s = "1. " + news.get(news.lastKey() - 1) + "\n2. " + news.get(news.lastKey());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return s;
    }

    //последния картинка
    public String getLastImageUrl(){
        TreeMap<Integer, String> urlList = new TreeMap<>();
        String s = "-";
        try {
            ResultSet rs = getLastImageUrl.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String url = rs.getString("url");
                urlList.put(id, url);
            }
            if(urlList.size()!=0) {
                s = urlList.get(urlList.lastKey());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return s;
    }

    //добавить идею пользователя в БД
    public void addIdea(String date, String text, Long id, String userName, String firstName, String lastName) {
        String array[] ={date,text,userName,firstName,lastName};
        for(int i=0;i<array.length;i++){
            if(array[i]== null||array[i].equals("")){
                array[i] = "-";
            }
        }
        try {
            addIdea.setString(1, array[0]);
            addIdea.setString(2, array[1]);
            addIdea.setLong(3, id);
            addIdea.setString(4, array[2]);
            addIdea.setString(5, array[3]);
            addIdea.setString(6, array[4]);
            addIdea.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //добавить обратную связь пользователя в БД
    public void addFeedback(String date, String text, Long id, String userName, String firstName, String lastName) {
        String array[] ={date,text,userName,firstName,lastName};
        for(int i=0;i<array.length;i++){
            if(array[i]== null||array[i].equals("")){
                array[i] = "-";
            }
        }
        try {
            addFeedback.setString(1, array[0]);
            addFeedback.setString(2, array[1]);
            addFeedback.setLong(3, id);
            addFeedback.setString(4, array[2]);
            addFeedback.setString(5, array[3]);
            addFeedback.setString(6, array[4]);
            addFeedback.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ

    //преобразовать файла в формат byte
    public byte[] fileToBytes(byte[] imageByte, String imagePath) {
        File image = new File(imagePath);
        try {
            FileInputStream fis = new FileInputStream(image);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                bos.write(buf, 0, readNum);
            }
            imageByte = bos.toByteArray();
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return imageByte;
    }

    //получить id последней записи
    public int getLastId() {
        int id = 0;
        try {
            stm = connection.createStatement();
            ResultSet rs = stm.executeQuery("SELECT LAST_INSERT_ROWID();");
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    //для таблицы
    public ObservableList<ObservableList> getAllInfo(ObservableList<ObservableList> data, TableView tableView, String array[], String sql) {
        try {

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:profsouz.db");
            stm = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            tableView.getColumns().clear();
            ResultSet rs = stm.executeQuery(sql);
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                //We are using non property style for making dynamic table
                final int j = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(param ->
                        new SimpleStringProperty(param.getValue().get(j).toString()));
                col.setText(array[i]);
                col.setMinWidth(140);
                tableView.getColumns().addAll(col);
            }
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                //row.removeAll();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stm != null) stm.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
            }
        }
        return data;

    }

}
