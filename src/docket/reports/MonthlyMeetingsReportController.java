package docket.reports;

import docket.db.Appointment;
import docket.db.Contact;
import docket.db.DatabaseManager;
import docket.localization.lz;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

/**
 * Displays the numer of meetings for a particular contact
 */
public class MonthlyMeetingsReportController {
    int thisYear;

    @FXML
    private LineChart meetingsLineChart;

    @FXML
    private NumberAxis meetingsAxis;

    @FXML
    private CategoryAxis monthsAxis;

    /**
     * Constructor
     */
    public MonthlyMeetingsReportController() {
        thisYear = LocalDate.now().getYear();
    }

    /**
     * Initializer
     * Localization and sets up graph
     */
    public void initialize() {
        meetingsLineChart.setTitle(lz.get("Monthly Meetings for") + " " + String.valueOf(thisYear));
        meetingsAxis.setLabel(lz.get("Number of Meetings"));
        monthsAxis.setLabel(lz.get("Months"));

        for (Contact contact : DatabaseManager.getAllContacts()) {
            setChartDate(contact);
        }
    }

    /**
     * Sets up the data in the graph
     * @param contact
     */
    private void setChartDate(Contact contact) {
        XYChart.Series series = new XYChart.Series();
        series.setName(contact.getName());

        String[] months = { lz.get("January"), lz.get("February"), lz.get("March"), lz.get("April"),
                lz.get("May"), lz.get("June"), lz.get("July"), lz.get("August"),
                lz.get("September"), lz.get("October"), lz.get("November"), lz.get("December") };

        for (int i = 0; i < 12; i++) {
            int numberOfDays = 0;
            for (Appointment appointment : DatabaseManager.getAllAppointments()) {
                if (appointment.getStartDateUTC().getMonth() == Month.of(i + 1)
                        && appointment.getStartDateUTC().getYear() == thisYear
                        && appointment.getContactId() == contact.getId()) {
                    numberOfDays++;
                }
            }

            series.getData().add(new XYChart.Data(months[i], numberOfDays));
        }

        meetingsLineChart.getData().add(series);
    }


}
