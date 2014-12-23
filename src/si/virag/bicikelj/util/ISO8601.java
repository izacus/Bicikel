package si.virag.bicikelj.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Helper class for handling ISO 8601 strings of the following format:
 * "2008-03-01T13:00:00+01:00". It also supports parsing the "Z" timezone.
 */
public final class ISO8601 {

    private static ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<>();

    /** Transform Calendar to ISO 8601 string. */
    public static String fromCalendar(final Calendar calendar) {
        if (sdf.get() == null)
            sdf.set(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));

        Date date = calendar.getTime();
        String formatted = sdf.get().format(date);
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    /** Get current date and time formatted as ISO 8601 string. */
    public static String now() {
        return fromCalendar(GregorianCalendar.getInstance());
    }

    /** Transform ISO 8601 string to Calendar. */
    public static Calendar toCalendar(final String iso8601string) throws ParseException {
        if (iso8601string == null)
            throw new ParseException("Null string.", 0);

        Calendar calendar = GregorianCalendar.getInstance();
        String s = iso8601string.replace("Z", "+00:00");
        try {
            s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Invalid length: " + iso8601string, 0);
        }

        if (sdf.get() == null)
            sdf.set(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        Date date = sdf.get().parse(s);
        calendar.setTime(date);
        return calendar;
    }
}
