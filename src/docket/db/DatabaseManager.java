package docket.db;

import docket.ScheduleHelpers;
import docket.security.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

/**
 * Handles all communication with the SQL database
 */
public final class DatabaseManager {

    //<editor-fold desc="Private Members">

    private static ObservableList<Contact> allContacts = FXCollections.observableArrayList();
    private static ObservableList<Customer> allCustomers = FXCollections.observableArrayList();
    private static ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();
    private static ObservableList<Country> allCountries = FXCollections.observableArrayList();
    private static ObservableList<FirstLevelDivision> allFirstLevelDivisions = FXCollections.observableArrayList();

    private static int loggedInUserId = -1; // -1 if a user is not logged in
    private static String loggedInUsername = "";

    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructor
     * Private since class is "static"
     */
    private DatabaseManager() { }

    //</editor-fold>

    //<editor-fold desc="Public Getters">

    /**
     * Gets all contacts in database
     * @return All contacts
     */
    public static ObservableList<Contact> getAllContacts() { return allContacts; }

    /**
     * Gets all customers in database
     * @return All customer
     */
    public static ObservableList<Customer> getAllCustomers() { return allCustomers; }

    /**
     * Gets all appointments in database
     * @return All appointments
     */
    public static ObservableList<Appointment> getAllAppointments() { return allAppointments; }

    /**
     * Gets all countries in database
     * @return All countries
     */
    public static ObservableList<Country> getAllCountries() { return allCountries; }

    /**
     * Gets all first-level divisions
     * @return All first-level divisions
     */
    public static ObservableList<FirstLevelDivision> getAllFirstLevelDivisions() { return allFirstLevelDivisions; }

    /**
     * Gets the username of the logged in user
     * @return Username as string
     */
    public static String getLoggedInUsername() { return loggedInUsername; }

    /**
     * Gets the User ID of the logged in user
     * @return User ID as int
     */
    public static int getLoggedInUserId() { return loggedInUserId; }

    //</editor-fold>

    //<editor-fold desc="Public Methods">

    /**
     * Checks to see if the username and password combination is correct
     * @param username Username
     * @param password Password
     * @return returns true if combo is valid, otherwise false
     * @throws Exception
     */
    public static boolean isUserPassValid(String username, String password) throws Exception {
        Connection conn = connectToDatabase();

        var sql = "SELECT User_ID, Password FROM users WHERE User_Name = ?";

        try (var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("User_ID");
                    String savedPassword = rs.getString("Password");
                    if (password.equals(savedPassword)) {
                        conn.close();
                        setLoggedInUser(userId, username);
                        Logger.logLoginAttempt(username, true);
                        return true;
                    }
                }
            }
        }
        conn.close();

        // Log attempt
        Logger.logLoginAttempt(username, false);

        return false;
    }

    /**
     * Logs user out from records
     */
    public static void logoutUser() {
        loggedInUserId = -1;
        loggedInUsername = "";
    }

    //</editor-fold>

    //<editor-fold desc="Public Methods - Appointments">

    /**
     * Downloads all records from database
     * @throws Exception
     */
    public static void downloadRecordsFromDatabase() throws Exception {
        downloadCountriesFromDB();
        downloadFirstLevelDivisionsFromDB();
        downloadContactsFromDB();
        downloadCustomersFromDB();

        Connection conn = connectToDatabase();

        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        String sql = "SELECT * FROM appointments";

        try (var ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Appointment appointment = new Appointment(
                        rs.getInt("Appointment_ID"),
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getString("Location"),
                        rs.getString("Type"),
                        rs.getString("Start"),
                        rs.getString("End"),
                        rs.getString("Create_Date"),
                        rs.getString("Created_By"),
                        rs.getString("Last_Update"),
                        rs.getString("Last_Updated_By"),
                        rs.getInt("Customer_ID"),
                        rs.getInt("User_ID"),
                        rs.getInt("Contact_ID")
                        );
                appointments.add(appointment);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        conn.close();

        allAppointments.clear();
        allAppointments.addAll(appointments);
    }

    /**
     * Deletes an appointment in the database
     * @param index Index of appointment in allAppointments
     * @throws Exception
     */
    public static void deleteAppointment(int index) throws Exception {
        Appointment appointment = allAppointments.get(index);
        int id = appointment.getAppointmentId();

        Connection conn = connectToDatabase();
        var deleteSql = "DELETE FROM appointments WHERE Appointment_ID = ?";
        try (var ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            System.out.println(result); // 1
        }

        conn.close();
        downloadRecordsFromDatabase(); // Refresh database
    }

    /**
     * Adds a new appointment to the database
     * @param title
     * @param description
     * @param location
     * @param type
     * @param startDate
     * @param endDate
     * @param customerId
     * @param contactId
     * @throws Exception
     */
    public static void addNewAppointment(
            String title,
            String description,
            String location,
            String type,
            String startDate,
            String endDate,
            int customerId,
            int contactId
    ) throws Exception {

        Connection conn = connectToDatabase();

        var sql = "INSERT INTO appointments VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (var ps = conn.prepareStatement(sql)) {
                    ps.setString(1, title);
                    ps.setString(2, description);
                    ps.setString(3, location);
                    ps.setString(4, type);
                    ps.setString(5, startDate);
                    ps.setString(6, endDate);
                    ps.setString(7, ScheduleHelpers.getZonedDateTimeAsUtcString(ZonedDateTime.now()));
                    ps.setString(8, DatabaseManager.getLoggedInUsername());
                    ps.setString(9, ScheduleHelpers.getZonedDateTimeAsUtcString(ZonedDateTime.now()));
                    ps.setString(10, DatabaseManager.getLoggedInUsername());
                    ps.setInt(11, customerId);
                    ps.setInt(12, DatabaseManager.getLoggedInUserId());
                    ps.setInt(13, contactId);

            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("Error with " + ex.getMessage());
        }

        conn.close();
    }

    /**
     * Updates an existing appointment in the database
     * @param id
     * @param title
     * @param description
     * @param location
     * @param type
     * @param startDate
     * @param endDate
     * @param createdBy
     * @param createdOn
     * @param customerId
     * @param contactId
     * @throws Exception
     */
    public static void updateAppointment(
            int id,
            String title,
            String description,
            String location,
            String type,
            String startDate,
            String endDate,
            String createdBy,
            String createdOn,
            int customerId,
            int contactId
    ) throws Exception {

        Connection conn = connectToDatabase();

        var sql = "UPDATE appointments SET " +
                "Title = ?, " +
                "Description = ?, " +
                "Location = ?, " +
                "Type = ?, " +
                "Start = ?, " +
                "End = ?, " +
                "Create_Date = ?, " +
                "Created_By = ?, " +
                "Last_Update = ?, " +
                "Last_Updated_By = ?, " +
                "Customer_ID = ?, " +
                "User_ID = ?, " +
                "Contact_ID = ? " +
                "WHERE Appointment_ID = ?";
        try (var ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, location);
            ps.setString(4, type);
            ps.setString(5, startDate);
            ps.setString(6, endDate);
            ps.setString(7, createdOn);
            ps.setString(8, createdBy);
            ps.setString(9, ScheduleHelpers.getZonedDateTimeAsUtcString(ZonedDateTime.now()));
            ps.setString(10, DatabaseManager.getLoggedInUsername());
            ps.setInt(11, customerId);
            ps.setInt(12, DatabaseManager.getLoggedInUserId());
            ps.setInt(13, contactId);
            ps.setInt(14, id);

            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("Error with " + ex.getMessage());
        }

        conn.close();
    }

    //</editor-fold>

    //<editor-fold desc="Public Methods - Customer">

    /**
     * Downloads all customer records from database
     * @throws Exception
     */
    public static void downloadCustomersFromDB() throws Exception {
        Connection conn = connectToDatabase();

        ObservableList<Customer> customers = FXCollections.observableArrayList();
        String sql = "SELECT * FROM customers";

        try (var ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("Customer_ID"),
                        rs.getString("Customer_Name"),
                        rs.getString("Address"),
                        rs.getString("Postal_Code"),
                        rs.getString("Phone"),
                        rs.getString("Create_Date"),
                        rs.getString("Created_By"),
                        rs.getString("Last_Update"),
                        rs.getString("Last_Updated_By"),
                        rs.getInt("Division_ID")
                );
                customers.add(customer);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        conn.close();

        allCustomers.clear();
        allCustomers.addAll(customers);
    }

    /**
     * Adds new customer to database
     * @param name
     * @param address
     * @param postalCode
     * @param phone
     * @param divisionId
     * @throws Exception
     */
    public static void addNewCustomer(
            String name,
            String address,
            String postalCode,
            String phone,
            int divisionId
    )throws Exception {
        Connection conn = connectToDatabase();

        var sql = "INSERT INTO customers VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (var ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setString(3, postalCode);
            ps.setString(4, phone);
            ps.setString(5, ScheduleHelpers.getZonedDateTimeAsUtcString(ZonedDateTime.now()));
            ps.setString(6, DatabaseManager.getLoggedInUsername());
            ps.setString(7, ScheduleHelpers.getZonedDateTimeAsUtcString(ZonedDateTime.now()));
            ps.setString(8, DatabaseManager.getLoggedInUsername());
            ps.setInt(9, divisionId);

            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("Error with " + ex.getMessage());
        }

        conn.close();
    }

    /**
     * Updates existing customer data in database
     * @param id
     * @param name
     * @param address
     * @param postalCode
     * @param phone
     * @param divisionId
     * @throws Exception
     */
    public static void modifyCustomer(
            int id,
            String name,
            String address,
            String postalCode,
            String phone,
            int divisionId
    ) throws Exception {
        Connection conn = connectToDatabase();

        var sql = "UPDATE customers SET " +
                "Customer_Name = ?, " +
                "Address = ?, " +
                "Postal_Code = ?, " +
                "Phone = ?, " +
                "Last_Update = ?, " +
                "Last_Updated_By = ?, " +
                "Division_ID = ? " +
                "WHERE Customer_ID = ?";
        try (var ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setString(3, postalCode);
            ps.setString(4, phone);
            ps.setString(5, ScheduleHelpers.getZonedDateTimeAsUtcString(ZonedDateTime.now()));
            ps.setString(6, DatabaseManager.getLoggedInUsername());
            ps.setInt(7, divisionId);
            ps.setInt(8, id);

            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println("Error with " + ex.getMessage());
        }

        conn.close();
    }

    /**
     * Deletes a customer from the database
     * @param customer the customer to delete
     * @throws Exception
     */
    public static void deleteCustomer(Customer customer) throws Exception {
        int customerId = customer.getCustomerId();

        deleteAllCustomerAppointments(customer);

        Connection conn = connectToDatabase();
        var deleteSql = "DELETE FROM customers WHERE Customer_ID = ?";
        try (var ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, customerId);
            int result = ps.executeUpdate();
            System.out.println(result); // 1
        }

        conn.close();
        downloadRecordsFromDatabase();
    }

    //</editor-fold>

    //<editor-fold desc="Public Methods - Contacts">

    /**
     * Downloads all contacts from the database
     * @throws Exception
     */
    public static void downloadContactsFromDB() throws Exception {
        Connection conn = connectToDatabase();

        ObservableList<Contact> contacts = FXCollections.observableArrayList();
        String sql = "SELECT * FROM contacts";

        try (var ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Contact contact = new Contact(
                        rs.getInt("Contact_ID"),
                        rs.getString("Contact_Name"),
                        rs.getString("Email")
                );
                contacts.add(contact);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        conn.close();

        allContacts.clear();
        allContacts.addAll(contacts);
    }

    //</editor-fold>

    //<editor-fold desc="Public Methods - Countries & Divisions">

    /**
     * Downloads all countries from database
     * @throws Exception
     */
    public static void downloadCountriesFromDB() throws Exception {
        Connection conn = connectToDatabase();

        ObservableList<Country> countries = FXCollections.observableArrayList();
        String sql = "SELECT * FROM countries";

        try (var ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Country country = new Country(
                        rs.getInt("Country_ID"),
                        rs.getString("Country")
                );
                countries.add(country);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        conn.close();

        allCountries.clear();
        allCountries.addAll(countries);
    }

    /**
     * Downloads all first-level divisions from database
     * @throws Exception
     */
    public static void downloadFirstLevelDivisionsFromDB() throws Exception {
        Connection conn = connectToDatabase();

        ObservableList<FirstLevelDivision> firstLevelDivisions = FXCollections.observableArrayList();
        String sql = "SELECT * FROM first_level_divisions";

        try (var ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                FirstLevelDivision firstLevelDivision = new FirstLevelDivision(
                        rs.getInt("Division_ID"),
                        rs.getString("Division"),
                        rs.getInt("Country_ID")
                );
                firstLevelDivisions.add(firstLevelDivision);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        conn.close();

        allFirstLevelDivisions.clear();
        allFirstLevelDivisions.addAll(firstLevelDivisions);
    }

    //</editor-fold>

    //<editor-fold desc="Private Helpers">

    /**
     * Creates a connection to the SQL database
     * @return Returns a reference to the SQL connection
     * @throws Exception
     */
    private static Connection connectToDatabase() throws Exception{
        Connection conn = null;
        try {
            // 1. Load the data access driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            //2. Connect to the data "library"
            String url = "jdbc:mysql://wgudb.ucertify.com:3306/WJ07ZEC";
            String user= "U07ZEC";
            String passwd= "53689173134";
            conn= DriverManager.getConnection(url, user, passwd);
        } catch (SQLException ex) {
            System.out.println("Error with " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Error with " + ex.getMessage());
        }

        return conn;
    }

    /**
     * Stores username and user ID of logged in user
     * @param userId
     * @param username
     */
    private static void setLoggedInUser(int userId, String username) {
        loggedInUserId = userId;
        loggedInUsername = username;
    }

    /**
     * Deletes all records for a particular customer
     * Used when deleting a customer
     * @param customer The customer who's appointments should be deleted
     * @throws Exception
     */
    private static void deleteAllCustomerAppointments(Customer customer) throws Exception {
        int customerId = customer.getCustomerId();

        Connection conn = connectToDatabase();
        var deleteSql = "DELETE FROM appointments WHERE Customer_ID = ?";
        try (var ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, customerId);
            int result = ps.executeUpdate();
            System.out.println(result); // 1
        }

        conn.close();
    }

    //</editor-fold>
}
