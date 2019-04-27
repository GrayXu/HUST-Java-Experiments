package sample;

import db.Config;
import db.DataBase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;
import sample.tool.TimeManager;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class Start extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // intialize connector
        try {

            org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
            BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.translucencyAppleLike;
            org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
            UIManager.put("RootPane.setupButtonVisible", false);
            setFontForBeautyEye();


            DataBase.getInstance().connectDataBase(Config.LocalDBDomain, 3306, "java_lab2", "java", "javajava");
            Parent root = FXMLLoader.load(getClass().getResource("../fxml/Login.fxml"));
            primaryStage.setTitle("Hospital System");
            primaryStage.setMinWidth(400);
            primaryStage.setMinHeight(190);
            primaryStage.setScene(new Scene(root));
            primaryStage.getIcons().add(new Image(this.getClass().getResource("hos.png").toString()));
            primaryStage.setResizable(false);
            primaryStage.setOnCloseRequest(e-> TimeManager.stopAllThread());
            primaryStage.show();
        } catch (SQLException e) {
            System.err.println("failed to connect to sql database");
            Parent root = FXMLLoader.load(getClass().getResource("../fxml/Fail.fxml"));
            primaryStage.setTitle("Info");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.setOnCloseRequest((WindowEvent event1) -> TimeManager.stopAllThread());
            primaryStage.show();

        }

    }

    public static void main(String[] args) {
        launch(args);
//        System.out.println(matchSubConseq("13","1234"));
    }


    /**
     * 修复字体发虚
     */
    private static void setFontForBeautyEye() {
        String[] DEFAULT_FONT = new String[]{
                "Table.font"
                , "TableHeader.font"
                , "CheckBox.font"
                , "Tree.font"
                , "Viewport.font"
                , "ProgressBar.font"
                , "RadioButtonMenuItem.font"
                , "ToolBar.font"
                , "ColorChooser.font"
                , "ToggleButton.font"
                , "Panel.font"
                , "TextArea.font"
                , "Menu.font"
                , "TableHeader.font"
                , "OptionPane.font"
                , "MenuBar.font"
                , "Button.font"
                , "Label.font"
                , "PasswordField.font"
                , "ScrollPane.font"
                , "MenuItem.font"
                , "ToolTip.font"
                , "List.font"
                , "EditorPane.font"
                , "Table.font"
                , "TabbedPane.font"
                , "RadioButton.font"
                , "CheckBoxMenuItem.font"
                , "TextPane.font"
                , "PopupMenu.font"
                , "TitledBorder.font"
                , "ComboBox.font"
        };

        for (String aDEFAULT_FONT : DEFAULT_FONT) {
            UIManager.put(aDEFAULT_FONT, new Font("微软雅黑", Font.PLAIN, 12));
        }
    }
}
