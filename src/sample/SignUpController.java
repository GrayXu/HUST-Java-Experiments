package sample;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.robot.impl.FXRobotHelper;
import db.DataBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import sample.tool.TimeManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SignUpController {
    @FXML
    JFXTextField textSignName;
    @FXML
    JFXPasswordField textSignPassword;
    @FXML
    Label labelSign;
    @FXML
    Label labelSignTime;
    @FXML
    GridPane rootSignPanel;

    private static boolean isPressedControl = false;


    @FXML
    void initialize() {
        TimeManager.bindLableThread("sign", labelSignTime);
//        FXRobotHelper.getStages().get(0).setMaxWidth(10);

        rootSignPanel.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.CONTROL) {
                isPressedControl = false;
            } else if (e.getCode() == KeyCode.E && isPressedControl) {
                try {
                    handleSignUpExit();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else if (e.getCode() == KeyCode.C && isPressedControl) {
                try {
                    handleSignUp();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        rootSignPanel.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.CONTROL) {
                isPressedControl = true;
            }
        });
        rootSignPanel.setBackground(new Background(new BackgroundFill(Color.color(1, 1, 0.95),null,null)));
    }


    @FXML
    void handleSignUp() throws IOException {
        textSignName.setStyle("");
        textSignPassword.setStyle("");
        String name = textSignName.getText();
        String pwd = textSignPassword.getText();
        String pid;
        if (isNewInfoValid(name, pwd)) {
            try {
                pid = DataBase.getInstance().insertPatientInfo(name, pwd, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                showDialog("Successful! Remember your unique id " + pid + " to login");
                Scene scene = new Scene(FXMLLoader.load(getClass().getResource("../fxml/Login.fxml")));
                FXRobotHelper.getStages().get(0).setScene(scene);
            } catch (SQLException e) {
                e.printStackTrace();
                showDialog("YOU JUST FUCK UP!");
            }


        } else {
            labelSign.setText("invalid password or name format");
            labelSign.setStyle("-fx-text-fill: red;");
        }
    }

    private void showDialog(String info) {
        Alert information = new Alert(Alert.AlertType.INFORMATION);
        information.setTitle("Notify"); //设置标题，不设置默认标题为本地语言的information
        information.setHeaderText(info); //设置头标题，默认标题为本地语言的information
        Button infor = new Button("Okay");
        information.showAndWait();
    }

    //check patients' info format
    private boolean isNewInfoValid(String name, String pwd) {
        if (name.length() == 0) {
            textSignName.setStyle("-fx-background-color: pink;");
            return false;
        } else if (pwd.length() == 0) {
            textSignPassword.setStyle("-fx-background-color: pink;");
            return false;
        } else if (pwd.length() > 8 || name.length() < 2 || name.contains("\"") || name.contains("\'") || pwd.contains("\'") || pwd.contains("\"") || pwd.length() <= 1) {
            return false;
        } else {
            return true;
        }

    }

    @FXML
    void handleSignUpExit() throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("../fxml/Login.fxml")));
        TimeManager.getInstance().start("login");
        TimeManager.getInstance().stop("sign");
        FXRobotHelper.getStages().get(0).setScene(scene);

    }

}
