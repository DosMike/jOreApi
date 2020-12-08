package de.dosmike.spongepowered.oreapi.utility;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Converts from api timestamp strings to unixtime millis and back.
 */
public class RepositoryTimestamp {
    private static final SimpleDateFormat timestampParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private static final SimpleDateFormat timestampParser2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    static {
        timestampParser.setLenient(true);
        timestampParser2.setLenient(true);
    }

    public static long toNative(String time) {
        try {
            return timestampParser.parse(time).getTime();
        } catch (Exception ignore) {}
        try {
            return timestampParser2.parse(time).getTime();
        } catch (Exception ignore) {}
        throw new RuntimeException("Could not parse time \""+time+"\"");
    }

    public static String fromNative(long unixTime) {
        return timestampParser.format(new Date(unixTime));
    }

}
