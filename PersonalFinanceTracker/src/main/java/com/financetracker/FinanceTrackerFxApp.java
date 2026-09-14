package com.financetracker;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

public class FinanceTrackerFxApp extends Application {

    private static final String DATA_FILE = "data/transactions.csv";

    private final FinanceTracker tracker = new FinanceTracker();
    private final FileManager fileManager = new FileManager(DATA_FILE);
    private final ObservableList<Transaction> tableItems = FXCollections.observableArrayList();

    private final Label balanceValue = new Label("$0.00");
    private final Label incomeValue = new Label("$0.00");
    private final Label expenseValue = new Label("$0.00");
    private final Label statusLabel = new Label();

    private final TableView<Transaction> table = new TableView<>();
    private final VBox categoryBox = new VBox(8);

    @Override
    public void start(Stage stage) {
        loadData();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(buildHeader());
        root.setCenter(buildContent());

        Scene scene = new Scene(root, 980, 680);
        scene.getStylesheets().add(
                getClass().getResource("/com/financetracker/finance-tracker.css").toExternalForm()
        );

        stage.setTitle("Personal Finance Tracker");
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();

        refreshAll();
    }

    private VBox buildHeader() {
        Label title = new Label("Personal Finance Tracker");
        title.getStyleClass().add("title");

        VBox header = new VBox(title);
        header.getStyleClass().add("header");
        return header;
    }

    private VBox buildContent() {
        HBox summaryCards = new HBox(14,
                createSummaryCard("Balance", balanceValue, "balance-value"),
                createSummaryCard("Income", incomeValue, "income-value"),
                createSummaryCard("Expenses", expenseValue, "expense-value")
        );
        summaryCards.setFillHeight(true);
        HBox.setHgrow(summaryCards.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(summaryCards.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(summaryCards.getChildren().get(2), Priority.ALWAYS);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Transactions", buildTransactionsTab()));
        tabs.getTabs().add(new Tab("Summary", buildSummaryTab()));
        VBox.setVgrow(tabs, Priority.ALWAYS);

        VBox content = new VBox(18, summaryCards, tabs);
        content.setPadding(new Insets(20));
        return content;
    }

    private VBox createSummaryCard(String heading, Label value, String valueStyle) {
        Label label = new Label(heading);
        label.getStyleClass().add("card-label");
        value.getStyleClass().addAll("card-value", valueStyle);

        VBox card = new VBox(6, label, value);
        card.getStyleClass().add("summary-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox buildTransactionsTab() {
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        ComboBox<TransactionType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(TransactionType.INCOME, TransactionType.EXPENSE);
        typeBox.setValue(TransactionType.EXPENSE);
        typeBox.setMaxWidth(Double.MAX_VALUE);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.add(new Label("Description"), 0, 0);
        form.add(descriptionField, 0, 1);
        form.add(new Label("Category"), 1, 0);
        form.add(categoryField, 1, 1);
        form.add(new Label("Amount"), 2, 0);
        form.add(amountField, 2, 1);
        form.add(new Label("Type"), 3, 0);
        form.add(typeBox, 3, 1);
        form.add(new Label("Date"), 4, 0);
        form.add(datePicker, 4, 1);

        for (int i = 0; i < 5; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setPercentWidth(20);
            form.getColumnConstraints().add(column);
        }

        Button addButton = new Button("Add Transaction");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                tracker.addTransaction(
                        datePicker.getValue(),
                        descriptionField.getText(),
                        categoryField.getText(),
                        amount,
                        typeBox.getValue()
                );
                saveData();
                descriptionField.clear();
                categoryField.clear();
                amountField.clear();
                datePicker.setValue(LocalDate.now());
                statusLabel.setText("Transaction added.");
                refreshAll();
            } catch (NumberFormatException ex) {
                statusLabel.setText("Enter a valid amount.");
            } catch (InvalidTransactionException ex) {
                statusLabel.setText(ex.getMessage());
            }
        });

        statusLabel.getStyleClass().add("status-label");

        HBox formActions = new HBox(12, addButton, statusLabel);
        formActions.setAlignment(Pos.CENTER_LEFT);

        VBox formCard = new VBox(12, new Label("Add Transaction"), form, formActions);
        formCard.getStyleClass().add("content-card");

        configureTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        Button deleteButton = new Button("Delete Selected");
        deleteButton.getStyleClass().add("secondary-button");
        deleteButton.setOnAction(e -> {
            Transaction selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLabel.setText("Select a transaction first.");
                return;
            }
            tracker.deleteTransaction(selected.getId());
            saveData();
            statusLabel.setText("Transaction deleted.");
            refreshAll();
        });

        HBox tableHeader = new HBox();
        Label transactionsLabel = new Label("Transactions");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        tableHeader.getChildren().addAll(transactionsLabel, spacer, deleteButton);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox tableCard = new VBox(10, tableHeader, table);
        tableCard.getStyleClass().add("content-card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        VBox tab = new VBox(16, formCard, tableCard);
        tab.setPadding(new Insets(16, 4, 4, 4));
        return tab;
    }

    private VBox buildSummaryTab() {
        Label heading = new Label("Spending by Category");
        heading.getStyleClass().add("section-title");

        categoryBox.getStyleClass().add("category-list");

        VBox card = new VBox(14, heading, categoryBox);
        card.getStyleClass().add("content-card");

        VBox tab = new VBox(card);
        tab.setPadding(new Insets(16, 4, 4, 4));
        return tab;
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No transactions yet."));

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().toString()));

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data -> {
            Transaction t = data.getValue();
            String sign = t.getType() == TransactionType.INCOME ? "+" : "-";
            return new SimpleStringProperty(String.format("%s$%,.2f", sign, t.getAmount()));
        });

        table.getColumns().setAll(dateCol, descCol, categoryCol, typeCol, amountCol);
        table.setItems(tableItems);
    }

    private void refreshAll() {
        tableItems.setAll(tracker.getAllTransactions());
        balanceValue.setText(money(tracker.getBalance()));
        incomeValue.setText(money(tracker.getTotalIncome()));
        expenseValue.setText(money(tracker.getTotalExpenses()));
        refreshCategorySummary();
    }

    private void refreshCategorySummary() {
        categoryBox.getChildren().clear();
        Map<String, Double> expenses = tracker.getExpensesByCategory();

        if (expenses.isEmpty()) {
            Label empty = new Label("No expense data yet.");
            empty.getStyleClass().add("muted-text");
            categoryBox.getChildren().add(empty);
            return;
        }

        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            Label category = new Label(entry.getKey());
            Label amount = new Label(money(entry.getValue()));
            amount.getStyleClass().add("category-amount");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(10, category, spacer, amount);
            row.getStyleClass().add("category-row");
            row.setAlignment(Pos.CENTER_LEFT);
            categoryBox.getChildren().add(row);
        }
    }

    private String money(double value) {
        return String.format("$%,.2f", value);
    }

    private void loadData() {
        try {
            fileManager.loadInto(tracker);
        } catch (IOException e) {
            statusLabel.setText("Could not load saved data.");
        }
    }

    private void saveData() {
        try {
            fileManager.save(tracker.getAllTransactions());
        } catch (IOException e) {
            statusLabel.setText("Could not save data.");
        }
    }

    @Override
    public void stop() {
        saveData();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
