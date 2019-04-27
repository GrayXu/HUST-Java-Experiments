package sample;

import com.jfoenix.controls.*;
import com.sun.javafx.robot.impl.FXRobotHelper;
import db.Config;
import db.DataBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import sample.tool.TimeManager;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoginController {
    @FXML
    JFXTextField inputUsername;
    @FXML
    JFXPasswordField inputPassword;
    @FXML
    JFXButton buttonLoginDoctor;
    @FXML
    JFXButton buttonLoginPatient;
    @FXML
    JFXButton buttonPatientSignUp;
    @FXML
    Label labelStatus;
    @FXML
    ImageView imageLogin;
    @FXML
    GridPane rootLoginPanel;

    private static boolean isPressedControl = false;

    @FXML
    void initialize() {
        imageLogin.setImage(new Image(Config.LoginImagePath));

        rootLoginPanel.setOnKeyReleased(e ->{
            if (e.getCode() ==KeyCode.CONTROL){
                isPressedControl = false;
            }else if (e.getCode() == KeyCode.D && isPressedControl){
                try {
                    doctorLogin();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }else if (e.getCode() == KeyCode.P &&isPressedControl){
                try {
                    patientLogin();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }else if (e.getCode() == KeyCode.S && isPressedControl){
                try {
                    patientSignUp();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        });
        rootLoginPanel.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.CONTROL){
                isPressedControl = true;
            }
        });



        buttonLoginDoctor.setOnKeyReleased(keyEvent -> {
            try {
                if (keyEvent.getCode() == KeyCode.ENTER)
                    doctorLogin();
            } catch (IOException e) {
            }
        });

        buttonLoginPatient.setOnKeyReleased(keyEvent -> {
            try {
                if (keyEvent.getCode() == KeyCode.ENTER)
                    patientLogin();
            } catch (IOException e) {
            }
        });

        buttonPatientSignUp.setOnKeyReleased(keyEvent -> {
            try {
                if (keyEvent.getCode() == KeyCode.ENTER)
                    patientSignUp();
            } catch (IOException e) {
            }
        });
        TimeManager.bindLableThread("login", labelStatus);
        TimeManager.getInstance().start("login");
//        FXRobotHelper.getStages().get(0).setOnCloseRequest(event -> TimeManager.stopAllThread());

        rootLoginPanel.setBackground(new Background(new BackgroundFill(Color.color(1, 1, 0.95),null,null)));
    }

    @FXML
    void doctorLogin() throws IOException {
        if (!isUserNameAndPasswordValid()) {
            return;
        }

        ResultSet result = DataBase.getInstance().getDoctorInfo(inputUsername.getText().trim());

        try {
            if (result == null || !result.next()) {
                showDialog("No such user id");
                return;
            } else if (!result.getString(Config.NameTableColumnDoctorPassword).equals(inputPassword.getText())) {
                showDialog("Wrong password or username");
                return;
            }

            DoctorController.doctorName = result.getString(Config.NameTableColumnDoctorName);
            DoctorController.doctorNumber = result.getString(Config.NameTableColumnDoctorNumber);

            DataBase.getInstance().updateDoctorLoginTime(
                    result.getString(Config.NameTableColumnDoctorNumber),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("../fxml/Doctor.fxml")));
            scene.getStylesheets().add(getClass().getResource("../fxml/Main.css").toExternalForm());
            TimeManager.getInstance().start("login");
            FXRobotHelper.getStages().get(0).setResizable(false);
            FXRobotHelper.getStages().get(0).setScene(scene);
            TimeManager.getInstance().start("doctor");
        } catch (SQLException e) {
            // progrom shouldn't came here
            e.printStackTrace();
            return;
        }
    }

    @FXML
    void patientLogin() throws IOException {
        if (!isUserNameAndPasswordValid()) {
            return;
        }

        ResultSet result = DataBase.getInstance().getPatientInfo(inputUsername.getText().trim());

        try {
            if (result == null || !result.next()) {
                showDialog("No such user id");
                return;
            } else if (!result.getString(Config.NameTableColumnPatientPassword).equals(inputPassword.getText())) {
                showDialog("Wrong password or username");
                return;
            }

            // fill info and login to patient page
            PatientController.patientName = result.getString(Config.NameTableColumnPatientName);
            PatientController.patientBalance = result.getDouble(Config.NameTableColumnPatientBalance);
            PatientController.patientNumber = result.getString(Config.NameTableColumnPatientNumber);

            DataBase.getInstance().updatePatientLoginTime(
                    result.getString(Config.NameTableColumnPatientNumber),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("../fxml/Patient.fxml")));
            scene.getStylesheets().add(getClass().getResource("../fxml/Main.css").toExternalForm());
            TimeManager.getInstance().stop("login");
            FXRobotHelper.getStages().get(0).setResizable(false);
            FXRobotHelper.getStages().get(0).setScene(scene);
            TimeManager.getInstance().start("patient");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    @FXML
    void patientSignUp() throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("../fxml/SignUp.fxml")));
        TimeManager.getInstance().stop("login");
        FXRobotHelper.getStages().get(0).setScene(scene);
        FXRobotHelper.getStages().get(0).setResizable(false);
        TimeManager.getInstance().start("sign");
    }

    private boolean isUserNameAndPasswordValid() {
        if (inputUsername.getText().isEmpty()) {
            inputUsername.setStyle("-fx-background-color: pink;");
//            showDialog("input usename please");
            return false;
        }
        if (inputPassword.getText().isEmpty()) {
            inputPassword.setStyle("-fx-background-color: pink;");
//            showDialog("input password please");
            return false;
        }

        labelStatus.setText("Login, wait...");
        labelStatus.setStyle("");
        return true;
    }

    @FXML
    void onInputUsernameAction() {
        inputUsername.setStyle("");
    }

    @FXML
    void onInputPasswordAction() {
        inputPassword.setStyle("");
    }

    private void showDialog(String info) {
        Alert information = new Alert(Alert.AlertType.INFORMATION);
        information.setTitle("Notify"); //设置标题，不设置默认标题为本地语言的information
        information.setHeaderText(info); //设置头标题，默认标题为本地语言的information
        Button infor = new Button("Okay");
        information.showAndWait();
        labelStatus.setText("");
    }
}

// TODO: update login time