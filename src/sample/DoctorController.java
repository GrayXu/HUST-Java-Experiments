package sample;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.sun.javafx.robot.impl.FXRobotHelper;
import db.Config;
import db.DataBase;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import sample.tool.DateConverter;
import sample.tool.TimeManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class DoctorController {
    private static final class Register extends RecursiveTreeObject<Register> {
        public StringProperty number;
        public StringProperty namePatient;
        public StringProperty dateTimeDisplay;
        public StringProperty isSpecialistDisplay;

        public Register(String number, String namePatient, Timestamp dateTime, boolean isSpecialist) {
            this.number = new SimpleStringProperty(number);
            this.namePatient = new SimpleStringProperty(namePatient);
            this.dateTimeDisplay = new SimpleStringProperty(dateTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            this.isSpecialistDisplay = new SimpleStringProperty(isSpecialist ? "专家号" : "普通号");
        }
    }

    /**
     * 窗口放置桌面中央
     *
     * @param c component waited to be reset
     */
    private void setCenter(Component c) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        SCREEN_WIDTH = dim.width;
        SCREEN_HEIGHT = dim.height;
        c.setLocation((SCREEN_WIDTH - c.getWidth()) / 2, (SCREEN_HEIGHT - c.getHeight()) / 2);
        c.setVisible(true);
    }

    private static final class Income extends RecursiveTreeObject<Income> {
        public StringProperty departmentName;
        public StringProperty doctorNumber;
        public StringProperty doctorName;
        public StringProperty registerType;
        public StringProperty registerPopulation;
        public StringProperty incomeSum;

        public Income(String depName, String docNum, String docName, boolean isSpec, int regNumPeople, Double incomSum) {
            this.departmentName = new SimpleStringProperty(depName);
            this.doctorNumber = new SimpleStringProperty(docNum);
            this.doctorName = new SimpleStringProperty(docName);
            this.registerType = new SimpleStringProperty(isSpec ? "专家号" : "普通号");
            this.registerPopulation = new SimpleStringProperty(Integer.toString(regNumPeople));
            this.incomeSum = new SimpleStringProperty(String.format("%.2f", incomSum));
        }
    }

    // set by LoginController
    public static String doctorName;
    public static String doctorNumber;

    private static int SCREEN_WIDTH;
    private static int SCREEN_HEIGHT;

    @FXML
    private Label labelUpdate;
    @FXML
    private Label labelWelcome;
    @FXML
    private JFXDatePicker pickerDateStart;
    @FXML
    private JFXDatePicker pickerDateEnd;
    @FXML
    private JFXTimePicker pickerTimeStart;
    @FXML
    private JFXTimePicker pickerTimeEnd;

    @FXML
    private JFXTabPane mainPane;
    @FXML
    private GridPane rootPanel;
    @FXML
    private Tab tabRegister;
    @FXML
    private Tab tabIncome;

    @FXML
    private JFXTreeTableView<Register> tableRegister;
    @FXML
    private TreeTableColumn<Register, String> columnRegisterNumber;
    @FXML
    private TreeTableColumn<Register, String> columnRegisterPatientName;
    @FXML
    private TreeTableColumn<Register, String> columnRegisterDateTime;
    @FXML
    private TreeTableColumn<Register, String> columnRegisterType;
    private TreeItem<Register> rootRegister;

    @FXML
    private JFXTreeTableView<Income> tableIncome;
    @FXML
    private TreeTableColumn<Income, String> columnIncomeDepartmentName;
    @FXML
    private TreeTableColumn<Income, String> columnIncomeDoctorNumber;
    @FXML
    private TreeTableColumn<Income, String> columnIncomeDoctorName;
    @FXML
    private TreeTableColumn<Income, String> columnIncomeRegisterType;
    @FXML
    private TreeTableColumn<Income, String> columnIncomeRegisterPopulation;
    @FXML
    private TreeTableColumn<Income, String> columnIncomeSum;
    private TreeItem<Income> rootIncome;

    private ObservableList<Register> listRegister = FXCollections.observableArrayList();
    private ObservableList<Income> listIncome = FXCollections.observableArrayList();

    @FXML
    JFXCheckBox checkBoxAllTime;
    @FXML
    JFXCheckBox checkBoxToday;
    @FXML
    JFXButton buttonFilter;
    @FXML
    Label labelDoctorTime;

    private static boolean isPressedControl = false;

    @FXML
    void initialize() {

        rootPanel.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.CONTROL) {
                isPressedControl = false;
            } else if (e.getCode() == KeyCode.U && isPressedControl) {
                buttomFilterPressed();
            } else if (e.getCode() == KeyCode.L && isPressedControl) {
                try {
                    buttonExitClicked();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else if (e.getCode() == KeyCode.C && isPressedControl) {
                buttonChartPressed();
            } else if (e.getCode() == KeyCode.A && isPressedControl) {
                if (checkBoxAllTime.isSelected()) {
                    checkBoxAllTime.setSelected(false);
                } else {
                    checkBoxAllTime.setSelected(true);
                }
                checkBoxAllTimeSelected();
            } else if (e.getCode() == KeyCode.T && isPressedControl) {
                if (checkBoxToday.isSelected()) {
                    checkBoxToday.setSelected(false);
                } else {
                    checkBoxToday.setSelected(true);
                }
                checkBoxTodaySelected();
            }
        });

        rootPanel.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.CONTROL) {
                isPressedControl = true;
            }
        });

        rootPanel.setBackground(new Background(new BackgroundFill(Color.color(1, 1, 0.97), null, null)));

        labelWelcome.setText(String.format("Welcome, %s", doctorName));

        // set two date converter (formatter)
        pickerDateStart.setConverter(new DateConverter());
        pickerDateEnd.setConverter(new DateConverter());
        // default to current date
        pickerDateStart.setValue(LocalDate.now());
        pickerDateEnd.setValue(LocalDate.now());

        // set time selector to 24h
        pickerTimeStart.setIs24HourView(true);
        pickerTimeEnd.setIs24HourView(true);
        // default to 00:00 to 23:59
        pickerTimeStart.setValue(LocalTime.MIN);
        pickerTimeEnd.setValue(LocalTime.MAX);

        /**
         * bindLableThread instance from factory pattern
         */
        columnRegisterNumber.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Register, String> param) -> param.getValue().getValue().number);
        columnRegisterPatientName.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Register, String> param) -> param.getValue().getValue().namePatient);
        columnRegisterDateTime.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Register, String> param) -> param.getValue().getValue().dateTimeDisplay);
        columnRegisterType.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Register, String> param) -> param.getValue().getValue().isSpecialistDisplay);

        rootRegister = new RecursiveTreeItem<>(listRegister, RecursiveTreeObject::getChildren);
        tableRegister.setRoot(rootRegister);

        columnIncomeDepartmentName.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().departmentName);
        columnIncomeDoctorNumber.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().doctorNumber);
        columnIncomeDoctorName.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().doctorName);
        columnIncomeRegisterType.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().registerType);
        columnIncomeRegisterPopulation.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().registerPopulation);
        columnIncomeSum.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().incomeSum);

        rootIncome = new RecursiveTreeItem<>(listIncome, RecursiveTreeObject::getChildren);
        tableIncome.setRoot(rootIncome);

        // init time label
        TimeManager.bindLableThread("doctor", labelDoctorTime);
        TimeManager.getInstance().start("doctor");

    }

    private void showRegResult(ResultSet[] result) {
        //update panel
        try {
            listRegister.clear();
            long startTime = System.currentTimeMillis();
            while (result[0].next()) {
                listRegister.add(new Register(
                        result[0].getString(Config.NameTableColumnRegisterNumber),
                        result[0].getString(Config.NameTableColumnPatientName),
                        result[0].getTimestamp(Config.NameTableColumnRegisterDateTime),
                        result[0].getBoolean(Config.NameTableColumnCategoryRegisterIsSpecialist)
                ));
            }
            long endTime = System.currentTimeMillis();
            System.out.println("渲染时间:" + (endTime - startTime) + "ms");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        labelUpdate.setText("update successfully");
    }

    private void showIncomeResult(ResultSet[] result) {
        listIncome.clear();
        long startTime = System.currentTimeMillis();
        try {
            while (result[0].next()) {
                listIncome.add(new Income(
                                result[0].getString("depname"),
                                result[0].getString(Config.NameTableColumnDoctorNumber),
                                result[0].getString("docname"),
                                result[0].getBoolean(Config.NameTableColumnCategoryRegisterIsSpecialist),
                                result[0].getInt(Config.NameTableColumnRegisterCurrentRegisterCount),
                                result[0].getDouble("sum")
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("渲染时间:" + (endTime - startTime) + "ms");
        labelUpdate.setText("update successfully");
    }

    /**
     * mode: year month year
     *
     * @param panel
     * @param dataset
     */
    private void drawChart(JPanel panel, CategoryDataset dataset, String target) {
        panel.removeAll();
//        String horizon = "Income";
        JFreeChart chart = ChartFactory.createLineChart(target, "Time", "", dataset, PlotOrientation.VERTICAL, false, false, false);

        //设置字体
        CategoryPlot plot = chart.getCategoryPlot();//获取图表区域对象

        CategoryAxis domainAxis = plot.getDomainAxis();         //水平底部列表
        domainAxis.setLabelFont(new Font("黑体", Font.BOLD, 14));         //水平底部标题
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
        domainAxis.setTickLabelFont(new Font("宋体", Font.BOLD, 12));  //垂直标题
        ValueAxis rangeAxis = plot.getRangeAxis();//获取柱状
        rangeAxis.setLabelFont(new Font("黑体", Font.BOLD, 15));
        chart.getTitle().setFont(new Font("宋体", Font.BOLD, 20));//设置标题字体
        ChartPanel chartP = new ChartPanel(chart);
        chartP.setOpaque(false);
        panel.add(chartP);
        chartP.updateUI();
        panel.updateUI();

    }

    //为图表赋值
    private CategoryDataset getChartData(String startTime, String endTime, String target, String mode) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ArrayList<ArrayList<String>> dataLists = DataBase.getInstance().ResultSet2TwoDimList(DataBase.getInstance().getChartInfo(startTime, endTime, target, mode));

            for (ArrayList<String> list : dataLists) {
                int length = list.size();
                double value = Double.parseDouble(list.get(length - 1));
                String columnKey = list.get(0);
                dataset.addValue(value, "gray", columnKey);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库获取数据出错");
        }

//        lock = true;
        return dataset;
    }


    @FXML
    private void buttonChartPressed() {
        System.out.println("buttonChartPressed");
//        if (mainPane.getSelectionModel().getSelectedItem() == tabRegister) {
        JFrame jFrame = new JFrame();

        JPanel contPane = new JPanel(new BorderLayout());
        jFrame.setContentPane(contPane);

        JComboBox<String> jComboxMode = new JComboBox<>(new String[]{"Year", "Month", "Week", "Day"});
        JComboBox<String> jComboxTarget = new JComboBox<>(new String[]{"Income", "Register Count"});
        JPanel argPanel = new JPanel(new BorderLayout());

        JPanel jPOM = new JPanel();
        jPOM.add(jComboxMode);
        JPanel jPOT = new JPanel();
        jPOT.add(jComboxTarget);

        argPanel.add(jPOT, BorderLayout.WEST);
        argPanel.add(jPOM, BorderLayout.EAST);

        contPane.add(argPanel, BorderLayout.NORTH);

        JPanel chartPanel = new JPanel();
        contPane.add(chartPanel, BorderLayout.CENTER);

        jComboxMode.addActionListener(e -> updateChart(((String) jComboxMode.getSelectedItem()), ((String) jComboxTarget.getSelectedItem()), chartPanel));
        jComboxTarget.addActionListener(e -> updateChart(((String) jComboxMode.getSelectedItem()), ((String) jComboxTarget.getSelectedItem()), chartPanel));

        jFrame.setTitle("Analysis Chart");
        jFrame.setSize(new Dimension(800, 600));
        jFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setCenter(jFrame);
        jFrame.setVisible(true);

        updateChart(((String) jComboxMode.getSelectedItem()), ((String) jComboxTarget.getSelectedItem()), chartPanel);
//        }
    }

    private void updateChart(String mode, String target, JPanel argPanel) {
        if (checkBoxAllTime.isSelected()) {
            new Thread(() -> {
                CategoryDataset dataset = getChartData(
                        "0000-00-00 00:00:00",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        target,
                        mode);
                Platform.runLater(() -> drawChart(argPanel, dataset, target));

            }).start();
        } else if (checkBoxToday.isSelected()) {
            new Thread(() -> {
                CategoryDataset dataset = getChartData(
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        target,
                        mode);
                Platform.runLater(() -> drawChart(argPanel, dataset, target));

            }).start();
        } else {
            new Thread(() -> {
                CategoryDataset dataset = getChartData(
                        pickerDateStart.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + pickerTimeStart.getValue().format(DateTimeFormatter.ofPattern(" HH:mm:ss")),
                        pickerDateEnd.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + pickerTimeEnd.getValue().format(DateTimeFormatter.ofPattern(" HH:mm:ss")),
                        target,
                        mode);
                Platform.runLater(() -> drawChart(argPanel, dataset, target));

            }).start();
        }
    }

    /**
     * Update Button Pressed recall function
     */
    @FXML
    private void buttomFilterPressed() {
        System.out.println("buttomFilterPressed");
        labelUpdate.setText("Pleas wait...");
        if (mainPane.getSelectionModel().getSelectedItem() == tabRegister) {
            final ResultSet[] result = new ResultSet[1];
            if (checkBoxAllTime.isSelected()) {
                new Thread(() -> {
                    long startTime = System.currentTimeMillis();
                    result[0] = DataBase.getInstance().getRegisterForDoctor(
                            doctorNumber,
                            "0000-00-00 00:00:00",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                    long endTime = System.currentTimeMillis();
                    System.out.println("查询时间:" + (endTime - startTime) + "ms");
                    Platform.runLater(() -> {
                        showRegResult(result);
                    });
                }).start();

            } else if (checkBoxToday.isSelected()) {
                new Thread(() -> {
                    result[0] = DataBase.getInstance().getRegisterForDoctor(
                            doctorNumber,
                            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    Platform.runLater(() -> {
                        showRegResult(result);
                    });
                }).start();
            } else {
                new Thread(() -> {
                    result[0] = DataBase.getInstance().getRegisterForDoctor(
                            doctorNumber,
                            pickerDateStart.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                    pickerTimeStart.getValue().format(DateTimeFormatter.ofPattern(" HH:mm:ss")),
                            pickerDateEnd.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                    pickerTimeEnd.getValue().format(DateTimeFormatter.ofPattern(" HH:mm:ss")));
                    Platform.runLater(() -> {
                        showRegResult(result);
                    });
                }).start();
            }

            /**
             * Income界面
             */
        } else if (mainPane.getSelectionModel().getSelectedItem() == tabIncome) {
            final ResultSet[] result = new ResultSet[1];
            if (checkBoxAllTime.isSelected()) {
                //异步更新
                new Thread(() -> {
                    long startTime = System.currentTimeMillis();
                    result[0] = DataBase.getInstance().getIncomeInfo(
                            "0000-00-00 00:00:00",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    );
                    long endTime = System.currentTimeMillis();
                    System.out.println("查询时间:" + (endTime - startTime) + "ms");
                    Platform.runLater(() -> {
                        showIncomeResult(result);
                    });
                }).start();

            } else if (checkBoxToday.isSelected()) {
                new Thread(() -> {
                    result[0] = DataBase.getInstance().getIncomeInfo(
                            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    );
                    Platform.runLater(() -> {
                        //更新JavaFX的主线程的代码放在此处
                        showIncomeResult(result);
                    });
                }).start();

            } else {
                new Thread(() -> {
                    //更新JavaFX的主线程的代码放在此处
                    result[0] = DataBase.getInstance().getIncomeInfo(
                            pickerDateStart.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                    pickerTimeStart.getValue().format(DateTimeFormatter.ofPattern(" HH:mm:ss")),
                            pickerDateEnd.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                    pickerTimeEnd.getValue().format(DateTimeFormatter.ofPattern(" HH:mm:ss"))
                    );
                    Platform.runLater(() -> {
                        showIncomeResult(result);
                    });
                }).start();
            }
        }
    }

    @FXML
    private void tabSelectionChanged(Event event) {
        if (((Tab) (event.getTarget())).isSelected()) ;
    }

    @FXML
    private void buttonExitClicked() throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("../fxml/Login.fxml")));
        FXRobotHelper.getStages().get(0).setScene(scene);
        TimeManager.getInstance().start("login");
        TimeManager.getInstance().stop("doctor");
    }

    @FXML
    void checkBoxAllTimeSelected() {
        if (checkBoxAllTime.isSelected()) {
            checkBoxToday.setSelected(false);
            pickerDateStart.setDisable(true);
            pickerDateEnd.setDisable(true);
            pickerTimeStart.setDisable(true);
            pickerTimeEnd.setDisable(true);
        } else if (!checkBoxToday.isSelected()) {
            pickerDateStart.setDisable(false);
            pickerDateEnd.setDisable(false);
            pickerTimeStart.setDisable(false);
            pickerTimeEnd.setDisable(false);
        }
    }

    @FXML
    void checkBoxTodaySelected() {
        if (checkBoxToday.isSelected()) {
            checkBoxAllTime.setSelected(false);
            pickerDateStart.setDisable(true);
            pickerDateEnd.setDisable(true);
            pickerTimeStart.setDisable(true);
            pickerTimeEnd.setDisable(true);
        } else if (!checkBoxAllTime.isSelected()) {
            pickerDateStart.setDisable(false);
            pickerDateEnd.setDisable(false);
            pickerTimeStart.setDisable(false);
            pickerTimeEnd.setDisable(false);
        }
    }
}

