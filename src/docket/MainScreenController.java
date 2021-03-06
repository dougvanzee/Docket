package docket;

import docket.db.*;
import docket.localization.lz;
import docket.reports.AppointmentTypeReportController;
import docket.reports.MonthlyMeetingsReportController;
import docket.reports.ScheduleReportController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Controller for the Main Screen of the application
 */
public class MainScreenController {

    //<editor-fold desc="Private Members">

    private int currentWeekOffset = 0; // Week offset for displaying appointments
    private int currentMonthOffset = 0; // Month offset for displaying appointments

    private int appointmentFilterStart = 20210117; // Used to compare dates of displayed appointments
    private int appointmentFilterEnd = 20210124; // Used to compare dates of displayed appointments

    FilteredList<Appointment> filteredAppointmentList; // Displayed appointments

    private boolean isUserLoggingIn; // Used to determine whether to display login welcome message

    //</editor-fold>

    //<editor-fold desc="FXML - Side Menu">

    @FXML
    private Label timeLabel;

    @FXML
    private Label dayLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private ToggleButton scheduleButton;

    @FXML
    private ToggleButton clientsButton;

    @FXML
    private ToggleButton reportsButton;

    @FXML
    private ToggleGroup sideMenuGroup;

    @FXML
    private Button logoutButton;

    @FXML
    private TabPane menuTabPane;

    //</editor-fold>

    //<editor-fold desc="FXML - Appointments View">

    @FXML
    private Label appointmentLabel;

    @FXML
    private Label appointmentTitleLabel;

    @FXML
    private TableView appointmentTableView;

    @FXML
    private TableColumn appointColumnId;

    @FXML
    private TableColumn appointColumnTitle;

    @FXML
    private TableColumn appointColumnDescription;

    @FXML
    private TableColumn appointColumnLocation;

    @FXML
    private TableColumn appointColumnContact;

    @FXML
    private TableColumn appointColumnType;

    @FXML
    private TableColumn appointColumnStart;

    @FXML
    private TableColumn appointColumnEnd;

    @FXML
    private TableColumn appointColumnCustomerId;

    @FXML
    private Button previousAppointmentViewButton;

    @FXML
    private Button nextAppointmentViewButton;

    @FXML
    private RadioButton weeklyRadioButton;

    @FXML
    private RadioButton monthlyRadioButton;

    @FXML
    private Button createAppointmentButton;

    @FXML
    private Button editAppointmentButton;

    @FXML
    private Button deleteAppointmentButton;

    //</editor-fold>

    //<editor-fold desc="FXML - Clients View">

    @FXML
    private Label clientsLabel;

    @FXML
    private TableView clientsTableView;

    @FXML
    private TableColumn clientColumnId;

    @FXML
    private TableColumn clientColumnName;

    @FXML
    private TableColumn clientColumnPhone;

    @FXML
    private TableColumn clientColumnAddress;

    @FXML
    private TableColumn clientColumnFirstLevel;

    @FXML
    private TableColumn clientColumnCountry;

    @FXML
    private TableColumn clientColumnPostalCode;

    @FXML
    private Button addClientButton;

    @FXML
    private Button editClientButton;

    @FXML
    private Button deleteClientButton;

    //</editor-fold>

    //<editor-fold desc="FXML - Reports View">

    @FXML
    private Label reportsLabel;

    @FXML
    private Label appointmentReportTitleLabel;

    @FXML
    private Label appointmentReportSubtitleLabel;

    @FXML
    private Button generateAppointmentReportButton;

    @FXML
    private Label scheduleReportTitleLabel;

    @FXML
    private Label scheduleReportSubtitleLabel;

    @FXML
    private Label scheduleReportContactLabel;

    @FXML
    private ComboBox scheduleContactComboBox;

    @FXML
    private Button generateScheduleReportButton;

    @FXML
    private Label meetingsReportTitleLabel;

    @FXML
    private Label meetingsReportSubtitleLabel;

    @FXML
    private Button generateMeetingsReportButton;

    //</editor-fold>

    //<editor-fold desc="FXML - Other">

    @FXML
    private GridPane loadingGridPane;

    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Default constructor
     */
    public MainScreenController() {
        isUserLoggingIn = true;
    }

    //</editor-fold>

    //<editor-fold desc="Initializers">

    /**
     * Initializes all elements of UI
     * @throws Exception
     */
    public void initialize() throws Exception {
        // Initialize side menu
        startClock();
        initializeSideMenu();

        // Setup appointment table view
        initializeAppointmentTableView();
        initializeAppointmentScreen();

        // Setup client table view
        initializeClientTableView();

        // Setup reports page
        initializeReportsScreen();

        // Download data from database
        refreshDb();
    }

    /**
     * Initializes language and listeners for Side Menu
     * @throws Exception
     */
    private void initializeSideMenu() throws Exception {
        // Localization
        scheduleButton.setText(lz.get("Appointments"));
        clientsButton.setText(lz.get("Clients"));
        reportsButton.setText(lz.get("Reports"));
        logoutButton.setText(lz.get("Logout"));

        // Listeners
        addSideMenuGroupListener();
    }

    /**
     * Initializes language and listeners for the Appointment Tab
     */
    private void initializeAppointmentTableView() {
        // Localization
        appointmentLabel.setText(lz.get("Appointments"));
        weeklyRadioButton.setText(lz.get("Weekly"));
        monthlyRadioButton.setText(lz.get("Monthly"));
        appointColumnTitle.setText(lz.get("Title"));
        appointColumnDescription.setText(lz.get("Description"));
        appointColumnLocation.setText(lz.get("Location"));
        appointColumnContact.setText(lz.get("Contact"));
        appointColumnType.setText(lz.get("Type"));
        appointColumnStart.setText(lz.get("Start"));
        appointColumnEnd.setText(lz.get("End"));
        appointColumnCustomerId.setText(lz.get("Customer ID"));
        createAppointmentButton.setText(lz.get("Create Appointment"));
        editAppointmentButton.setText(lz.get("Edit Appointment"));
        deleteAppointmentButton.setText(lz.get("Delete Appointment"));

        filteredAppointmentList = new FilteredList<>(DatabaseManager.getAllAppointments());

        // Setup Appointment Table View
        appointmentTableView.setItems(filteredAppointmentList);
        appointmentTableView.setPlaceholder(new Label(lz.get("No appointments")));
        appointColumnId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        appointColumnTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        appointColumnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        appointColumnLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        appointColumnContact.setCellValueFactory(new PropertyValueFactory<>("contactId"));
        appointColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        appointColumnStart.setCellValueFactory(new PropertyValueFactory<>("localStartDateAsString"));
        appointColumnEnd.setCellValueFactory(new PropertyValueFactory<>("localEndDateAsString"));
        appointColumnCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
    }

    /**
     * Initializes language and listeners for the Client Tab
     */
    private void initializeClientTableView() {
        // Localization
        clientsLabel.setText(lz.get("Clients"));
        clientColumnName.setText(lz.get("Name"));
        clientColumnPhone.setText(lz.get("Phone"));
        clientColumnAddress.setText(lz.get("Address"));
        clientColumnFirstLevel.setText(lz.get("First-Level"));
        clientColumnCountry.setText(lz.get("Country"));
        clientColumnPostalCode.setText(lz.get("Postal Code"));
        addClientButton.setText(lz.get("Add Client"));
        editClientButton.setText(lz.get("Edit Client"));
        deleteClientButton.setText(lz.get("Delete Client"));

        // Setup Clients Table View
        clientsTableView.setItems(DatabaseManager.getAllCustomers());
        clientsTableView.setPlaceholder(new Label(lz.get("No clients")));
        clientColumnId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        clientColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        clientColumnPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        clientColumnAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        clientColumnFirstLevel.setCellValueFactory(new PropertyValueFactory<>("divisionName"));
        clientColumnCountry.setCellValueFactory(new PropertyValueFactory<>("countryName"));
        clientColumnPostalCode.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
    }

    /**
     * Initializes language and listeners for the Reports Tab
     */
    private void initializeReportsScreen() {
        // Localization
        reportsLabel.setText(lz.get("Reports"));
        appointmentReportTitleLabel.setText(lz.get("Appointments by Month and Type"));
        appointmentReportSubtitleLabel.setText(lz.get("Displays the total number of customer appointments by type and month"));
        generateAppointmentReportButton.setText(lz.get("Open Report"));
        scheduleReportTitleLabel.setText(lz.get("Schedule"));
        scheduleReportSubtitleLabel.setText(lz.get("A schedule for a particular contact"));
        scheduleReportContactLabel.setText(lz.get("Contact"));
        generateScheduleReportButton.setText(lz.get("Open Report"));
        meetingsReportTitleLabel.setText(lz.get("Meetings Per Month"));
        meetingsReportSubtitleLabel.setText(lz.get("Displays a chart showing the number of meetings per month this year for all contacts"));
        generateMeetingsReportButton.setText(lz.get("Open Report"));

        // Set items for contact combo box
        scheduleContactComboBox.setItems(DatabaseManager.getAllContacts());
    }

    /**
     * Resets Appointment Tab parameters
     * @throws Exception
     */
    private void initializeAppointmentScreen() throws Exception {
        currentWeekOffset = 0;
        currentMonthOffset = 0;
        weeklyRadioButton.setSelected(true);
        setAppointmentFilterType();
    }

    //</editor-fold>

    //<editor-fold desc="Listeners">

    /**
     * Event when next arrow button is clicked on the appointment view. Changes filter to be next week or month.
     */
    public void  nextAppointmentViewButtonListener() {
        if (weeklyRadioButton.isSelected())
            currentWeekOffset++;
        else
            currentMonthOffset++;
        setAppointmentFilterType();
    }
    /**
     * Event when previous arrow button is clicked on the appointment view. Changes filter to be previous week or month.
     */
    public void  previousAppointmentViewButtonListener() {
        if (weeklyRadioButton.isSelected())
            currentWeekOffset--;
        else
            currentMonthOffset--;
        setAppointmentFilterType();
    }

    /**
     * Event when weekly or monthly radio button is selected
     */
    public void weekMonthRadioButtonListener() { setAppointmentFilterType(); }

    /**
     * Displays create appointment form
     * @throws Exception
     */
    public void createAppointmentButtonListener() throws Exception {
        // Create add appointment form
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AppointmentForm.fxml"));
        loader.setController(new AppointmentFormController(this));
        Parent root = loader.load();
        Stage popupwindow = new Stage();

        // Set add appointment form parameters
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.initStyle(StageStyle.UTILITY);
        popupwindow.setResizable(false);
        popupwindow.setTitle("");

        // Display form
        Scene scene1= new Scene(root, 800, 550);
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();
    }

    /**
     * Displays modify appointments form. Does nothing if no appointment is selected in table view.
     * @throws Exception
     */
    public void modifyAppointmentButtonListener() throws Exception {
        // Get selected appointment
        int selectedIndex = appointmentTableView.getSelectionModel().getSelectedIndex();

        // If nothing selected, display error message and return
        if (selectedIndex == -1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, lz.get("No appointment selected to modify."), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Create add product form
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AppointmentForm.fxml"));
        loader.setController(new AppointmentFormController(this, (Appointment)appointmentTableView.getSelectionModel().getSelectedItem()));
        Parent root = loader.load();
        Stage popupwindow = new Stage();

        // Set add product form parameters
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.initStyle(StageStyle.UTILITY);
        popupwindow.setResizable(false);
        popupwindow.setTitle("");

        // Display form
        Scene scene1= new Scene(root, 800, 550);
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();
    }

    /**
     * Deletes selected appointment from database. Does nothing if no appointment is selected in table view.
     * @throws Exception
     */
    public void deleteAppointmentButtonListener() throws Exception {
        // Get selected appointment
        int selectedIndex = appointmentTableView.getSelectionModel().getSelectedIndex();

        // If nothing is selected, display message and return
        if (selectedIndex == -1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, lz.get("No appointment selected to delete."), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        Appointment appointment = filteredAppointmentList.get(selectedIndex);

        // Display confirmation message
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                lz.get("Are you sure you want to delete this appointment?") +
                        "\n\n" +
                        lz.get("Title") + ": " + appointment.getTitle() +
                        "\n" +
                        lz.get("Appointment ID") + ": " + appointment.getAppointmentId() +
                        "\n" +
                        lz.get("Type") + ": " + appointment.getType()
                , ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();

        // If confirmed, delete appointment from database
        if (alert.getResult() == ButtonType.YES) {
            System.out.println("Delete appointment");
            DatabaseManager.deleteAppointment(filteredAppointmentList.getSourceIndex(selectedIndex));
        }
    }

    /**
     * Displays add client form
     * @throws Exception
     */
    public void addClientButtonListener() throws Exception {
        // Create add client form
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientForm.fxml"));
        loader.setController(new ClientFormController(this));
        Parent root = loader.load();
        Stage popupwindow = new Stage();

        // Set add client form parameters
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.initStyle(StageStyle.UTILITY);
        popupwindow.setResizable(false);
        popupwindow.setTitle("");

        // Display form
        Scene scene1= new Scene(root, 800, 425);
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();
    }

    /**
     * Displays modify client form
     * @throws Exception
     */
    public void modifyClientButtonListener() throws Exception {
        // If not client selected, display error message and return
        if (clientsTableView.getSelectionModel().getSelectedIndex() == -1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, lz.get("No client selected to modify."), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Create add client form
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientForm.fxml"));
        loader.setController(new ClientFormController(this, (Customer)clientsTableView.getSelectionModel().getSelectedItem()));
        Parent root = loader.load();
        Stage popupwindow = new Stage();

        // Set add client form parameters
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.initStyle(StageStyle.UTILITY);
        popupwindow.setResizable(false);
        popupwindow.setTitle("");

        // Display form
        Scene scene1= new Scene(root, 800, 425);
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();
    }

    /**
     * Deletes selected client from database. If no client selected, do nothing
     * @throws Exception
     */
    public void deleteClientButtonListener() throws Exception {
        // If nothing is selected, display message and return
        if (clientsTableView.getSelectionModel().getSelectedIndex() == -1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, lz.get("No client selected to delete."), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Display confirmation message
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                lz.get("Are you sure you want to delete this client?") +
                "\n\n" + lz.get("All appointments assigned to this client will be deleted."),
                ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();

        // If confirmed, delete client from database
        if (alert.getResult() == ButtonType.YES) {
            System.out.println("Delete client");
            DatabaseManager.deleteCustomer((Customer)(clientsTableView.getSelectionModel().getSelectedItem()));
        }
    }

    /**
     * Displays Appointment Type report
     * @throws Exception
     */
    public void generateAppointmentReportButtonListener() throws Exception {
        // Create appointment type form
        FXMLLoader loader = new FXMLLoader(getClass().getResource("reports/AppointmentTypeReport.fxml"));
        loader.setController(new AppointmentTypeReportController());
        Parent root = loader.load();
        Stage popupwindow = new Stage();

        // Set report parameters
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.initStyle(StageStyle.UTILITY);
        popupwindow.setResizable(false);
        popupwindow.setTitle("");

        // Display report
        Scene scene1= new Scene(root, 800, 550);
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();
    }

    /**
     * Display Schedule Report report
     * @throws Exception
     */
    public void generateScheduleReportButtonListener() throws Exception {
        // If no contact selected, display error message and return
        if (scheduleContactComboBox.getSelectionModel().getSelectedIndex() == -1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, lz.get("No contact selected."), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Create form
        FXMLLoader loader = new FXMLLoader(getClass().getResource("reports/ScheduleReport.fxml"));
        loader.setController(new ScheduleReportController((Contact)(scheduleContactComboBox.getSelectionModel().getSelectedItem())));
        Parent root = loader.load();
        Stage popupwindow = new Stage();

        // Set report parameters
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.initStyle(StageStyle.UTILITY);
        popupwindow.setResizable(false);
        popupwindow.setTitle("");

        // Display report
        Scene scene1= new Scene(root, 900, 600);
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();
    }

    /**
     * Displays Meetings per Month report
     * @throws Exception
     */
    public void generateMeetingsReportButtonListener() throws Exception {
        // Create report
        FXMLLoader loader = new FXMLLoader(getClass().getResource("reports/MonthlyMeetingsReport.fxml"));
        loader.setController(new MonthlyMeetingsReportController());
        Parent root = loader.load();
        Stage popupwindow = new Stage();

        // Set parameters
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.initStyle(StageStyle.UTILITY);
        popupwindow.setResizable(false);
        popupwindow.setTitle("");

        // Display report
        Scene scene1= new Scene(root, 800, 550);
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();
    }

    /**
     * Logs user out from application and displays login screen
     * @param event
     * @throws IOException
     */
    public void logoutButtonListener(ActionEvent event) throws IOException {
        DatabaseManager.logoutUser();

        Parent parent = FXMLLoader.load(getClass().getResource("LoginScreen.fxml"));
        Scene parentScene = new Scene(parent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.setScene(parentScene);
        window.show();
    }

    /**
     * Listener that changes view when side menu button is clicked
     * Lambda is used to simulate a radio button group on a group of toggle buttons
     * @throws Exception
     */
    private void addSideMenuGroupListener() throws Exception {
        sideMenuGroup.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null)
                oldVal.setSelected(true);
            try {
                changeMenu();
            }
            catch (Exception ex) {
                System.out.println(ex);
            }
        });
    }

    //</editor-fold>

    //<editor-fold desc="Public Methods">

    /**
     * Starts async update of all database items
     * @throws Exception
     */
    public void refreshDb() throws Exception {
        showLoadingScreen(true); // Show loading screen

        // Create async task and don't wait
        Task<Boolean> task = new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {
                DatabaseManager.downloadRecordsFromDatabase();
                return true;
            }

        };

        // On succeed
        task.setOnSucceeded(evt -> {
            showLoadingScreen(false);
            if (isUserLoggingIn) {
                isUserLoggingIn = false;
                displayLoginMessage();
            }
        });

        // On fail
        task.setOnFailed(evt -> {
            showLoadingScreen(false);
        });

        new Thread(task).start();
    }

    //</editor-fold>

    //<editor-fold desc="Private Methods">

    /**
     * Display or hide loading screen
     * @param bool
     */
    private void showLoadingScreen(boolean bool) {
        loadingGridPane.setVisible(bool);
    }

    /**
     * Setup for the clock in the side menu bar
     * Lambda is used to set the date and time in the animation timeline
     */
    private void startClock() {

        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            Date date = new Date();
            DateFormat timeFormat = new SimpleDateFormat("h:mm a");
            DateFormat dayFormat = new SimpleDateFormat("EEEE");
            DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
            timeLabel.setText(timeFormat.format(date));
            dayLabel.setText(dayFormat.format(date));
            dateLabel.setText(dateFormat.format(date));
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

    }

    /**
     * Switches between the three views (Appointments, Clients, Reports)
     * @throws Exception
     */
    private void changeMenu() throws Exception {
        if (scheduleButton.isSelected() == true) {
            initializeAppointmentScreen();
            menuTabPane.getSelectionModel().select(0);
        }
        else if (clientsButton.isSelected() == true) {
            menuTabPane.getSelectionModel().select(1);
        }
        else if (reportsButton.isSelected() == true) {
            initializeReportsScreen();
            menuTabPane.getSelectionModel().select(2);
        }
    }

    /**
     * Updates the title of the Appointment view and updates the appointment filtered list
     */
    private void setAppointmentFilterType() {
        if (weeklyRadioButton.isSelected() == true)
            setWeeklyMode(currentWeekOffset);
        else
            setMonthlyMode(currentMonthOffset);
        setAppointmentFilter();
    }

    /**
     * Updates the filtered list of appointments based upon the selected week or month
     */
    private void setAppointmentFilter() {
        // Search field predicate
        filteredAppointmentList.setPredicate(myObject -> {
            int startDate = Integer.parseInt(myObject.getFilterStartDateAsString());
            int endDate = Integer.parseInt(myObject.getFilterEndDateAsString());

            if (startDate >= appointmentFilterStart && startDate <= appointmentFilterEnd)
                return true;
            else if (endDate >= appointmentFilterStart && endDate <= appointmentFilterEnd)
                return true;

            return false;
        });
    }

    /**
     * Displays login message
     * If there's an upcoming appointment, it informs the users
     * Otherwise, displays generic login message
     */
    private void displayLoginMessage() {
        Appointment upcomingAppointment = ScheduleHelpers.getApproachingAppointment();
        Alert alert;
        try {
            alert = new Alert(Alert.AlertType.CONFIRMATION,
                    lz.get("Welcome! You have an upcoming appointment.") +
                            "\n" +
                            lz.get("Appointment ID") + ": " + upcomingAppointment.getAppointmentId() +
                            "\n" +
                            lz.get("Starts at") + " " + upcomingAppointment.getLocalStartDateAsString(),
                    ButtonType.OK);
        } catch (Exception ex) {
            alert = new Alert(Alert.AlertType.CONFIRMATION,
                    lz.get("Welcome! You do not have any upcoming appointments."),
                    ButtonType.OK);
        }
        alert.showAndWait();
        return;
    }

    /**
     *Sets the appointment filter to weekly and modifies table view title
     * @param weekOffset The week offset from current week
     */
    public void setWeeklyMode(int weekOffset) {
        // Get calendar set to current date and time
        Calendar c = Calendar.getInstance();

        // Get start and end of week from offset
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        c.add(Calendar.DATE, 7* weekOffset);

        DateFormat df = new SimpleDateFormat("MMMM d, yyyy");
        appointmentTitleLabel.setText(lz.get("Week of") + " " + df.format(c.getTime()));

        DateFormat startEndDF = new SimpleDateFormat("yyyyMMdd");
        appointmentFilterStart = Integer.parseInt(startEndDF.format(c.getTime()));

        c.add(Calendar.DATE, 6);
        appointmentFilterEnd = Integer.parseInt(startEndDF.format(c.getTime()));
    }

    /**
     * Sets the appointment filter to monthly and modifies table view title
     * @param monthOffset The month offset from current month
     */
    public void setMonthlyMode(int monthOffset) {

        Calendar c = Calendar.getInstance();

        // Get start and end of month from offset
        Date currentMonth = new Date();
        c.setTime(currentMonth);
        c.add(Calendar.MONTH, monthOffset);

        DateFormat df = new SimpleDateFormat("MMMM yyyy");
        appointmentTitleLabel.setText(df.format(c.getTime()));

        // Set start of month filter
        c.set(Calendar.DAY_OF_MONTH, 1);
        DateFormat startEndDF = new SimpleDateFormat("yyyyMMdd");
        appointmentFilterStart = Integer.parseInt(startEndDF.format(c.getTime()));
        System.out.println(startEndDF.format(c.getTime()));

        // Set end of month filter
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DATE, -1);
        appointmentFilterEnd = Integer.parseInt(startEndDF.format(c.getTime()));
        System.out.println(startEndDF.format(c.getTime()));
    }

    //</editor-fold>
}
