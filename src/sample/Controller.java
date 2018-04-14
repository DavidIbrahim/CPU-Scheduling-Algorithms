package sample;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.GanttChart.ExtraData;
import sample.CpuSimulate.CpuSchedule;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.converter.IntegerStringConverter;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {

    public static final int FCFS = 0;
    public static final int SJF = 1;
    public static final int PrioritySch = 2;
    public static final int RoundRobin = 3;
    private static int currentAlgorithm = 0;
    private static int numberOfProcess = 0;
    private static int quantumTime = 1;
    public MenuItem menuItemSave;
    public MenuItem menuItemImport;
    private int animationSpeed = 1200;


    public TextField textFieldQuantumTime;
    public HBox hBoxQuantumTime;
    public CheckBox checkBoxPerimitive;
    public Button buttonStartSimulation;


    public VBox vboxParent;
    public GanttChart<Number, String> chart;
    public NumberAxis xAxis;
    public CategoryAxis yAxis;
    public HBox hboxChart;
    public HBox hboxPreemptive;
    public Text textAverageWaitingTime;
    public Text textTurnAroundTime;
    public Text textCounter;
    public BorderPane borderPane;
    public Slider sliderAnimationSpeed;
    private ObservableList<CpuProcess> processesList = FXCollections.observableArrayList();


    @FXML
    public TableView<CpuProcess> tableProcessData;

    public TableColumn<CpuProcess, Integer> columnArrivalTime;
    public TableColumn<CpuProcess, Integer> columnPriority;
    public TableColumn<CpuProcess, Integer> columnProcessNo;
    public TableColumn<CpuProcess, Integer> columnCpuBurst;

    public TextField textFieldProcessNo;

    @FXML
    private CheckBox checkBoxArrivalTIme;


    ObservableList<String> scheduleAlgoritms = FXCollections.observableArrayList("FCFS", "SJF", "Priority", "Round Robin");
    @FXML
    private ChoiceBox choiceBoxAlg;
    private static Boolean isPreemptive = false;

    String[] colors = new String[]{"status-red", "status-green", "status-blue", "status-tomato",
            "status-violet", "status-purple", "status-yellow", "status-brown", "status-black","status-purple2"};
    String[] colorsHex = new String[]{"#800000", "#008000", "#000080", "#ff6347", "#ee82ee", "#6a5acd", "#ffff47"
            , "#996600", "#000000","#993366"};

    private void initalizeChoiceBox() {
        choiceBoxAlg.setValue("FCFS");
        choiceBoxAlg.setItems(scheduleAlgoritms);
        choiceBoxAlg.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                currentAlgorithm = newValue.intValue();
                if (currentAlgorithm == PrioritySch) {
                    columnPriority.setVisible(true);
                    hboxPreemptive.setVisible(true);
                } else {

                    columnPriority.setVisible(false);
                }

                if (currentAlgorithm == RoundRobin) {
                    hBoxQuantumTime.setVisible(true);
                    hboxPreemptive.setVisible(false);
                } else {
                    hBoxQuantumTime.setVisible(false);
                }

                if (currentAlgorithm == SJF)
                    hboxPreemptive.setVisible(true);


            }
        });
    }


    private void initializeCheckBoxes() {

        checkBoxArrivalTIme.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue == true)
                    columnArrivalTime.setVisible(true);
                else {
                    columnArrivalTime.setVisible(false);
                    for (CpuProcess process:processesList
                         ) {
                        process.setArrivalTime(0);
                    }
                }

            }
        });

        checkBoxPerimitive.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                isPreemptive = newValue;
            }
        });

    }

    private void initalizetextFields() {
        textCounter.setVisible(false);

        textFieldProcessNo.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue) {
                    int oldNumberOfPrpcess = numberOfProcess;
                    try {
                        numberOfProcess = Integer.valueOf(textFieldProcessNo.getText());
                    } catch (Exception e) {
                        numberOfProcess = 0;
                    }
                    System.out.println(numberOfProcess);
                    addRows(numberOfProcess - oldNumberOfPrpcess);
                }

            }
        });
    }


    private void addRows(int numberOfRows) {
        if (numberOfRows == 0) {
            return;
        }
        int previousSize = processesList.size();
        for (int i = 0; i < Math.abs(numberOfRows); i++) {
            if(numberOfRows>0)
               processesList.add(new CpuProcess(i + previousSize));
            else
                processesList.remove(processesList.size()-1);

        }
        tableProcessData.setItems(processesList);
    }

    private void initalizeTable() {
        tableProcessData.setEditable(true);


        //initalize Process Column
        columnProcessNo = new TableColumn<>("Process");
        columnProcessNo.setVisible(true);
        columnProcessNo.setMinWidth(90);
        columnProcessNo.setCellValueFactory(new PropertyValueFactory<>("processId"));

        // Initialize Burst Column
        columnCpuBurst = new TableColumn<>("CPU Burst");
        columnCpuBurst.setVisible(true);
        columnCpuBurst.setMinWidth(90);
        columnCpuBurst.setCellValueFactory(new PropertyValueFactory<>("cpuBurstTime"));
        columnCpuBurst.setCellFactory(TextFieldTableCell.<CpuProcess, Integer>forTableColumn(new IntegerStringConverter()));
        columnCpuBurst.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<CpuProcess, Integer>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<CpuProcess, Integer> event) {
                event.getRowValue().setCpuBurstTime(event.getNewValue());
            }
        });


        //Initialize Priority column

        columnPriority = new TableColumn<>("Priority");
        columnPriority.setVisible(false);
        columnPriority.setMinWidth(90);
        columnPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        columnPriority.setSortable(false);
        columnPriority.setMinWidth(Control.USE_PREF_SIZE);
        columnPriority.setCellFactory(TextFieldTableCell.<CpuProcess, Integer>forTableColumn(new IntegerStringConverter()));
        columnPriority.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<CpuProcess, Integer>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<CpuProcess, Integer> event) {
                event.getRowValue().setPriority(event.getNewValue());
            }
        });


        //initalize Arrival time column
        columnArrivalTime = new TableColumn<>("Arrival");
        columnArrivalTime.setMinWidth(90);
        columnArrivalTime.setVisible(false);
        columnArrivalTime.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        columnArrivalTime.setCellFactory(TextFieldTableCell.<CpuProcess, Integer>forTableColumn(new IntegerStringConverter()));
        columnArrivalTime.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<CpuProcess, Integer>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<CpuProcess, Integer> event) {
                event.getRowValue().setArrivalTime(event.getNewValue());
            }
        });


        tableProcessData.getColumns().addAll(columnProcessNo, columnCpuBurst, columnArrivalTime, columnPriority);

        tableProcessData.setMaxWidth(Control.USE_COMPUTED_SIZE);
        tableProcessData.setMinWidth(Control.USE_COMPUTED_SIZE);

        // single cell selection mode
        tableProcessData.getSelectionModel().setCellSelectionEnabled(true);


    }

    private void initializeHbox() {
        hboxPreemptive.setVisible(false);
        hBoxQuantumTime.setVisible(false);
        textFieldQuantumTime.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    quantumTime = Integer.valueOf(newValue);
                } catch (Exception e) {
                    quantumTime = 1;
                }
                System.out.println(quantumTime);
            }
        });
    }

    private void initializeButtons() {

        buttonStartSimulation.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ArrayList<CpuProcess> cpuProcesses = new ArrayList<>();

                for (CpuProcess process : processesList) {
                    cpuProcesses.add(new CpuProcess(process.getProcessId(), process.getCpuBurstTime(), process.getArrivalTime(), process.getPriority()));
                }

                CpuSimulate cpuSimulate = new CpuSimulate(cpuProcesses);
                CpuSchedule schedule = null;
                if (currentAlgorithm == FCFS) {
                    schedule = cpuSimulate.simulateFCFS();

                } else if (currentAlgorithm == SJF) {
                    schedule = cpuSimulate.simulateSJF(isPreemptive);
                } else if (currentAlgorithm == PrioritySch) {
                    schedule = cpuSimulate.simulatePriority(isPreemptive);
                } else if (currentAlgorithm == RoundRobin) {
                    schedule = cpuSimulate.simulateRoundRobin(quantumTime);
                }

                buildGanttChart(schedule);

            }
        });
    }

    private void setAverageWaitingAndTurnAroundTime(CpuSchedule schedule) {
        int numberOfProcesses = processesList.size();
        ArrayList<Integer> departureTime = new ArrayList<>(numberOfProcesses);
        for (int i = 0; i < numberOfProcesses; i++) {
            departureTime.add(0);
        }

        for (int i = 0; i < numberOfProcesses; i++) {
            for (int j = schedule.finishTimes.size() - 1; j >= 0; j--) {
                if (schedule.processIDs.get(j) == i) {
                    departureTime.set(i, schedule.finishTimes.get(j));
                    break;
                }


            }
        }
        double averageWaitingTime = 0;
        double averageTurnAroundTime = 0;
        for (int i = 0; i < numberOfProcesses; i++) {
            if (processesList.get(i).getCpuBurstTime() != 0) {
                averageWaitingTime += departureTime.get(i) - processesList.get(i).getArrivalTime() - processesList.get(i).getCpuBurstTime();
                averageTurnAroundTime += departureTime.get(i) - processesList.get(i).getArrivalTime();
            }
        }
        averageTurnAroundTime /= numberOfProcesses;
        averageWaitingTime /= numberOfProcesses;
        textAverageWaitingTime.setText("Average Waiting Time = " + averageWaitingTime);
        textTurnAroundTime.setText("Average TurnAround Time = " + averageTurnAroundTime);


    }

    private void buildGanttChart(CpuSchedule schedule) {
        chart.getData().clear();
        hboxChart.setMaxWidth(vboxParent.getWidth()-300);
        hboxChart.setMinWidth(vboxParent.getWidth()-300 );
        chart.setMinWidth(vboxParent.getWidth()-300);
        chart.setMaxWidth(vboxParent.getWidth());


        XYChart.Series series = new XYChart.Series();


        double lastFinish = schedule.finishTimes.get(schedule.finishTimes.size() - 1);
        xAxis.setUpperBound(lastFinish);
        if (lastFinish < 20) {
            xAxis.setTickUnit(1);

        } else if (lastFinish < 30) {
            xAxis.setTickUnit(2);
        } else {
            xAxis.setTickUnit(5);
        }

        chart.getData().add(series);
        /*      for (int i = 0; i < schedule.startTimes.size(); i++) {
         *//*  double currentStart = schedule.startTimes.get(i);
            double currentfinish = schedule.finishTimes.get(i);


            series.getData().add(new XYChart.Data(currentStart, "",
                    new ExtraData((int) (currentfinish - currentStart), colors[schedule.processIDs.get(i)])));*//*



            try {
                TimeUnit.MILLISECONDS.sleep(100);
                System.out.println("david");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }*/
        final ScheduledExecutorService scheduler
                = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
                new Runnable() {

                    int counter = -1;

                    @Override
                    public void run() {
                        runsimulation();
                        counter++;
                        if (counter < schedule.startTimes.size()) {

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    buildGanttChartHpFn(schedule, counter, series);

                                }
                            });


                        } else {
                            endSimulation();
                            setAverageWaitingAndTurnAroundTime(schedule);
                            scheduler.shutdown();

                         }

                    }
                },
                1,
                animationSpeed,TimeUnit.MILLISECONDS);


        VBox vBoxLegend = new VBox();
        HBox hBox = new HBox();
        int i = 0;
        while (i < processesList.size()) {
            CpuProcess process = processesList.get(i);
            Rectangle rectangle = new Rectangle(20, 20);
            rectangle.setFill(Paint.valueOf(colorsHex[process.getProcessId()]));
            Label label = new Label("P" + process.getProcessId());
            label.setFont(new Font("Arial", 11));
            label.setPadding(new Insets(0, 10, 0, 10));

            if (i % 2 == 0) {
                hBox = new HBox(label, rectangle);
                hBox.setPadding(new Insets(2));
            } else {
                hBox.getChildren().addAll(label, rectangle);
                vBoxLegend.getChildren().addAll(hBox);
            }


            i++;
        }
        if (vBoxLegend.getChildren().indexOf(hBox) == -1)
            vBoxLegend.getChildren().add(hBox);

        borderPane.setRight(vBoxLegend);

        /*if (hboxChart.getChildren().size() == 2) {
            hboxChart.getChildren().set(1, vBoxLegend);
        } else {
            hboxChart.getChildren().add(vBoxLegend);

        }*/


    }

    private void endSimulation() {
        textCounter.setWrappingWidth(0);
        textCounter.setVisible(false);

        buttonStartSimulation.setVisible(true);
        buttonStartSimulation.setMaxWidth(Region.USE_PREF_SIZE);
        textAverageWaitingTime.setVisible(true);
        textTurnAroundTime.setVisible(true);
        sliderAnimationSpeed.setDisable(false);

    }

    private void runsimulation() {
        textCounter.setVisible(true);
        textCounter.setWrappingWidth(50);
        buttonStartSimulation.setVisible(false);
        buttonStartSimulation.setMaxWidth(0);
        textTurnAroundTime.setVisible(false);
        textAverageWaitingTime.setVisible(false);
        sliderAnimationSpeed.setDisable(true);


    }

    private void buildGanttChartHpFn(CpuSchedule schedule, int i, XYChart.Series series) {
        int currentStart = schedule.startTimes.get(i);
        int currentfinish = schedule.finishTimes.get(i);
        textCounter.setText(String.valueOf(currentfinish));
        textCounter.setFill(Paint.valueOf(colorsHex[schedule.processIDs.get(i)]));


        series.getData().add(new XYChart.Data(currentStart, "",
                new ExtraData((int) (currentfinish - currentStart), colors[schedule.processIDs.get(i)])));
    }

    private void initializeganttChart() {


        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();

        chart = new GanttChart<Number, String>(xAxis, yAxis);
        xAxis.setLabel("");
        xAxis.setTickLabelFill(Color.CHOCOLATE);
        xAxis.setMinorTickCount(1);

        xAxis.setAutoRanging(false);

        yAxis.setLabel("");
        yAxis.setTickLabelFill(Color.CHOCOLATE);
        yAxis.setTickLabelGap(10);

        //chart.setTitle("Machine Monitoring");
        chart.setLegendVisible(false);
        chart.setBlockHeight(50);


        URL url = this.getClass().getResource("ganttchart.css");
        if (url == null) {
            System.out.println("Resource not found. Aborting.");
            System.exit(-1);
        }
        String css = url.toExternalForm();

        chart.setBlockHeight(040);
        chart.getStylesheets().add(css);
        chart.setAnimated(true);


        hboxChart = new HBox(chart);

        borderPane.setCenter(hboxChart);
        hboxChart.setMaxWidth(700);
        hboxChart.setMaxWidth(vboxParent.getMaxWidth() );
        chart.setMinWidth(700);
        chart.setMaxWidth(vboxParent.getMaxWidth());
        chart.setLegendVisible(false);


    }

    private void initalizeMenu() {
        menuItemSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save As Csv");
                fileChooser.setInitialFileName("table.csv");
                File file = fileChooser.showSaveDialog(new Stage());

                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String header =  "Process,CPU Burst,Arrival,Priority\n";


                for (CpuProcess process:
                        processesList) {
                    header += String.valueOf(process.getProcessId())+ ','+String.valueOf(process.getCpuBurstTime())
                            +','+String.valueOf(process.getArrivalTime())+','+String.valueOf(process.getPriority())+'\n';

                }

                pw.write(header);

                pw.close();
                System.out.println("done!");
            }
        });

        menuItemImport.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                processesList.clear();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Csv File");

                File file = fileChooser.showOpenDialog(new Stage());

                String line = "";
                String cvsSplitBy = ",";
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file.getAbsolutePath().toString()));
                    while ((line = br.readLine()) != null) {

                        // use comma as separator
                        String[] s = line.split(cvsSplitBy);
                        if(!s[0].equals("Process")) {
                            CpuProcess process = new CpuProcess(Integer.valueOf(s[0]),Integer.valueOf(s[1]),
                                    Integer.valueOf(s[2]),Integer.valueOf(s[3]));
                            processesList.add(process);
                        }
                    }
                    tableProcessData.setItems(processesList);
                    checkBoxArrivalTIme.setSelected(true);
                    numberOfProcess = processesList.size();
                    textFieldProcessNo.setText(String.valueOf(numberOfProcess));
                    choiceBoxAlg.setValue("Priority");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    private void initializeSlider() {
        sliderAnimationSpeed.setMax(10);
        sliderAnimationSpeed.setMin(0);
        sliderAnimationSpeed.setBlockIncrement(1);
        sliderAnimationSpeed.setValue(1);
        sliderAnimationSpeed.setShowTickLabels(true);
        sliderAnimationSpeed.setShowTickMarks(false);
        sliderAnimationSpeed.setMajorTickUnit(2);
        sliderAnimationSpeed.setMinorTickCount(2);
        sliderAnimationSpeed.setBlockIncrement(2);
        sliderAnimationSpeed.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int newValueInt = newValue.intValue();
                if(newValueInt==0) animationSpeed = 1500;
                else {
                    animationSpeed = 1500/newValueInt;
                }
            }
        });
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSlider();
        initalizeTable();
        initalizeChoiceBox();
        initializeCheckBoxes();
        initalizetextFields();
        initializeHbox();
        initializeButtons();
        initializeganttChart();
        initalizeMenu();

    }


}
