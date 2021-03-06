package docket;

import com.mysql.cj.xdevapi.Client;
import docket.db.*;
import docket.localization.lz;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.time.ZonedDateTime;

/**
 * Controller for the Add Client Form and the Modify Client Form
 */
public class ClientFormController {

    //<editor-fold desc="Private Members">

    private final MainScreenController parentController; // Used to refresh DB on close

    /**
     * Type of client form
     */
    private enum FormTypes {
        AddClient,
        ModifyClient
    }

    private final FormTypes formType; // Form type to display

    private Customer existingCustomer;

    private FilteredList<FirstLevelDivision> filteredFirstLevelDivisions;

    //</editor-fold>

    //<editor-fold desc="FXML">

    @FXML
    private Label mainTitleLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private TextField nameTextField;

    @FXML
    private Label phoneLabel;

    @FXML
    private TextField phoneTextField;

    @FXML
    private Label addressLabel;

    @FXML
    private TextField addressTextField;

    @FXML
    private Label countryLabel;

    @FXML
    private ComboBox countryComboBox;

    @FXML
    private Label firstDivisionLabel;

    @FXML
    private ComboBox firstDivisionComboBox;

    @FXML
    private Label postalCodeLabel;

    @FXML
    private TextField postalCodeTextField;

    @FXML
    private Label idLabel;

    @FXML
    private TextField idTextField;

    @FXML
    private Label createdByLabel;

    @FXML
    private Label createdByOutputLabel;

    @FXML
    private Label createdOnLabel;

    @FXML
    private Label createdOnOutputLabel;

    @FXML
    private Label lastUpdatedByLabel;

    @FXML
    private Label lastUpdatedByOutputLabel;

    @FXML
    private Label lastUpdatedOnLabel;

    @FXML
    private Label lastUpdatedOnOutputLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private GridPane loadingOverlayGridPane;

    //</editor-fold>

    //<editor-fold desc="Constructors">

    /**
     * Constructor
     * Creates an Add Client Form
     * @param controller Controller of owner
     */
    public ClientFormController( MainScreenController controller) {
        formType = FormTypes.AddClient;
        parentController = controller;
    }

    /**
     * Constructor
     * Creates a Modify Client Form
     * @param controller Controller of owner
     * @param existingCustomer Customer to modify
     */
    public ClientFormController(MainScreenController controller, Customer existingCustomer) {
        formType = FormTypes.ModifyClient;
        parentController = controller;
        this.existingCustomer = existingCustomer;
    }

    //</editor-fold>

    //<editor-fold desc="Initializers">

    /**
     * Initializer
     * Sets up form and processes localization
     */
    public void initialize() {
        // Load constants from database
        countryComboBox.setItems(DatabaseManager.getAllCountries());
        filteredFirstLevelDivisions = new FilteredList<>(DatabaseManager.getAllFirstLevelDivisions());
        firstDivisionComboBox.setItems(filteredFirstLevelDivisions);
        setDivisionFilter();

        // Call individual initializers for Add or Modify form
        if (formType == FormTypes.AddClient)
            initializeAddClientForm();
        else
            initializeModifyClientForm();

        // Localization
        nameLabel.setText(lz.get("Name"));
        phoneLabel.setText(lz.get("Phone"));
        addressLabel.setText(lz.get("Address"));
        countryLabel.setText(lz.get("Country"));
        firstDivisionLabel.setText(lz.get("First-Level"));
        postalCodeLabel.setText(lz.get("Postal Code"));
        idLabel.setText(lz.get("Customer ID"));
        idTextField.setPromptText(lz.get("Auto-Generated"));
        createdByLabel.setText(lz.get("Created By"));
        createdOnLabel.setText(lz.get("Created On"));
        lastUpdatedByLabel.setText(lz.get("Last Updated By"));
        lastUpdatedOnLabel.setText(lz.get("Last Updated On"));
        saveButton.setText(lz.get("Save"));
        cancelButton.setText(lz.get("Cancel"));
    }

    /**
     * Initializer - Add Client Form
     * Sets parameters specific to Add Client Form
     */
    private void initializeAddClientForm() {
        mainTitleLabel.setText(lz.get("Add Client"));
        createdByOutputLabel.setText(DatabaseManager.getLoggedInUsername());
        createdOnOutputLabel.setText(""); // Not needed for new customer
        lastUpdatedByOutputLabel.setText(""); // Not needed for new customer
        lastUpdatedOnOutputLabel.setText(""); // Not needed for new customer
    }

    /**
     * Initializer - Modify Client Form
     * Sets parameters specific to Modify Client Form
     */
    private void initializeModifyClientForm() {
        mainTitleLabel.setText(lz.get("Edit Client"));
        loadExistingClientData();
    }

    /**
     * Loads data into text fields and combo boxes from existing customer
     */
    private void loadExistingClientData() {
        nameTextField.setText(existingCustomer.getName());
        phoneTextField.setText(existingCustomer.getPhone());
        addressTextField.setText(existingCustomer.getAddress());
        postalCodeTextField.setText(existingCustomer.getPostalCode());
        idTextField.setText(String.valueOf(existingCustomer.getCustomerId()));
        createdByOutputLabel.setText(existingCustomer.getCreatedBy());
        createdOnOutputLabel.setText(existingCustomer.getLocalCreateDateAsString());
        lastUpdatedByOutputLabel.setText(existingCustomer.getLastUpdatedBy());
        lastUpdatedOnOutputLabel.setText(existingCustomer.getLocalLastUpdateAsString());

        for (FirstLevelDivision division : DatabaseManager.getAllFirstLevelDivisions()) {
            if (existingCustomer.getDivisionId() == division.getDivisionId()) {
                for (Country country : DatabaseManager.getAllCountries()) {
                    if (division.getCountryId() == country.getCountryId()) {
                        countryComboBox.getSelectionModel().select(country);
                        break;
                    }
                }
                setDivisionFilter();
                firstDivisionComboBox.getSelectionModel().select(division);
                break;
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="Listeners">

    /**
     * Saves new client or updates existing client, and closes the form
     * Validates that all the data is filled out
     */
    public void saveButtonListener() {
        // Check if not all form fields are filled out
        if (!bAllFormsFilledOut()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, lz.get("Not all fields are filled out."), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Display loading screen
        loadingOverlayGridPane.setVisible(true);

        // Disable exit button
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.setOnCloseRequest(event -> event.consume());

        // Add new client or update existing
        if (formType == FormTypes.AddClient) {
            addNewClient();
        } else {
            modifyExistingClient();
        }
    }

    /**
     * Exits the form
     */
    public void cancelButtonListener() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Updates the first level data based on selected country
     */
    public void countryComboBoxListener() {
        setDivisionFilter();
    }

     //</editor-fold>

     //<editor-fold desc="Private Helpers">

    /**
     * Updates the first level data based on selected country
     */
    private void setDivisionFilter() {
        filteredFirstLevelDivisions.setPredicate(myObject -> {
            if (countryComboBox.getSelectionModel().getSelectedIndex() == -1) {
                //firstDivisionComboBox.getSelectionModel().select(-1);
                return false;
            }

            int countryId = ((Country)(countryComboBox.getSelectionModel().getSelectedItem())).getCountryId();

            if (myObject.getCountryId() == countryId)
                return true;

            return false;
        });
    }

    /**
     * Checks to see if all form fields are filled out
     * @return True if all are filled out, otherwise false
     */
    private boolean bAllFormsFilledOut() {
        if (!nameTextField.getText().isEmpty()
                && !phoneTextField.getText().isEmpty()
                && !addressTextField.getText().isEmpty()
                && !postalCodeTextField.getText().isEmpty()
                && countryComboBox.getSelectionModel().getSelectedIndex() != -1
                && firstDivisionComboBox.getSelectionModel().getSelectedIndex() != -1)
        {
            return true;
        }

        return false;
    }

    /**
     * Adds new client asynchronously to the database and closes the form
     * Lambda used to create async task
     */
    private void addNewClient() {
        // Task to add new client
        Task<Boolean> task = new Task<>() {

            @Override
            protected Boolean call() throws Exception {
                DatabaseManager.addNewCustomer(
                        nameTextField.getText(),
                        addressTextField.getText(),
                        postalCodeTextField.getText(),
                        phoneTextField.getText(),
                        ((FirstLevelDivision)(firstDivisionComboBox.getSelectionModel().getSelectedItem()))
                                .getDivisionId());
                return true;
            }

        };

        // On task succeed
        task.setOnSucceeded(evt -> {
            try {
                parentController.refreshDb();
            } catch (Exception ex) {
                System.out.println(ex);
            }
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        // On task fail
        task.setOnFailed(evt -> {

        });

        // Start task
        new Thread(task).start();
    }

    /**
     * Updates existing client asynchronously to the database and closes form
     * Lambda used to create async task
     */
    private void modifyExistingClient() {
        // Task to modify existing client
        Task<Boolean> task = new Task<>() {

            @Override
            protected Boolean call() throws Exception {
                DatabaseManager.modifyCustomer(
                        existingCustomer.getCustomerId(),
                        nameTextField.getText(),
                        addressTextField.getText(),
                        postalCodeTextField.getText(),
                        phoneTextField.getText(),
                        ((FirstLevelDivision)(firstDivisionComboBox.getSelectionModel().getSelectedItem()))
                                .getDivisionId());
                return true;
            }

        };

        // On task succeed
        task.setOnSucceeded(evt -> {
            try {
                parentController.refreshDb();
            } catch (Exception ex) {
                System.out.println(ex);
            }
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        // On task fail
        task.setOnFailed(evt -> {

        });

        // Start task
        new Thread(task).start();
    }

    //</editor-fold>
}
