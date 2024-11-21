package com.onlinestore.jdoulke.onlinestorefx.controllers;

import com.onlinestore.jdoulke.onlinestorefx.UserSession;
import com.onlinestore.jdoulke.onlinestorefx.Utils;
import com.onlinestore.jdoulke.onlinestorefx.database.DatabaseConnection;
import com.onlinestore.jdoulke.onlinestorefx.entities.GenericEntry;
import com.onlinestore.jdoulke.onlinestorefx.entities.User;
import com.onlinestore.jdoulke.onlinestorefx.controllers.LogEntry;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;


public class HomeController {

    @FXML
    private TableView<LogEntry> logTable;
    @FXML
    private TableView<GenericEntry> table_selection;
    @FXML
    private TableColumn<LogEntry, Integer> logIdColumn;
    @FXML
    private TableColumn<LogEntry, String> operationTypeColumn;
    @FXML
    private TableColumn<LogEntry, String> tableNameColumn;
    @FXML
    private TableColumn<LogEntry, Timestamp> operationTimeColumn;
    @FXML
    private TableColumn<LogEntry, String> performedByColumn;
    @FXML
    private TableColumn<LogEntry, String> oldDataColumn;
    @FXML
    private TableColumn<LogEntry, String> newDataColumn;
    @FXML
    private Label notificationLabel;
    @FXML
    private StackPane notificationPane;
    @FXML
    private Label welcome_label;
    @FXML
    private ComboBox selection_box;
    private static boolean firstTime = true;

    @FXML
    public void initialize() {
        User user = UserSession.getCurrentUser();

        if(firstTime) {
            String message = "Welcome " + user.getFirstName() + " " + user.getLastName() + "!";
            showPopupMessage(message, 3, "green", "white", false);
            setFirstTime();
        }
        welcome_label.setText("Welcome User: " + user.getFirstName() + " " + user.getLastName());

        logIdColumn.setCellValueFactory(new PropertyValueFactory<>("logId"));
        operationTypeColumn.setCellValueFactory(new PropertyValueFactory<>("operationType"));
        tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
        operationTimeColumn.setCellValueFactory(new PropertyValueFactory<>("operationTime"));
        performedByColumn.setCellValueFactory(new PropertyValueFactory<>("performedBy"));
        oldDataColumn.setCellValueFactory(new PropertyValueFactory<>("oldData"));
        newDataColumn.setCellValueFactory(new PropertyValueFactory<>("newData"));


        logTable.setItems(fetchLogEntries());

        selection_box.getItems().addAll(
                "Top Users by Sales",
                "Top Users by Revenue",
                "Top Customers",
                "Top Selling Products",
                "Largest Sales",
                "Best Sales Days"
        );

        selection_box.setValue("Top Users by Sales");
        handleSelection();

        selection_box.setOnAction(event -> handleSelection());


    }

    public void showPopupMessage(String message, int duration, String background, String textColor, boolean playSound) {

        Utils.popUpMessage(message, duration, background, textColor, playSound, notificationPane, notificationLabel);
    }

    public static void resetFirstTime() {
        firstTime = true;
    }

    public static void setFirstTime(){
        firstTime = false;
    }

    private ObservableList<LogEntry> fetchLogEntries() {
        ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();

        try {
            Connection dbConnection = DatabaseConnection.getConnection();

            CallableStatement getLogsStmt = dbConnection.prepareCall("{CALL get_logs(?)}");

            getLogsStmt.registerOutParameter(1, OracleTypes.CURSOR);

            getLogsStmt.execute();

            try (ResultSet resultSet = (ResultSet) getLogsStmt.getObject(1)) {
                while (resultSet.next()) {
                    logEntries.add(new LogEntry(
                            resultSet.getInt("log_id"),
                            resultSet.getString("operation_type"),
                            resultSet.getString("table_name"),
                            resultSet.getTimestamp("operation_time"),
                            resultSet.getString("performed_by"),
                            resultSet.getString("old_data"),
                            resultSet.getString("new_data")
                    ));
                }
            }

            getLogsStmt.close();
            dbConnection.close();

        } catch (SQLException e) {
            showPopupMessage("An error occurred while fetching logs.", 3, "red", "white", true);
            System.out.println(e.getMessage());
        }

        return logEntries;
    }

    private void handleSelection() {
        String selectedOption = (String) selection_box.getValue();
        if (selectedOption == null) return;

        switch (selectedOption) {
            case "Top Users by Sales":
                fetchDataFromProcedure("{CALL get_top_users_by_sales(?)}");
                break;
            case "Top Users by Revenue":
                fetchDataFromProcedure("{CALL get_top_users_by_revenue(?)}");
                break;
            case "Top Customers":
                fetchDataFromProcedure("{CALL get_top_customers(?)}");
                break;
            case "Top Selling Products":
                fetchDataFromProcedure("{CALL get_top_selling_products(?)}");
                break;
            case "Largest Sales":
                fetchDataFromProcedure("{CALL get_largest_sales(?)}");
                break;
            case "Best Sales Days":
                fetchDataFromProcedure("{CALL get_best_sales_days(?)}");
                break;
        }
    }

    private void fetchDataFromProcedure(String procedureCall) {
        ObservableList<GenericEntry> entries = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
             CallableStatement callableStatement = connection.prepareCall(procedureCall)) {

            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();

            try (ResultSet resultSet = (ResultSet) callableStatement.getObject(1)) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                configureTable(metaData);

                while (resultSet.next()) {
                    GenericEntry entry = new GenericEntry();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        entry.addData(metaData.getColumnName(i), resultSet.getString(i));
                    }
                    entries.add(entry);
                }
            }

            table_selection.setItems(entries);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void configureTable(ResultSetMetaData metaData) throws SQLException {
        table_selection.getColumns().clear();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            final String columnName = metaData.getColumnName(i);
            TableColumn<GenericEntry, String> column = new TableColumn<>(columnName);

            column.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getData(columnName))
            );

            table_selection.getColumns().add(column);
        }
    }



    @FXML
    public void exportToTxt() {
        try {
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Log File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            String currentDate =  LocalDate.now().toString();
            fileChooser.setInitialFileName("logs-" + currentDate + ".txt");

            
            File selectedFile = fileChooser.showSaveDialog(logTable.getScene().getWindow());

            if (selectedFile != null) {
                
                FileWriter writer = new FileWriter(selectedFile);
                for (LogEntry entry : logTable.getItems()) {
                    writer.write("Log ID: " + entry.getLogId() + "\n");
                    writer.write("Operation Type: " + entry.getOperationType() + "\n");
                    writer.write("Table Name: " + entry.getTableName() + "\n");
                    writer.write("Operation Time: " + entry.getOperationTime() + "\n");
                    writer.write("Performed By: " + entry.getPerformedBy() + "\n");
                    writer.write("Old Data: " + entry.getOldData() + "\n");
                    writer.write("New Data: " + entry.getNewData() + "\n");
                    writer.write("--------------------------------------------------\n");
                }
                writer.close();

                
                showPopupMessage("Logs exported successfully!", 3, "green", "white", false);

                
                Desktop.getDesktop().open(selectedFile);
            }
        } catch (IOException e) {
            
            showPopupMessage("An error occurred while exporting logs.", 3, "red", "white", true);
            System.out.println(e.getMessage());
        }
    }

    @FXML
    public void clearLogs() {
        try {

            Connection dbConnection = DatabaseConnection.getConnection();


            CallableStatement clearLogsStmt = dbConnection.prepareCall("{CALL clear_logs()}");


            clearLogsStmt.execute();


            logTable.getItems().clear();

            showPopupMessage("All logs have been deleted successfully!", 3, "green", "white", false);

            clearLogsStmt.close();
            dbConnection.close();

        } catch (SQLException e) {
            showPopupMessage("An error occurred while clearing logs.", 3, "red", "white", true);
            System.out.println(e.getMessage());
        }
    }


}
