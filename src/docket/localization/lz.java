package docket.localization;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Static class used to get a localized String
 */
public final class lz {

    // The resource bundle to pull localization form
    private static ResourceBundle rb = ResourceBundle.getBundle("docket/localization/ui", Locale.getDefault());

    /**
     * Constructor
     * Since class is "static", constructor is private
     */
    private lz() { }

    /**
     * Gets localized string
     * @param string String to localize
     * @return Localized String, returns original string if no localization found
     */
    public static String get(String string) {
        try {
            if (Locale.getDefault().getLanguage().equals("fr"))
                return rb.getString(string);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

         return string;
    }
}
