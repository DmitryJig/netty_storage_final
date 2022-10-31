package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.logic.ClientLogic;

import java.util.List;

public class FxController {
    @FXML
    public TextField loginField;
    @FXML
    public TextField passwordField;
    private String login;
    private String password;
    @FXML
    public HBox fileBox;
    @FXML
    public VBox loginBox;
    @FXML
    private ListView<String> serverFileList;
    @FXML
    private ListView<String> clientFileList;

    public void updateUserFileList(ActionEvent actionEvent) {
        updateUserFilesList();
    }

    public void onSendToServerButtonClick(ActionEvent actionEvent) {
        final String path = clientFileList.getSelectionModel().getSelectedItem();
        List<String> filesInServer = ClientLogic.sendFileToServer(path, login, password);
        updateServerFilesList(filesInServer);
    }

    public void onDeleteInClientButtonClick(ActionEvent actionEvent) {
        final String fileForDelete = clientFileList.getSelectionModel().getSelectedItem();
        ClientLogic.deleteFile(fileForDelete);
        updateUserFilesList();
    }

    public void updateServerFileList(ActionEvent actionEvent) {

        List<String> serverFilesList = ClientLogic.getServerFileList(login, password);
        updateServerFilesList(serverFilesList);
    }

    public void onSendToClientButtonClick(ActionEvent actionEvent) {
        final String path = serverFileList.getSelectionModel().getSelectedItem();
        ClientLogic.sendFromServerToClient(path, login, password);
        updateUserFilesList();
    }

    public void onDeleteInServerButtonClick(ActionEvent actionEvent) {
        final String fileForDelete = serverFileList.getSelectionModel().getSelectedItem();
        List<String> serverFilesList = ClientLogic.deleteFileInServer(fileForDelete, login, password);
        updateServerFilesList(serverFilesList);
    }

    public void updateUserFilesList() {
        clientFileList.getItems().clear();
        clientFileList.getItems().addAll(ClientLogic.getFileList());
        System.out.println("Список файлов в директории клиента обновлен");
        clientFileList.getSelectionModel().selectFirst();
    }

    public void updateServerFilesList(List<String> files) {
        System.out.println("обновление списка файлов на сервере, получены имена файлов: " + files);
        serverFileList.getItems().clear();
        serverFileList.getItems().addAll(files);
        System.out.println("Список файлов на сервере обновлен");
        serverFileList.getSelectionModel().selectFirst();
    }

    public void authButtonClick(ActionEvent actionEvent) {
        this.login = loginField.getText();
        this.password = passwordField.getText();
        List<String> serverFilesList = ClientLogic.getServerFileList(login, password);
        loginBox.setVisible(false);
        fileBox.setVisible(true);
        updateUserFilesList();
        updateServerFilesList(serverFilesList);
    }

    public void regNewClientButtonClick(ActionEvent actionEvent) {
        this.login = loginField.getText();
        this.password = passwordField.getText();
        if (ClientLogic.regNewClientInServer(login, password)){
            loginBox.setVisible(false);
            fileBox.setVisible(true);
            updateUserFilesList();
        }
    }
}
