package docket;

import docket.db.Appointment;
import docket.db.Contact;
import docket.db.Customer;
import docket.db.DatabaseManager;

import docket.localization.lz;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the add or modify appointment form
 */
public class AppointmentFormController {

    //<editor-fold desc="Private Members">

    private final MainScreenController parentController; // Used for refreshing database data on form close

    /**
     * Type of form to use
     */
    private enum FormTypes {
        AddAppointment,
        ModifyAppointment
    }

    private final FormTypes formType;

    private Appointment appointment; // Existing appointment to use and update

    //</editor-fold desc="">

    //<editor-fold desc="FXML Private Members">

    @FXML
    private Label mainTitleLabel;

    @FXML
    private GridPane mainGridPane;

    @FXML
    private Label titleLabel;

    @FXML
    private TextField titleTextField;

    @FXML
    private Label descriptionLabel;

    @FXML
    private TextField descriptionTextField;

    @FXML
    private Label clientLabel;

    @FXML
    private ComboBox clientComboBox;

    @FXML
    private Label contactLabel;

    @FXML
    private ComboBox contactComboBox;

    @FXML
    private Label locationLabel;

    @FXML
    private TextField locationTextField;

    @FXML
    private Label typeLabel;

    @FXML
    private TextField typeTextField;

    @FXML
    private Label startDateLabel;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private Label endDateLabel;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label startTimeLabel;

    @FXML
    private ComboBox startHourComboBox;

    @FXML
    private ComboBox startMinuteComboBox;

    @FXML
    private ComboBox startAmPmComboBox;

    @FXML
    private Label endTimeLabel;

    @FXML
    private ComboBox endHourComboBox;

    @FXML
    private ComboBox endMinuteComboBox;

    @FXML
    private ComboBox endAmPmComboBox;

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
     * Creates new appointment controller
     * @param controller Controller from parent form
     */
    public AppointmentFormController(MainScreenController controller) {
        parentController = controller;
        formType = FormTypes.AddAppointment;
    }

    /**
     * Constructor
     * Creates edit appointment controller
     * @param controller Controller from parent form
     * @param appointment Existing appointment to update
     */
    public AppointmentFormController(MainScreenController controller, Appointment appointment) {
        parentController = controller;
        this.appointment = appointment;
        formType = FormTypes.ModifyAppointment;
    }

    //</editor-fold>

    //<editor-fold desc="Initializers">

    /**
     * Initializer
     */
    @FXML
    public void initialize() {
        initializeAllComboBoxes();
        loadingOverlayGridPane.setVisible(false);

        // Call specific initializer for add or edit appointment
        if (formType == FormTypes.AddAppointment)
            initializeAddAppointmentForm();
        else
            initializeModifyAppointmentForm();

        // Localization
        titleLabel.setText(lz.get("Title"));
        descriptionLabel.setText(lz.get("Description"));
        clientLabel.setText(lz.get("Client"));
        contactLabel.setText(lz.get("Contact"));
        locationLabel.setText(lz.get("Location"));
        typeLabel.setText(lz.get("Type"));
        startDateLabel.setText(lz.get("Start Date"));
        startTimeLabel.setText(lz.get("Start Time"));
        endDateLabel.setText(lz.get("End Date"));
        endTimeLabel.setText(lz.get("End Time"));
        idLabel.setText(lz.get("Appointment ID"));
        idTextField.setPromptText(lz.get("Auto-Generated"));
        createdByLabel.setText(lz.get("Created By"));
        createdOnLabel.setText(lz.get("Created On"));
        lastUpdatedByLabel.setText(lz.get("Last Updated By"));
        lastUpdatedOnLabel.setText(lz.get("Last Updated On"));
        saveButton.setText(lz.get("Save"));
        cancelButton.setText(lz.get("Cancel"));
    }

    /**
     * Initializer specific to Add Appointment Form
     */
    private void initializeAddAppointmentForm() {
        mainTitleLabel.setText(lz.get("Add Appointment"));
        createdByOutputLabel.setText(DatabaseManager.getLoggedInUsername());
        createdOnOutputLabel.setText("");
        lastUpdatedByOutputLabel.setText("");
        lastUpdatedOnOutputLabel.setText("");
        initializeDatePicker(startDatePicker);
        initializeDatePicker(endDatePicker);
    }

    /**
     * Initializer specific to Modify Appointment Form
     */
    private void initializeModifyAppointmentForm() {
        mainTitleLabel.setText(lz.get("Edit Appointment"));
        loadExistingAppointment();
    }

    /**
     * Initilizes combo boxes
     */
    private void initializeAllComboBoxes() {
        initializeHourComboBox(startHourComboBox, 7);
        initializeHourComboBox(endHourComboBox, 8);
        initializeMinuteComboBox(startMinuteComboBox);
        initializeMinuteComboBox(endMinuteComboBox);
        initializeAmPmComboBox(startAmPmComboBox);
        initializeAmPmComboBox(endAmPmComboBox);

        initializeClientComboBox();
        initializeContactComboBox();
    }

    /**
     * Initializes hour combo box
     * @param comboBox Combo box to initializes
     * @param defaultIndex used to set selected index
     */
    private void initializeHourComboBox(ComboBox comboBox, int defaultIndex) {
        comboBox.getItems().addAll("1","2","3","4","5","6","7","8","9","10","11","12");
        comboBox.getSelectionModel().select(defaultIndex);
    }

    /**
     * Initializes minutes combo box
     * @param comboBox Combo box to initializes
     */
    private void initializeMinuteComboBox(ComboBox comboBox) {
        comboBox.getItems().addAll("00","15","30","45");
        comboBox.getSelectionModel().select(0);
    }

    /**
     * Initializes ap/pm combo box
     * @param comboBox Combo box to initializes
     */
    private void initializeAmPmComboBox(ComboBox comboBox) {
        comboBox.getItems().addAll("AM","PM");
        comboBox.getSelectionModel().select(0);
    }

    /**
     * Adds all clients in database to combo box
     */
    private void initializeClientComboBox() {

        clientComboBox.setItems(DatabaseManager.getAllCustomers());
    }

    /**
     * Adds all contacts in database to combo box
     */
    private void initializeContactComboBox() {
        contactComboBox.setItems(DatabaseManager.getAllContacts());
    }

    /**
     * Sets data picker to today
     * @param datePicker the date picker
     */
    private void initializeDatePicker(DatePicker datePicker) {
        datePicker.setValue(LocalDate.now());
        datePicker.getEditor().setDisable(true);
    }

    /**
     * Loads existing appointment data into the form fields
     */
    private void loadExistingAppointment() {
        titleTextField.setText(appointment.getTitle());
        descriptionTextField.setText(appointment.getDescription());
        locationTextField.setText(appointment.getLocation());
        typeTextField.setText(appointment.getType());
        idTextField.setText(String.valueOf(appointment.getAppointmentId()));
        createdByOutputLabel.setText(appointment.getCreatedBy());
        createdOnOutputLabel.setText(appointment.getLocalCreateDateAsString());
        lastUpdatedByOutputLabel.setText(appointment.getLastUpdatedBy());
        lastUpdatedOnOutputLabel.setText(appointment.getLocalUpdateDateAsString());

        for (Contact contact : DatabaseManager.getAllContacts()) {
            if (contact.getId() == appointment.getContactId()) {
                contactComboBox.getSelectionModel().select(contact);
                break;
            }
        }

        for (Customer customer : DatabaseManager.getAllCustomers()) {
            if (customer.getCustomerId() == appointment.getCustomerId()) {
                clientComboBox.getSelectionModel().select(customer);
                break;
            }
        }

        DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("h");
        DateTimeFormatter minuteFormatter = DateTimeFormatter.ofPattern("mm");
        DateTimeFormatter ampmFormatter = DateTimeFormatter.ofPattern("a");

        // Start date/time
        ZonedDateTime startDateTimeLocal =
                appointment.getStartDateUTC().withZoneSameInstant(ZoneId.systemDefault());
        startDatePicker.setValue(startDateTimeLocal.toLocalDate());
        startHourComboBox.getSelectionModel().select(startDateTimeLocal.format(hourFormatter));
        startMinuteComboBox.getSelectionModel().select(startDateTimeLocal.format(minuteFormatter));
        startAmPmComboBox.getSelectionModel().select(startDateTimeLocal.format(ampmFormatter));

        // End date/time
        ZonedDateTime endDateTimeLocal =
                appointment.getEndDateUTC().withZoneSameInstant(ZoneId.systemDefault());
        endDatePicker.setValue(endDateTimeLocal.toLocalDate());
        endHourComboBox.getSelectionModel().select(endDateTimeLocal.format(hourFormatter));
        endMinuteComboBox.getSelectionModel().select(endDateTimeLocal.format(minuteFormatter));
        endAmPmComboBox.getSelectionModel().select(endDateTimeLocal.format(ampmFormatter));
    }

    //</editor-fold>

    //<editor-fold desc="Listeners">

    /**
     * Saves appointment data to database and closes window
     * Performs validations to make sure that all form fields are filled out and that appointment is within business hours
     * @throws Exception
     */
    public void saveButtonListener() throws Exception {
        // Display error and return if not all forms are filled out
        if (!bAllFormsFilledOut()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, lz.get("Not all fields are filled out."), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Checks to see if start is before end date time
        if (!ScheduleHelpers.isStartDateBeforeEndDate(getStartDate(), getEndDate())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, lz.get("Start date/time must be before end date/time."), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Checks to see if during business hours
        if (!ScheduleHelpers.isDuringBusinessHours(getStartDate(), getEndDate())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, lz.get("Appointment must be between 8am and 10pm U.S. Eastern Time."), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        int refernceApptId = -1;

        // Get existing appointment ID
        if (formType == FormTypes.ModifyAppointment)
            refernceApptId = appointment.getAppointmentId();

        // Check if appointment overlaps another
        if (ScheduleHelpers.bDatesOverlapping(getStartDate(), getEndDate(), refernceApptId)) {
            Alert alert = new Alert(Alert.AlertType.WARNING, lz.get("Appointment overlaps with another appointment."), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.WARNING,
                lz.get("Appointment start time is before current time.") + " " +
                        lz.get("Appointment will be used for record keeping purposes only.") +
                        "\n\n" + lz.get("Do you want to continue?"), ButtonType.YES, ButtonType.NO);

        // Checks to see if appointment start date is before now
        if (ScheduleHelpers.isStartDateBeforeEndDate(getStartDate(), ZonedDateTime.now())) {
            alert.showAndWait();
        }

        // If confirmation declined, return
        if (alert.getResult() == ButtonType.NO)
            return;

        // Show loading screen and disable exit button
        loadingOverlayGridPane.setVisible(true);
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.setOnCloseRequest(event -> event.consume());

        // Add or update appointment
        if (formType == FormTypes.AddAppointment) {
            addNewAppointment();
        } else {
            updateAppointment();
        }
    }

    /**
     * Close the form window
     */
    public void cancelButtonListener() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    //</editor-fold>

    //<editor-fold desc="Private Helpers">

    /**
     * Asynchronously adds new appointment and closes form window
     * Lambda used to created async task and code upon succeed/fail
     * @throws Exception
     */
    private void addNewAppointment() throws Exception {
        // Create task
        Task<Boolean> task = new Task<>() {

            @Override
            protected Boolean call() throws Exception {
                DatabaseManager.addNewAppointment(
                        titleTextField.getText(),
                        descriptionTextField.getText(),
                        locationTextField.getText(),
                        typeTextField.getText(),
                        ScheduleHelpers.getZonedDateTimeAsUtcString(getStartDate()),
                        ScheduleHelpers.getZonedDateTimeAsUtcString(getEndDate()),
                        ((Customer)clientComboBox.getSelectionModel().getSelectedItem()).getCustomerId(),
                        ((Contact)contactComboBox.getSelectionModel().getSelectedItem()).getId());
                return true;
            }

        };

        // On task succeed
        task.setOnSucceeded(evt -> {
            try {
                parentController.refreshDb();
            } catch (Exception e) {
                e.printStackTrace();
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
     * Asynchronously updates appointment
     * Lambda used to created async task and code upon succeed/fail
     * @throws Exception
     */
    private void updateAppointment() throws Exception {
        // Create task
        Task<Boolean> task = new Task<>() {

            @Override
            protected Boolean call() throws Exception {
                DatabaseManager.updateAppointment(
                        appointment.getAppointmentId(),
                        titleTextField.getText(),
                        descriptionTextField.getText(),
                        locationTextField.getText(),
                        typeTextField.getText(),
                        ScheduleHelpers.getZonedDateTimeAsUtcString(getStartDate()),
                        ScheduleHelpers.getZonedDateTimeAsUtcString(getEndDate()),
                        appointment.getCreatedBy(),
                        ScheduleHelpers.getZonedDateTimeAsUtcString(appointment.getCreateDateUTC()),
                        ((Customer)clientComboBox.getSelectionModel().getSelectedItem()).getCustomerId(),
                        ((Contact)contactComboBox.getSelectionModel().getSelectedItem()).getId());
                return true;
            }

        };

        // On task succeed
        task.setOnSucceeded(evt -> {
            try {
                parentController.refreshDb();
            } catch (Exception e) {
                e.printStackTrace();
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
     * Checks to see if all form fields are filled out
     * @return True if all filled out
     */
    private boolean bAllFormsFilledOut() {
        if (!titleTextField.getText().isEmpty()
                && !locationTextField.getText().isEmpty()
                && !typeTextField.getText().isEmpty()
                && !descriptionTextField.getText().isEmpty()
                && contactComboBox.getSelectionModel().getSelectedIndex() != -1
                && clientComboBox.getSelectionModel().getSelectedIndex() != -1)
        {
            return true;
        }

        return false;
    }

    /**
     * Parses the form data for the start date/time into a ZoneDateTime
     * @return
     */
    private ZonedDateTime getStartDate() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd ");
        LocalDate date = startDatePicker.getValue();
        String dateString = dateFormatter.format(date);

        String timeString = startHourComboBox.getSelectionModel().getSelectedItem().toString()
                + ":"
                + startMinuteComboBox.getSelectionModel().getSelectedItem().toString()
                + " "
                + startAmPmComboBox.getSelectionModel().getSelectedItem().toString();
        String dateTimeString = dateString + timeString;

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a");
        return  ZonedDateTime.parse(dateTimeString, dateTimeFormatter.withZone(ZoneId.systemDefault()));
    }

    /**
     * Parses the form data for the end date/time into a ZoneDateTime
     * @return
     */
    private ZonedDateTime getEndDate() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd ");
        LocalDate date = endDatePicker.getValue();
        String dateString = dateFormatter.format(date);

        String timeString = endHourComboBox.getSelectionModel().getSelectedItem().toString()
                + ":"
                + endMinuteComboBox.getSelectionModel().getSelectedItem().toString()
                + " "
                + endAmPmComboBox.getSelectionModel().getSelectedItem().toString();
        String dateTimeString = dateString + timeString;

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a");
        return  ZonedDateTime.parse(dateTimeString, dateTimeFormatter.withZone(ZoneId.systemDefault()));
    }

    //</editor-folder>
}
