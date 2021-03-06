package docket.reports;

import docket.db.Appointment;
import docket.db.Contact;
import docket.db.DatabaseManager;
import docket.localization.lz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * Controller for the schedule report form
 */
public class ScheduleReportController {
    private Contact contact;

    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();

    @FXML
    private Label titleLabel;

    @FXML
    private TableView reportTableView;

    @FXML
    private TableColumn idColumn;

    @FXML
    private TableColumn titleColumn;

    @FXML
    private TableColumn typeColumn;

    @FXML
    private TableColumn descriptionColumn;

    @FXML
    private TableColumn startColumn;

    @FXML
    private TableColumn endColumn;

    @FXML
    private TableColumn customerColumn;

    /**
     * Constructor
     * @param contact The contact to generate a schedule for.
     */
    public ScheduleReportController(Contact contact) {
        this.contact = contact;
    }

    /**
     * Initializer
     * Localization and sets up the table view
     */
    public void initialize() {
        titleLabel.setText(lz.get("Schedule for") + " " + contact.getName());
        idColumn.setText(lz.get("Appointment ID"));
        titleColumn.setText(lz.get("Title"));
        typeColumn.setText(lz.get("Type"));
        descriptionColumn.setText(lz.get("Description"));
        startColumn.setText(lz.get("Start"));
        endColumn.setText(lz.get("End"));
        customerColumn.setText(lz.get("Customer ID"));

        reportTableView.setItems(appointments);
        reportTableView.setPlaceholder(new Label(lz.get("Nothing scheduled")));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("localStartDateAsString"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("localEndDateAsString"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        getContactSchedule();
    }

    /**
     * Gets the schedule data for the contact
     */
    private void getContactSchedule() {
        // Gets appointments after now
        for (Appointment appointment : DatabaseManager.getAllAppointments()) {
            if (appointment.getContactId() == contact.getId() && isDateAfterNow(appointment.getStartDateUTC()))
                appointments.addAll(appointment);
        }

        // Orders appointment in chronological order
        Comparator<Appointment> comparator = Comparator.comparingDouble(Appointment::getStartDateUTCAsDouble);
        FXCollections.sort(appointments, comparator);
    }

    /**
     * Checks to see if zonedDateTime is after now
     * @param zonedDateTime Date/Time to check
     * @return True if after now
     */
    private boolean isDateAfterNow(ZonedDateTime zonedDateTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneId.of("UTC"));
        Double nowTime = Double.parseDouble(ZonedDateTime.now().format(df));
        Double appointmentStartTime = Double.parseDouble(zonedDateTime.format(df));

        return appointmentStartTime >= nowTime;
    }
}
