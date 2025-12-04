package com.emr.gds.main.clinicalLab.controller;

import com.emr.gds.main.clinicalLab.db.ClinicalLabDatabase;
import com.emr.gds.main.clinicalLab.model.ClinicalLabItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClinicalLabController implements Initializable {

    // Left Panel
    @FXML private ListView<String> selectedItemsList;

    // Center Panel
    @FXML private TextField searchField;
    @FXML private TableView<ClinicalLabItem> labTable;
    @FXML private TableColumn<ClinicalLabItem, String> colCategory;
    @FXML private TableColumn<ClinicalLabItem, String> colTestName;
    @FXML private TableColumn<ClinicalLabItem, String> colUnit;
    @FXML private TableColumn<ClinicalLabItem, String> colMaleRef;
    @FXML private TableColumn<ClinicalLabItem, String> colFemaleRef;
    @FXML private TableColumn<ClinicalLabItem, String> colCodes;
    @FXML private TableColumn<ClinicalLabItem, String> colComments;

    // Right Panel (Details)
    @FXML private Label lblTestName;
    @FXML private Label lblCategory;
    @FXML private Label lblUnit;
    @FXML private Label lblMaleRange;
    @FXML private Label lblMaleRef;
    @FXML private Label lblFemaleRange;
    @FXML private Label lblFemaleRef;
    @FXML private Label lblCodes;
    @FXML private Label lblComments;

    private final ClinicalLabDatabase database = new ClinicalLabDatabase();
    private final ObservableList<ClinicalLabItem> masterData = FXCollections.observableArrayList();
    private final ObservableList<String> selectedItems = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupSelectionModel();
        selectedItemsList.setItems(selectedItems);
        loadData();
    }

    private void setupTable() {
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colTestName.setCellValueFactory(new PropertyValueFactory<>("testName"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colMaleRef.setCellValueFactory(new PropertyValueFactory<>("maleReferenceRange"));
        colFemaleRef.setCellValueFactory(new PropertyValueFactory<>("femaleReferenceRange"));
        colCodes.setCellValueFactory(new PropertyValueFactory<>("codes"));
        colComments.setCellValueFactory(new PropertyValueFactory<>("comments"));
    }

    private void setupSelectionModel() {
        labTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showDetails(newVal);
            } else {
                clearDetails();
            }
        });
    }

    @FXML
    private void loadData() {
        masterData.clear();
        masterData.addAll(database.getAllItems());
        labTable.setItems(masterData);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query == null || query.trim().isEmpty()) {
            labTable.setItems(masterData);
        } else {
            // Use database search directly
            java.util.List<ClinicalLabItem> results = database.searchItems(query);
            labTable.setItems(FXCollections.observableArrayList(results));
        }
    }

    private void showDetails(ClinicalLabItem item) {
        lblTestName.setText(item.getTestName());
        lblCategory.setText(item.getCategory());
        lblUnit.setText(item.getUnit());

        String mLow = item.getMaleRangeLow() != null ? String.valueOf(item.getMaleRangeLow()) : "-";
        String mHigh = item.getMaleRangeHigh() != null ? String.valueOf(item.getMaleRangeHigh()) : "-";
        lblMaleRange.setText(mLow + " - " + mHigh);
        lblMaleRef.setText(item.getMaleReferenceRange() != null ? item.getMaleReferenceRange() : "");

        String fLow = item.getFemaleRangeLow() != null ? String.valueOf(item.getFemaleRangeLow()) : "-";
        String fHigh = item.getFemaleRangeHigh() != null ? String.valueOf(item.getFemaleRangeHigh()) : "-";
        lblFemaleRange.setText(fLow + " - " + fHigh);
        lblFemaleRef.setText(item.getFemaleReferenceRange() != null ? item.getFemaleReferenceRange() : "");
        
        lblCodes.setText(item.getCodes() != null ? item.getCodes() : "-");
        lblComments.setText(item.getComments() != null ? item.getComments() : "-");
    }

    private void clearDetails() {
        lblTestName.setText("-");
        lblCategory.setText("-");
        lblUnit.setText("-");
        lblMaleRange.setText("-");
        lblMaleRef.setText("-");
        lblFemaleRange.setText("-");
        lblFemaleRef.setText("-");
        lblCodes.setText("-");
        lblComments.setText("-");
    }

    @FXML
    private void handleEdit() {
        ClinicalLabItem selected = labTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Item Selected");
            alert.setContentText("Please select an item to edit.");
            alert.showAndWait();
            return;
        }

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Item");
        dialog.setHeaderText("Edit Codes and Comments for " + selected.getTestName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codesField = new TextField(selected.getCodes());
        codesField.setPromptText("Enter codes");
        TextArea commentsArea = new TextArea(selected.getComments());
        commentsArea.setPromptText("Enter comments");
        commentsArea.setPrefRowCount(3);

        grid.add(new Label("Codes:"), 0, 0);
        grid.add(codesField, 1, 0);
        grid.add(new Label("Comments:"), 0, 1);
        grid.add(commentsArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setCodes(codesField.getText());
                selected.setComments(commentsArea.getText());
                return true;
            }
            return null;
        });

        Optional<Boolean> result = dialog.showAndWait();
        result.ifPresent(success -> {
            if (success) {
                database.updateItem(selected);
                labTable.refresh();
                showDetails(selected); // Refresh details view
            }
        });
    }

    @FXML
    private void handleAdd() {
        Dialog<ClinicalLabItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Clinical Lab Item");
        dialog.setHeaderText("Enter details for the new lab item.");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        TextField testNameField = new TextField();
        testNameField.setPromptText("Test Name");
        TextField unitField = new TextField();
        unitField.setPromptText("Unit");
        TextField maleRangeLowField = new TextField();
        maleRangeLowField.setPromptText("Male Range Low");
        TextField maleRangeHighField = new TextField();
        maleRangeHighField.setPromptText("Male Range High");
        TextField femaleRangeLowField = new TextField();
        femaleRangeLowField.setPromptText("Female Range Low");
        TextField femaleRangeHighField = new TextField();
        femaleRangeHighField.setPromptText("Female Range High");
        TextField maleRefRangeField = new TextField();
        maleRefRangeField.setPromptText("Male Reference Range");
        TextField femaleRefRangeField = new TextField();
        femaleRefRangeField.setPromptText("Female Reference Range");
        TextField codesField = new TextField();
        codesField.setPromptText("Codes");
        TextArea commentsArea = new TextArea();
        commentsArea.setPromptText("Comments");
        commentsArea.setPrefRowCount(3);

        grid.add(new Label("Category:"), 0, 0);
        grid.add(categoryField, 1, 0);
        grid.add(new Label("Test Name:"), 0, 1);
        grid.add(testNameField, 1, 1);
        grid.add(new Label("Unit:"), 0, 2);
        grid.add(unitField, 1, 2);
        grid.add(new Label("Male Range Low:"), 0, 3);
        grid.add(maleRangeLowField, 1, 3);
        grid.add(new Label("Male Range High:"), 0, 4);
        grid.add(maleRangeHighField, 1, 4);
        grid.add(new Label("Female Range Low:"), 0, 5);
        grid.add(femaleRangeLowField, 1, 5);
        grid.add(new Label("Female Range High:"), 0, 6);
        grid.add(femaleRangeHighField, 1, 6);
        grid.add(new Label("Male Ref Range:"), 0, 7);
        grid.add(maleRefRangeField, 1, 7);
        grid.add(new Label("Female Ref Range:"), 0, 8);
        grid.add(femaleRefRangeField, 1, 8);
        grid.add(new Label("Codes:"), 0, 9);
        grid.add(codesField, 1, 9);
        grid.add(new Label("Comments:"), 0, 10);
        grid.add(commentsArea, 1, 10);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Double maleLow = maleRangeLowField.getText().isEmpty() ? null : Double.parseDouble(maleRangeLowField.getText());
                    Double maleHigh = maleRangeHighField.getText().isEmpty() ? null : Double.parseDouble(maleRangeHighField.getText());
                    Double femaleLow = femaleRangeLowField.getText().isEmpty() ? null : Double.parseDouble(femaleRangeLowField.getText());
                    Double femaleHigh = femaleRangeHighField.getText().isEmpty() ? null : Double.parseDouble(femaleRangeHighField.getText());

                    return new ClinicalLabItem(
                        0, // ID will be set by the database
                        categoryField.getText(),
                        testNameField.getText(),
                        unitField.getText(),
                        maleLow,
                        maleHigh,
                        femaleLow,
                        femaleHigh,
                        maleRefRangeField.getText(),
                        femaleRefRangeField.getText(),
                        codesField.getText(),
                        commentsArea.getText()
                    );
                } catch (NumberFormatException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Input Error");
                    errorAlert.setHeaderText("Invalid Number Format");
                    errorAlert.setContentText("Please ensure numeric fields contain valid numbers.");
                    errorAlert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        Optional<ClinicalLabItem> result = dialog.showAndWait();
        result.ifPresent(newItem -> {
            database.insertItem(newItem);
            loadData(); // Refresh the table
            labTable.getSelectionModel().select(newItem); // Select the newly added item
            showDetails(newItem); // Show details of newly added item
        });
    }

    @FXML
    private void handleDelete() {
        ClinicalLabItem selected = labTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Item Selected");
            alert.setContentText("Please select an item to delete.");
            alert.showAndWait();
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Delete Item: " + selected.getTestName());
        confirmationAlert.setContentText("Are you sure you want to delete this item?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            database.deleteItem(selected.getId());
            loadData(); // Refresh the table
            clearDetails(); // Clear details panel
        }
    }

    @FXML
    private void addToSelection() {
        ClinicalLabItem selected = labTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String itemStr = selected.getTestName() + " (" + selected.getCategory() + ")";
            if (!selectedItems.contains(itemStr)) {
                selectedItems.add(itemStr);
            }
        }
    }

    @FXML
    private void copySelected() {
        if (selectedItems.isEmpty()) return;
        String content = String.join("\n", selectedItems);
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    @FXML
    private void clearSelection() {
        selectedItems.clear();
    }
}
