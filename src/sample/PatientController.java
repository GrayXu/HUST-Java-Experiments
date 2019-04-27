package sample;

import com.jfoenix.controls.*;
import com.sun.javafx.robot.impl.FXRobotHelper;
import db.Config;
import db.DataBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.regex.Pattern;

import javafx.scene.paint.Color;
import sample.bean.*;
import sample.tool.TimeManager;
import sample.tool.TryRegisterService;

public class PatientController {
    // basic patient info, initialized by LoginController
    static public String patientName;
    static public String patientNumber;
    static public Double patientBalance;

    @FXML
    private GridPane mainPane;
    @FXML
    private Label labelWelcome;
    @FXML
    private JFXComboBox<String> inputNameDepartment;
    @FXML
    private JFXComboBox<String> inputNameDoctor;
    @FXML
    private JFXComboBox<String> inputTypeRegister;
    @FXML
    private JFXComboBox<String> inputNameRegister;
    @FXML
    private Label labelFee;
    @FXML
    private Label labelChange;
    @FXML
    private Label labelStatus;
    @FXML
    private JFXTextField textPay_my;
    @FXML
    private JFXButton buttonRegister;
    @FXML
    private JFXButton buttonExit;
    @FXML
    private JFXCheckBox checkBoxUseBalance;
    @FXML
    private JFXCheckBox checkBoxAddToBalance;
    @FXML
    private Label labelPatientTime;

    private int lastIndexInputNameDepartment = -1;
    private int lastIndexInputNameDoctor = -1;
    private int lastIndexInputTypeRegister = -1;
    private int lastIndexInputNameRegister = -1;

    // data list
    private ObservableList<ListItemNameDepartment> listNameDepartment = FXCollections.observableArrayList();
    private ObservableList<ListItemNameDoctor> listNameDoctor = FXCollections.observableArrayList();
    private ObservableList<ListItemTypeRegister> listTypeRegister = FXCollections.observableArrayList();
    private ObservableList<ListItemNameRegister> listNameRegister = FXCollections.observableArrayList();

    private ObservableList<ListItemNameDepartment> listNameDepartmentFiltered = FXCollections.observableArrayList();
    private ObservableList<ListItemNameDoctor> listNameDoctorFiltered = FXCollections.observableArrayList();
    private ObservableList<ListItemTypeRegister> listTypeRegisterFiltered = FXCollections.observableArrayList();
    private ObservableList<ListItemNameRegister> listNameRegisterFiltered = FXCollections.observableArrayList();

    /**
     * @param tableName name of database table related to the combobox
     * @param list      list to update/initialize
     * @param clazz     dumb thing, see https://stackoverflow.com/questions/11404086/how-could-i-initialize-a-generic-array
     * @return if update is successful
     * @brief update data for one editable combobox
     */
    private <ItemType extends ListItem> boolean updateOneSetOfData(
            String tableName,
            ObservableList<ItemType> list,
            Class<ItemType> clazz) {

        // get entire table from database
        ResultSet result = DataBase.getInstance().getWholeTable(tableName);

        if (result != null) {
            ObservableList<ItemType> tempList = FXCollections.observableArrayList();
            try {
                // iterate all entries in table
                while (result.next()) {
                    // get a entry and convert it to ListItem
                    ItemType item = clazz.newInstance();
                    item.fromSqlResult(result);
                    // add to table
                    tempList.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }

            list.clear();
            list.addAll(tempList);
            return true;
        }
        return true;
    }

    private boolean matchSubConseq(String small, String big) {
        //set a regex
        StringBuffer sb = new StringBuffer(".*");
        for (int i = 0; i < small.length(); i++) {
            sb.append("[" + small.charAt(i) + "]+.*");
        }
        return Pattern.matches(String.valueOf(sb), big);
    }

    public void updateData() {
        updateOneSetOfData(
                Config.NameTableDepartment,
                listNameDepartment,
                ListItemNameDepartment.class
        );

        updateOneSetOfData(
                Config.NameTableDoctor,
                listNameDoctor,
                ListItemNameDoctor.class
        );

        updateOneSetOfData(
                Config.NameTableCategoryRegister,
                listNameRegister,
                ListItemNameRegister.class
        );

        // initialize/update register Type info
        // note that this part must be placed under the update of catetgory register
        ListItemTypeRegister itemSpecialist = new ListItemTypeRegister();
        ListItemTypeRegister itemNormal = new ListItemTypeRegister();
        itemSpecialist.isSpecialist = true;
        itemSpecialist.pronounce = "zhuanjiahao";
        itemNormal.isSpecialist = false;
        itemNormal.pronounce = "potonghao";
        listTypeRegister.clear();
        listTypeRegister.add(itemSpecialist);
        listTypeRegister.add(itemNormal);
    }


    private void updateChange() {
        int index = inputNameRegister.getSelectionModel().getSelectedIndex();
        if (index != -1 && checkBoxUseBalance.isSelected()) {
            labelChange.setText("0¥");
            labelChange.setStyle("");
            return;
        }

        float temp;
        if (textPay_my.getText().isEmpty()) {
            temp = 0;
        } else {
            temp = Float.valueOf(textPay_my.getText());
        }
        if (index != -1 && temp > listNameRegisterFiltered.get(index).fee) {
            labelChange.setText(String.format("%.2f¥", temp - listNameRegisterFiltered.get(index).fee));
            labelChange.setStyle("");
        } else if (index != -1) {
            labelChange.setText("Not enough payment");
            labelChange.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * 更新现金余额后，判断更新checkbox提示文本和状态
     */
    private void updateUseBalance() {
        int index = inputNameRegister.getSelectionModel().getSelectedIndex();
        if (index != -1 && patientBalance < listNameRegisterFiltered.get(index).fee) {
            // fore to use cash
            checkBoxUseBalance.setSelected(false);
            textPay_my.setDisable(false);
            checkBoxUseBalance.setText("Not enough remainder");
            checkBoxUseBalance.setDisable(true);
        } else {
            // prefer to use balance
            checkBoxUseBalance.setDisable(false);
            checkBoxUseBalance.setText("Use remainder");
            checkBoxUseBalance.setSelected(true);
            textPay_my.setDisable(true);
        }
    }

    /**
     * 更新现金余额后判断是否可以点击确定挂号
     */
    private void updateRegisterButton() {
        buttonRegister.setDisable(true);

        float temp;
        if (textPay_my.getText().isEmpty()) {
            temp = 0;
        } else {
            temp = Float.valueOf(textPay_my.getText());
        }

        int selectedIndex = inputNameRegister.getSelectionModel().getSelectedIndex();
        if (inputNameDoctor.getSelectionModel().getSelectedIndex() != -1 &&
                selectedIndex != -1 &&
                ((checkBoxUseBalance.isSelected() && patientBalance >= listNameRegisterFiltered.get(selectedIndex).fee) ||
                        (!checkBoxUseBalance.isSelected() && temp >= listNameRegisterFiltered.get(selectedIndex).fee))) {
            buttonRegister.setDisable(false);
        }
    }

    /**
     * 更新checkbox状态、更新找钱
     */
    @FXML
    void useBalanceClicked() {
        if (checkBoxUseBalance.isSelected()) {
            checkBoxAddToBalance.setSelected(false);
            checkBoxAddToBalance.setDisable(true);
            textPay_my.setDisable(true);
            updateChange();
        } else {
            checkBoxAddToBalance.setSelected(false);
            checkBoxAddToBalance.setDisable(false);
            textPay_my.setDisable(false);
            updateChange();
        }
        updateRegisterButton();
    }

    private void updateUserDisplayInfo() {
        labelWelcome.setText(String.format("%s: ¥%.2f", patientName, patientBalance));
    }

    @FXML
    public void initialize() {
        updateUserDisplayInfo();
        // initialize datas
        updateData();

        // initialize combobox datas
        inputNameDepartment.setItems(FXCollections.observableArrayList());
        inputNameDoctor.setItems(FXCollections.observableArrayList());
        inputTypeRegister.setItems(FXCollections.observableArrayList());
        inputNameRegister.setItems(FXCollections.observableArrayList());
        reFilterDepartment(false);
        reFilterDoctor(false);
        reFilterRegisterType(false);
        reFilterRegisterName(false);

        //update time label
        TimeManager.bindLableThread("patient", labelPatientTime);
        TimeManager.getInstance().start("patient");
        updateRegisterButton();

        /**
         * re-filter content on key typed
         */
        inputNameDepartment.getEditor().setOnKeyReleased(keyEvent -> {
            // pass up/down and enter keys
            if (shouldSupressKeyCode(keyEvent.getCode()))
                return;

            reFilterDepartment(true);
            reFilterDoctor(false);
            reFilterRegisterType(false);
            reFilterRegisterName(false);
            if (!inputNameDepartment.isShowing()) {
                inputNameDepartment.show();
            } else {
                inputNameDepartment.hide();
                inputNameDepartment.show();
            }
        });
        inputNameDepartment.addEventHandler(ComboBox.ON_HIDDEN, e -> {
            int index;
            if ((index = inputNameDepartment.getSelectionModel().getSelectedIndex())
                    != lastIndexInputNameDepartment) {
                lastIndexInputNameDepartment = index;
                reFilterDoctor(false);
                reFilterRegisterType(false);
                reFilterRegisterName(false);
            }
            e.consume();
        });
        inputNameDoctor.getEditor().setOnKeyReleased(keyEvent -> {
            // pass up/down and enter keys
            if (shouldSupressKeyCode(keyEvent.getCode()))
                return;

            reFilterDoctor(true);
//            reFilterDepartment(false);
            reFilterRegisterType(false);
            reFilterRegisterName(false);
            if (!inputNameDoctor.isShowing()) {
                inputNameDoctor.show();
            } else {
                inputNameDoctor.hide();
                inputNameDoctor.show();
            }
        });
        inputNameDoctor.addEventHandler(ComboBox.ON_HIDDEN, e -> {
            int index;
            if ((index = inputNameDoctor.getSelectionModel().getSelectedIndex())
                    != lastIndexInputNameDoctor) {
                lastIndexInputNameDoctor = index;
                reFilterDepartment(false);
                reFilterRegisterType(false);
                reFilterRegisterName(false);
            }
            inputNameDoctor.setStyle("");
            updateRegisterButton();
            e.consume();
        });
        inputNameDoctor.setOnMousePressed(mouseEvent -> inputNameDoctor.setStyle(""));

        inputTypeRegister.getEditor().setOnKeyReleased(keyEvent -> {
            // pass up/down and enter keys
            if (shouldSupressKeyCode(keyEvent.getCode())) {
                return;
            }

            reFilterRegisterType(true);
            reFilterDepartment(false);
            reFilterDoctor(false);
            reFilterRegisterName(false);
            if (!inputTypeRegister.isShowing()) {
                inputTypeRegister.show();
            } else {
                inputTypeRegister.hide();
                inputTypeRegister.show();
            }
        });
        inputTypeRegister.addEventHandler(ComboBox.ON_HIDDEN, e -> {
            int index;
            if ((index = inputTypeRegister.getSelectionModel().getSelectedIndex())
                    != lastIndexInputTypeRegister) {
                lastIndexInputTypeRegister = index;
                reFilterDepartment(false);
                reFilterDoctor(false);
                reFilterRegisterName(false);
            }
            updateRegisterButton();
            e.consume();
        });

        inputNameRegister.getEditor().setOnKeyReleased(keyEvent -> {
            // pass up/down and enter keys
            if (shouldSupressKeyCode(keyEvent.getCode()))
                return;

            reFilterRegisterName(true);
            reFilterDepartment(false);
            reFilterDoctor(false);
            reFilterRegisterType(false);
            if (!inputNameRegister.isShowing()) {
                inputNameRegister.show();
            } else {
                inputNameRegister.hide();
                inputNameRegister.show();
            }
        });
        inputNameRegister.addEventHandler(ComboBox.ON_HIDDEN, e -> {
            int index;
            if ((index = inputNameRegister.getSelectionModel().getSelectedIndex())
                    != lastIndexInputNameRegister) {
                lastIndexInputNameRegister = index;
                reFilterDepartment(false);
                reFilterDoctor(false);
                reFilterRegisterType(false);
            }
            inputNameRegister.setStyle("");
            if (index != -1) {
                float fee = listNameRegisterFiltered.get(index).fee;
                labelFee.setText("" + fee + " ¥");
            }
            updateUseBalance();
            updateChange();
            updateRegisterButton();
            e.consume();
        });
        inputNameRegister.setOnMousePressed(mouseEvent -> {
            inputNameRegister.setStyle("");
        });

        buttonRegister.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)
                buttonRegisterPressed();
        });

        buttonExit.setOnKeyReleased(keyEvent -> {
            try {
                if (keyEvent.getCode() == KeyCode.ENTER)
                    buttonExitClicked();
            } catch (IOException e) {
            }
        });

        checkBoxUseBalance.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.SPACE)
                useBalanceClicked();
            else
                keyEvent.consume();
        });

        textPay_my.setTextFormatter(new TextFormatter<>(c -> {
            if (!c.getControlNewText().matches("\\d*")) {
                return null;
            } else {
                return c;
            }
        }));

        mainPane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.CONTROL) {
                isPressedControl = true;
            }
        });

        mainPane.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.CONTROL) {
                isPressedControl = false;
            } else if (e.getCode() == KeyCode.L && isPressedControl) {
                try {
                    buttonExitClicked();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else if (e.getCode() == KeyCode.C && isPressedControl) {
                if (!buttonRegister.isDisable()) {
                    buttonRegisterPressed();
                }
            }
        });

        mainPane.setBackground(new Background(new BackgroundFill(Color.color(1, 1, 0.95),null,null)));
    }

    private static boolean isPressedControl = false;

    /**
     * 更新现金缴费金额输入回调
     */
    @FXML
    private void sliderPayDragged() {
        updateChange();
        updateRegisterButton();
    }

    /**
     * 确认挂号按键按下回调
     */
    @FXML
    private void buttonRegisterPressed() {
        if (inputNameDoctor.getSelectionModel().getSelectedIndex() == -1) {
            statusError("Choose doctor first");
            inputNameDoctor.setStyle("-fx-background-color: pink;");
            return;
        }
        if (inputNameRegister.getSelectionModel().getSelectedIndex() == -1) {
            statusError("Choose category first");
            inputNameRegister.setStyle("-fx-background-color: pink;");
            return;
        }
        int index;
        if (!((index = inputNameRegister.getSelectionModel().getSelectedIndex()) != -1 &&
                inputNameDoctor.getSelectionModel().getSelectedIndex() != -1 && (
                (checkBoxUseBalance.isSelected() && patientBalance >= listNameRegisterFiltered.get(index).fee) ||
                        (!checkBoxUseBalance.isSelected() && Float.valueOf(textPay_my.getText()) >= listNameRegisterFiltered.get(index).fee)))) {
            // program should not run here
            statusError("Not enough money");
            return;
        }

        // freeze until sql response
        disableEverything();

        float remainder;
        try {
            remainder = Float.valueOf(textPay_my.getText()) - listNameRegisterFiltered.get(index).fee;
        } catch (Exception e) {
            remainder = 0;
        }
        TryRegisterService service = new TryRegisterService(
                listNameRegisterFiltered.get(inputNameRegister.getSelectionModel().getSelectedIndex()).number,
                listNameDoctorFiltered.get(inputNameDoctor.getSelectionModel().getSelectedIndex()).number,
                patientNumber,
                listNameRegisterFiltered.get(inputNameRegister.getSelectionModel().getSelectedIndex()).fee,
                checkBoxUseBalance.isSelected(),
                ((!checkBoxUseBalance.isSelected() && checkBoxAddToBalance.isSelected()) ?
                        remainder : 0)
        );
        float finalRemainder = remainder;
        service.setOnSucceeded(workerStateEvent -> {
            switch (service.returnCode) {
                case registerNumberExceeded:
                    statusError("Population overflow");
                    break;
                case registerCategoryNotFound:
                case sqlException:
                    statusError("Network error");
                    break;
                case retryTimeExceeded:
                    statusError("Busy now..");
                    break;
                case noError:
                    if (checkBoxUseBalance.isSelected()) {
                        showDialog("Registration success: " + service.registerNumber);
                    } else {
                        showDialog("Registration success: " + service.registerNumber + "\nYou got change ￥" + String.format("%.2f¥", finalRemainder) + ".");
                    }
                    patientBalance = service.updatedBalance;
                    updateUserDisplayInfo();
                    break;
            }
            enableEverything();
        });
        service.start();
    }

    private void disableEverything() {
        mainPane.setDisable(true);
    }

    private void enableEverything() {
        mainPane.setDisable(false);
    }

    private void statusError(String error) {
        labelStatus.setText(error);
        labelStatus.setStyle("-fx-text-fill: red;");
    }

    private void showDialog(String info) {
        Alert information = new Alert(Alert.AlertType.INFORMATION);
        information.setTitle("Notify"); //设置标题，不设置默认标题为本地语言的information
        information.setHeaderText(info); //设置头标题，默认标题为本地语言的information
        Button infor = new Button("Okay");
        information.showAndWait();
    }

    private void reFilterDepartment(boolean withoutSelect) {
        int index;
        String previousKey = "";
        if ((index = inputNameDepartment.getSelectionModel().getSelectedIndex()) != -1)
            previousKey = listNameDepartmentFiltered.get(index).number;

        ObservableList<ListItemNameDepartment> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemNameDepartment> list1 = FXCollections.observableArrayList();

        // filter Department Name
        for (ListItemNameDepartment listItemNameDepartment : listNameDepartment) {
            if (listItemNameDepartment.toString().contains(inputNameDepartment.getEditor().getText().trim()) ||
                    listItemNameDepartment.getPronounce().contains(inputNameDepartment.getEditor().getText().trim()) ||
                    matchSubConseq(inputNameDepartment.getEditor().getText().trim(), listItemNameDepartment.toString())) {
                listNameDepartmentFiltered.add(listItemNameDepartment);
                list0.add(listItemNameDepartment);
            }
        }

        // filter again according to doctor
        if ((index = inputNameDoctor.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemNameDepartment department : list0)
                if (department.number.equals(listNameDoctorFiltered.get(index).departmentNumber))
                    list1.add(department);
            list0 = list1;
        }

        // add to filtered list and combobox
        boolean isCurrentInputLegal = false;
        int counter = 0, newSelection = -1;
        inputNameDepartment.getItems().clear();
        listNameDepartmentFiltered.clear();
        for (ListItemNameDepartment department : list0) {
            inputNameDepartment.getItems().add(department.toString());
            listNameDepartmentFiltered.add(department);
            if (department.toString().contains(inputNameDepartment.getEditor().getText().trim()) ||
                    department.getPronounce().contains(inputNameDepartment.getEditor().getText().trim()) ||
                    matchSubConseq(inputNameDepartment.getEditor().getText().trim(), department.toString()))
                isCurrentInputLegal = true;
            if (previousKey.equals(department.number))
                newSelection = counter;
            ++counter;
        }

        // clear illegal input
        if (!withoutSelect) {
            if (!isCurrentInputLegal)
                inputNameDepartment.getEditor().clear();
            if (newSelection != -1) {
                inputNameDepartment.getSelectionModel().clearAndSelect(newSelection);
                inputNameDepartment.getEditor().setText(inputNameDepartment.getItems().get(newSelection));
            }
        }
    }

    private void reFilterDoctor(boolean withoutSelect) {
        int index;
        String previousKey = "";
        if ((index = inputNameDoctor.getSelectionModel().getSelectedIndex()) != -1)
            previousKey = listNameDoctorFiltered.get(index).number;

        ObservableList<ListItemNameDoctor> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemNameDoctor> list1 = FXCollections.observableArrayList();

        // filter Doctor Name
        for (ListItemNameDoctor listItemNameDoctor : listNameDoctor)
            if (listItemNameDoctor.toString().contains(inputNameDoctor.getEditor().getText().trim()) ||
                    listItemNameDoctor.getPronounce().contains(inputNameDoctor.getEditor().getText().trim()) ||
                    matchSubConseq(inputNameDoctor.getEditor().getText().trim(), listItemNameDoctor.toString()))
                list0.add(listItemNameDoctor);

        // filter by department
        if ((index = inputNameDepartment.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemNameDoctor listItemNameDoctor : list0)
                if (listItemNameDoctor.departmentNumber.equals(listNameDepartmentFiltered.get(index).number))
                    list1.add(listItemNameDoctor);
            list0 = list1;
        }

        // filter by register type
        list1 = FXCollections.observableArrayList();
        if ((index = inputTypeRegister.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemNameDoctor doctor : list0)
                if (doctor.isSpecialist || !listTypeRegisterFiltered.get(index).isSpecialist)
                    list1.add(doctor);
            list0 = list1;
        }

        // filter by register name
        list1 = FXCollections.observableArrayList();
        if ((index = inputNameRegister.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemNameDoctor doctor : list0)
                if (doctor.departmentNumber.equals(listNameRegisterFiltered.get(index).department))
                    list1.add(doctor);
            list0 = list1;
        }

        // add to filtered list and combobox
        boolean isCurrentInputLegal = false;
        int counter = 0, newSelection = -1;
        inputNameDoctor.getItems().clear();
        listNameDoctorFiltered.clear();
        for (ListItemNameDoctor doctor : list0) {
            listNameDoctorFiltered.add(doctor);
            inputNameDoctor.getItems().add(doctor.toString());
            if (doctor.toString().contains(inputNameDoctor.getEditor().getText().trim()) ||
                    doctor.getPronounce().contains(inputNameDoctor.getEditor().getText().trim()) ||
                    matchSubConseq(inputNameDoctor.getEditor().getText().trim(), doctor.toString()))
                isCurrentInputLegal = true;
            if (previousKey.equals(doctor.number))
                newSelection = counter;
            ++counter;
        }

        // clear illegal input
        if (!withoutSelect) {
            if (!isCurrentInputLegal)
                inputNameDoctor.getEditor().clear();
            if (newSelection != -1) {
                inputNameDoctor.getSelectionModel().clearAndSelect(counter);
                inputNameDoctor.getEditor().setText(inputNameDoctor.getItems().get(newSelection));
            }
        }
    }

    private void reFilterRegisterType(boolean withoutSelect) {
        int index;
        String previousKey = "";
        if ((index = inputTypeRegister.getSelectionModel().getSelectedIndex()) != -1)
            previousKey = listTypeRegisterFiltered.get(index).pronounce;

        ObservableList<ListItemTypeRegister> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemTypeRegister> list1 = FXCollections.observableArrayList();

        // filter Register Type
        for (ListItemTypeRegister listItemTypeRegister : listTypeRegister)
            if (listItemTypeRegister.toString().contains(inputTypeRegister.getEditor().getText().trim()) ||
                    listItemTypeRegister.getPronounce().contains(inputTypeRegister.getEditor().getText().trim()))
                list0.add(listItemTypeRegister);

        // filter by doctor
        if ((index = inputNameDoctor.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemTypeRegister listItemTypeRegister : list0)
                if (listNameDoctorFiltered.get(index).isSpecialist || !listItemTypeRegister.isSpecialist)
                    list1.add(listItemTypeRegister);
            list0 = list1;
        }

        // filter by register name
        list1 = FXCollections.observableArrayList();
        if ((index = inputNameRegister.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemTypeRegister register : list0)
                if (register.isSpecialist == listNameRegisterFiltered.get(index).isSpecialist)
                    list1.add(register);
            list0 = list1;
        }

        // add to filtered list and combobox list
        boolean isCurrentInputLegal = false;
        int counter = 0, newSelection = -1;
        listTypeRegisterFiltered.clear();
        inputTypeRegister.getItems().clear();
        for (ListItemTypeRegister register : list0) {
            listTypeRegisterFiltered.add(register);
            inputTypeRegister.getItems().add(register.toString());
            if (register.toString().contains(inputTypeRegister.getEditor().getText().trim()) ||
                    register.getPronounce().contains(inputTypeRegister.getEditor().getText().trim()) ||
                    matchSubConseq(inputTypeRegister.getEditor().getText().trim(), register.toString()))
                isCurrentInputLegal = true;
            if (previousKey.equals(register.pronounce))
                newSelection = counter;
            ++counter;
        }

        // delete illegal input
        if (!withoutSelect) {
            if (!isCurrentInputLegal)
                inputTypeRegister.getEditor().clear();
            if (newSelection != -1) {
                inputTypeRegister.getSelectionModel().clearAndSelect(newSelection);
                inputTypeRegister.getEditor().setText(inputTypeRegister.getItems().get(newSelection));
            }
        }
    }

    private void reFilterRegisterName(boolean withoutSelect) {
        int index;
        String previousKey = "";
        if ((index = inputNameRegister.getSelectionModel().getSelectedIndex()) != -1)
            previousKey = listNameRegisterFiltered.get(index).number;

        ObservableList<ListItemNameRegister> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemNameRegister> list1 = FXCollections.observableArrayList();

        // filter Register Name
        for (ListItemNameRegister listItemNameRegister : listNameRegister)
            if (listItemNameRegister.toString().contains(inputNameRegister.getEditor().getText().trim()) ||
                    listItemNameRegister.getPronounce().contains(inputNameRegister.getEditor().getText().trim()) ||
                    matchSubConseq(inputNameRegister.getEditor().getText().trim(), listItemNameRegister.toString()))
                list0.add(listItemNameRegister);

        // filter by department
        if ((index = inputNameDepartment.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemNameRegister listItemNameRegister : list0)
                if (listItemNameRegister.department.equals(listNameDepartmentFiltered.get(index).number))
                    list1.add(listItemNameRegister);
            list0 = list1;
        }

        // filter by doctor name
        list1 = FXCollections.observableArrayList();
        if ((index = inputNameDoctor.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemNameRegister listItemNameRegister : list0)
                if (!listItemNameRegister.isSpecialist || listNameDoctorFiltered.get(index).isSpecialist)
                    list1.add(listItemNameRegister);
            list0 = list1;
        }

        // filter by register type
        list1 = FXCollections.observableArrayList();
        if ((index = inputTypeRegister.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemNameRegister listItemNameRegister : list0)
                if (listItemNameRegister.isSpecialist == listTypeRegisterFiltered.get(index).isSpecialist)
                    list1.add(listItemNameRegister);
            list0 = list1;
        }

        // add to filtered list and combobox list
        boolean isCurrentInputLegal = false;
        int counter = 0, newSelection = -1;
        listNameRegisterFiltered.clear();
        inputNameRegister.getItems().clear();
        for (ListItemNameRegister listItemNameRegister : list0) {
            listNameRegisterFiltered.add(listItemNameRegister);
            inputNameRegister.getItems().add(listItemNameRegister.toString());
            if (listItemNameRegister.toString().contains(inputNameRegister.getEditor().getText().trim()) ||
                    listItemNameRegister.getPronounce().contains(inputNameRegister.getEditor().getText().trim()) ||
                    matchSubConseq(inputNameRegister.getEditor().getText().trim(), listItemNameRegister.toString()))
                isCurrentInputLegal = true;
            if (previousKey.equals(listItemNameRegister.number))
                newSelection = counter;
            ++counter;
        }

        // delete illegal input
        if (!withoutSelect) {
            if (!isCurrentInputLegal)
                inputNameRegister.getEditor().clear();
            if (newSelection != -1) {
                inputNameRegister.getSelectionModel().clearAndSelect(newSelection);
                inputNameRegister.getEditor().setText(inputNameRegister.getItems().get(newSelection));
            }
        }
    }

    private boolean shouldSupressKeyCode(KeyCode code) {
        return code == KeyCode.DOWN ||
                code == KeyCode.UP ||
                code == KeyCode.ENTER;
        //||
        //code == KeyCode.DELETE ||
        //code == KeyCode.BACK_SPACE;
    }

    @FXML
    void buttonExitClicked() throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("../fxml/Login.fxml")));
        FXRobotHelper.getStages().get(0).setScene(scene);
        TimeManager.getInstance().stop("patient");
        TimeManager.getInstance().start("login");
    }
}

