package docket.db;

import docket.ScheduleHelpers;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment {

    //<editor-fold desc="Private Members">

    private int appointmentId = -1;
    private String title;
    private String description;
    private String location;
    private String type;
    private ZonedDateTime startDateUTC;
    private ZonedDateTime endDateUTC;
    private ZonedDateTime createDateUTC;
    private String createdBy;
    private ZonedDateTime lastUpdateUTC;
    private String lastUpdatedBy;
    private int customerId;
    private int userId;
    private int contactId;

    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Used when downloading data from database
     * @param appointmentId
     * @param title
     * @param description
     * @param location
     * @param type
     * @param startDate
     * @param endDate
     * @param createDate
     * @param createdBy
     * @param lastUpdate
     * @param lastUpdatedBy
     * @param customerId
     * @param userId
     * @param contactId
     */
    protected Appointment(
            int appointmentId,
            String title,
            String description,
            String location,
            String type,
            String startDate,
            String endDate,
            String createDate,
            String createdBy,
            String lastUpdate,
            String lastUpdatedBy,
            int customerId,
            int userId,
            int contactId
    ) {
        this.appointmentId = appointmentId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.startDateUTC = ScheduleHelpers.getStringAsUtcZonedDataTime(startDate);
        this.endDateUTC = ScheduleHelpers.getStringAsUtcZonedDataTime(endDate);
        this.createDateUTC = ScheduleHelpers.getStringAsUtcZonedDataTime(createDate);
        this.createdBy = createdBy;
        this.lastUpdateUTC = ScheduleHelpers.getStringAsUtcZonedDataTime(lastUpdate);
        this.lastUpdatedBy = lastUpdatedBy;
        this.customerId = customerId;
        this.userId = userId;
        this.contactId = contactId;
    }

    //</editor-fold>

    //<editor-fold desc="Public Getters">

    public int getAppointmentId() { return appointmentId; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getLocation() { return location; }

    public String getType() { return type; }

    public ZonedDateTime getStartDateUTC() { return startDateUTC; }
    public String getStartDateAsString() { return  getZonedDateAsString(startDateUTC); }
    public String getLocalStartDateAsString() { return getZonedDateAsLocalString(startDateUTC); }
    public String getFilterStartDateAsString() { return getLocalDateAsStringHelper(startDateUTC); }
    public Double getStartDateUTCAsDouble() {
        DateTimeFormatter df =DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneId.of("UTC"));
        return Double.parseDouble(startDateUTC.format(df));
    }

    public ZonedDateTime getEndDateUTC() { return endDateUTC; }
    public String getEndDateAsString() { return getZonedDateAsString(endDateUTC); }
    public String getLocalEndDateAsString() { return getZonedDateAsLocalString(endDateUTC); }
    public String getFilterEndDateAsString() { return getLocalDateAsStringHelper(endDateUTC); }

    public ZonedDateTime getCreateDateUTC() { return createDateUTC; }
    public String getCreateDateAsString() { return getZonedDateAsLocalString(createDateUTC); }
    public String getLocalCreateDateAsString() { return getZonedDateAsLocalString(createDateUTC); }

    public String getCreatedBy() { return createdBy; }

    public ZonedDateTime getLastUpdateUTC() { return lastUpdateUTC; }
    public String getLastUpdateAsString() { return getZonedDateAsLocalString(lastUpdateUTC); }
    public String getLocalUpdateDateAsString() { return getZonedDateAsLocalString(lastUpdateUTC); }

    public String getLastUpdatedBy() { return lastUpdatedBy; }

    public int getCustomerId() { return customerId; }

    public int getUserId() { return userId; }

    public int getContactId() { return contactId; }

    //</editor-fold>


    /*
    protected void setAppointment(int appointmentId) { this.appointmentId = appointmentId; }

    public void setTitle(String title) { this.title = title; }

    public void setDescription(String description) { this.description = description; }

    public void setLocation(String location) { this.location = location; }

    public void setType(String type) { this.type = type; }

    public void setStartDate(ZonedDateTime zonedDateTime) {
        startDateUTC = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
    }

    public void setEndDate(ZonedDateTime zonedDateTime) {
        // Make sure time is in UTC
        endDateUTC = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
    }

    public void setCreateDate(ZonedDateTime zonedDateTime) {
        createDateUTC = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
    }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public void setLastUpdate(ZonedDateTime zonedDateTime) {
        lastUpdateUTC = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
    }

    public void setLastUpdatedBy(String lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }

    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public void setUserId(int userId) { this.userId = userId; }

    public void setContactId(int contactId) { this.contactId = contactId; }
    */
    //<editor-fold desc="Private Helpers">

    private String getZonedDateAsString(ZonedDateTime zonedDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return zonedDateTime.format(formatter);
    }

    private String getZonedDateAsLocalString(ZonedDateTime zonedDateTime) {
        ZonedDateTime dateTime = zonedDateTime.toInstant().atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        return dateTime.format(formatter);
    }

    private String getLocalDateAsStringHelper(ZonedDateTime zonedDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        ZonedDateTime dateTime = zonedDateTime.toInstant().atZone(ZoneId.systemDefault());
        return dateTime.format(formatter);
    }

    //</editor-fold>
}
