package docket.db;

import docket.ScheduleHelpers;

import javax.xml.crypto.Data;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Customer {
    private int customerId;
    private String name;
    private String address;
    private String postalCode;
    private String phone;
    private ZonedDateTime createDate;
    private String createdBy;
    private ZonedDateTime lastUpdate;
    private String lastUpdatedBy;
    private int divisionId;

    protected Customer(
            int customerId,
            String name,
            String address,
            String postalCode,
            String phone,
            String createDate,
            String createdBy,
            String lastUpdate,
            String lastUpdatedBy,
            int divisionId
    ) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.postalCode = postalCode;
        this.phone = phone;
        this.createDate = ScheduleHelpers.getStringAsUtcZonedDataTime(createDate);
        this.createdBy = createdBy;
        this.lastUpdate = ScheduleHelpers.getStringAsUtcZonedDataTime(lastUpdate);
        this.lastUpdatedBy = lastUpdatedBy;
        this.divisionId = divisionId;
    }

    public int getCustomerId() { return customerId; }

    public String getName() { return name; }

    public String getAddress() { return address; }

    public String getPostalCode() { return postalCode; }

    public String getPhone() { return phone; }

    public ZonedDateTime getCreateDate() { return createDate; }
    public String getLocalCreateDateAsString() { return getZonedDateAsLocalString(createDate); }

    public String getCreatedBy() { return createdBy; }

    public ZonedDateTime getLastUpdate() { return lastUpdate; }
    public String getLocalLastUpdateAsString() { return getZonedDateAsLocalString(lastUpdate); }

    public String getLastUpdatedBy() { return lastUpdatedBy; }

    public int getDivisionId() { return divisionId; }

    public String getDivisionName() {
        for (FirstLevelDivision division : DatabaseManager.getAllFirstLevelDivisions()) {
            if (division.getDivisionId() == divisionId)
                return division.getName();
        }

        return "";
    }

    public String getCountryName() {
        for (FirstLevelDivision division : DatabaseManager.getAllFirstLevelDivisions()) {
            if (division.getDivisionId() == divisionId) {
                for (Country country : DatabaseManager.getAllCountries()) {
                    if (division.getCountryId() == country.getCountryId())
                        return country.getName();
                }
            }
        }

        return "";
    }

    @Override
    public String toString() {
        return name;
    }

    private String getZonedDateAsLocalString(ZonedDateTime zonedDateTime) {
        ZonedDateTime dateTime = zonedDateTime.toInstant().atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        return dateTime.format(formatter);
    }
}
