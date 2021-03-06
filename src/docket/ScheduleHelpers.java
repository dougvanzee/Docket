package docket;

import docket.db.Appointment;
import docket.db.DatabaseManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper class
 */
public final class ScheduleHelpers {
    private ScheduleHelpers() { };

    /**
     * Converts time zone of ZonedDateTime to UTC
     * @param date The ZonedDateTime
     * @return Converted ZonedDateTime
     */
    public static ZonedDateTime getStringAsUtcZonedDataTime(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ZonedDateTime.parse(date, dateTimeFormatter.withZone(ZoneId.of("UTC")));
    }

    /**
     * Returns date time string formatted as "yyyy-MM-dd HH:mm:ss"
     * @param zonedDateTime The zoned date time
     * @return The date time as string
     */
    public static String getZonedDateTimeAsUtcString(ZonedDateTime zonedDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
        // ZonedDateTime dateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        return zonedDateTime.format(formatter);
    }

    public static String getZonedDateTimeAsLocalString(ZonedDateTime zonedDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a").withZone(ZoneOffset.systemDefault());
        // ZonedDateTime dateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        return zonedDateTime.format(formatter);
    }

    public static String getLocalHoursFromUtcTime(ZonedDateTime zonedDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH");
        ZonedDateTime dateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
        return dateTime.format(formatter);
    }

    public static String getLocalMinutesFromUtcTime(ZonedDateTime zonedDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm");
        ZonedDateTime dateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
        return dateTime.format(formatter);
    }

    public static String getLocalAmPmFromUtcTime(ZonedDateTime zonedDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a");
        ZonedDateTime dateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
        return dateTime.format(formatter);
    }

    /**
     * Checks to see if a start and end date time overlap with an existing appointment
     * @param startUtc Start date/tome
     * @param endUtc End date/time
     * @param referenceApptId Existing appointment's id, enter -1 if not checking against existing appointment
     * @return True if overlapping an existing appointment, excluding referenced appointment.
     */
    public static boolean bDatesOverlapping(ZonedDateTime startUtc, ZonedDateTime endUtc, int referenceApptId) {
        for (Appointment appointment : DatabaseManager.getAllAppointments()) {
            if (appointment.getAppointmentId() != referenceApptId || referenceApptId == -1) {
                if (isOverlapping(startUtc, endUtc, appointment.getStartDateUTC(), appointment.getEndDateUTC())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check to see if two date ranges overlap
     * @param start1 Start of first date range
     * @param end1 End of first date range
     * @param start2 Start of second date range
     * @param end2 End of second date range
     * @return True if ranges overlap, otherwise false
     */
    private static boolean isOverlapping(
            ZonedDateTime start1,
            ZonedDateTime end1,
            ZonedDateTime start2,
            ZonedDateTime end2) {

        start1 = start1.toInstant().atZone(ZoneId.of("UTC"));
        end1 = end1.toInstant().atZone(ZoneId.of("UTC"));
        start2 = start2.toInstant().atZone(ZoneId.of("UTC"));
        end2 = end2.toInstant().atZone(ZoneId.of("UTC"));

        Date _start1 = Date.from(start1.toInstant());
        Date _end1 = Date.from(end1.toInstant());
        Date _start2 = Date.from(start2.toInstant());
        Date _end2 = Date.from(end2.toInstant());

        return _start1.before(_end2) && _start2.before(_end1);
    }

    /**
     * Checks to see if a date is before another date
     * @param start
     * @param end
     * @return True if start date is before end date, otherwise false
     */
    public static boolean isStartDateBeforeEndDate(ZonedDateTime start, ZonedDateTime end) {
        start = start.toInstant().atZone(ZoneId.of("UTC"));
        end = end.toInstant().atZone(ZoneId.of("UTC"));

        Date _start1 = Date.from(start.toInstant());
        Date _end1 = Date.from(end.toInstant());

        return _start1.before(_end1);
    }

    /**
     * Checks to see if ZoneDateTime range is between 8AM and 10PM US Eastern time during the week
     * @param start Beginning of date range
     * @param end End of date range
     * @return True if date range falls within business hours
     */
    public static boolean isDuringBusinessHours(ZonedDateTime start, ZonedDateTime end) {
        ZoneId zoneId = ZoneId.of("America/New_York");
        start = start.toInstant().atZone(zoneId);
        end = end.toInstant().atZone(zoneId);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        double startInt = Double.parseDouble(start.format(dateTimeFormatter));
        double endInt = Double.parseDouble(end.format(dateTimeFormatter));

        DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        double businessStartInt = Double.parseDouble(start.format(dateformatter) + "0800");
        double businessEndInt = Double.parseDouble(start.format(dateformatter) + "2200");

        System.out.println(startInt);
        System.out.println(businessStartInt);
        System.out.println(endInt);
        System.out.println(businessEndInt);

        boolean bValue = startInt >= businessStartInt
                && startInt <= businessEndInt
                && endInt >= businessStartInt
                && endInt <= businessEndInt;

        System.out.println(bValue);
        return bValue;
    }

    /**
     * Checks to see if there is an appointment scheduled within the next 15 minutes from now
     * @return Returns an appointment scheduled within the next 15 minutes, otherwise returns NULL
     */
    public static Appointment getApproachingAppointment() {
        Appointment foundAppointment = null;

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneId.of("UTC"));
        Double nowTime = Double.parseDouble(ZonedDateTime.now().format(df));
        Double laterTime = nowTime + 15;

        // Loop through existing appointments
        for (Appointment appointment : DatabaseManager.getAllAppointments()) {
            Double startTime = Double.parseDouble(appointment.getStartDateUTC().format(df));
            if (startTime >= nowTime && startTime <= laterTime) {
                foundAppointment = appointment;
                break;
            }
        }

        return foundAppointment;
    }
}
