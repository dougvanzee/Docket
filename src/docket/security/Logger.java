package docket.security;

import docket.ScheduleHelpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;

/**
 * A "static" class to log attempts to login
 */
public final class Logger {

    /**
     * Constructor
     * Since the class is "static", constructor is Private
     */
    private Logger() {};

    /**
     * Logs an attempt to login
     * @param username The username that was user to attempt a login
     * @param wasSuccessful Whether login was successful
     * @throws IOException
     */
    public static void logLoginAttempt(String username, boolean wasSuccessful) throws IOException {
        try(FileWriter fileWriter = new FileWriter("login_activity.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter))
        {
            printWriter.println("Username: " + username + "\t" +
                    ScheduleHelpers.getZonedDateTimeAsUtcString(ZonedDateTime.now()) + " UTC\t" +
                    "Was Successful: " + wasSuccessful);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
