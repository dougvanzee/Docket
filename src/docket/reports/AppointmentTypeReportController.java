package docket.reports;

import docket.db.Appointment;
import docket.db.DatabaseManager;
import docket.localization.lz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.Month;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the appointment type report form
 */
public class AppointmentTypeReportController {

    private ObservableList<Month> monthList = FXCollections.observableArrayList();

    private ObservableList<AppointmentType> appointmentTypes = FXCollections.observableArrayList();

    @FXML
    private Label titleLabel;

    @FXML
    private ComboBox monthComboBox;

    @FXML
    private TableView reportTableView;

    @FXML
    private TableColumn typeColumn;

    @FXML
    private TableColumn quantityColumn;

    /**
     * Constructor
     */
    public AppointmentTypeReportController() { }

    /**
     * Initializer
     * Localization and sets up form
     */
    public void initialize() {
        titleLabel.setText(lz.get("Appointments by Month and Type"));
        typeColumn.setText(lz.get("Meeting Type"));
        quantityColumn.setText(lz.get("Quantity"));
        reportTableView.setPlaceholder(new Label(lz.get("No content in table")));

        monthList.addAll(Month.values());
        if (monthComboBox == null)
            System.out.println("monthComboBox null");
        monthComboBox.setItems(monthList);

        reportTableView.setItems(appointmentTypes);
        reportTableView.setPlaceholder(new Label(lz.get("No types")));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    /**
     * Listener for change in combo box
     */
    public void monthComboBoxListener() {
        getTableData();
    }

    /**
     * Get the data based up selected month in combo box
     */
    private void getTableData() {
        appointmentTypes.clear();

        // If no month selected, do nothing
        if (monthComboBox.getSelectionModel().getSelectedIndex() == -1) {
            return;
        }

        // Gets all appointments of particular month
        for (Appointment appointment : DatabaseManager.getAllAppointments()) {
            if (appointment.getStartDateUTC().getMonth() == (Month)(monthComboBox.getSelectionModel().getSelectedItem())) {
                boolean typeFound = false;
                for (AppointmentType appointmentType : appointmentTypes) {
                    if (appointmentType.getName().equals(appointment.getType().trim())) {
                        typeFound = true;
                        appointmentType.increment();
                        break;
                    }
                }

                if (!typeFound) {
                    appointmentTypes.addAll(new AppointmentType(appointment.getType()));
                }
            }
        }
    }
}
