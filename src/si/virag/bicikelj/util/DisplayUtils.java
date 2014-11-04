package si.virag.bicikelj.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.StringTokenizer;

public class DisplayUtils
{
    private static final HashSet<String> DONT_CAPITALIZE;
    private static final HashSet<String> KEEP;
    private static final int[] COLORS = {
            0xDDC2185B,
            0xDD7B1FA2,
            0xDD512DA8,
            0xDD303F9F,
            0xDD1976D2,
            0xDDEF6C00,
            0xDDE64A19,
            0xDD5D4037,
            0xDD455A64,
            0xDDD50000,
            0xDD1A237E,
            0xDD004D40,
            0xDD01579B,
            0xDDDD2C00
    };

    static
    {
        DONT_CAPITALIZE = new HashSet<>();
        DONT_CAPITALIZE.add("ul.");
        DONT_CAPITALIZE.add("ulica");
        DONT_CAPITALIZE.add("cesta");
        DONT_CAPITALIZE.add("center");
        DONT_CAPITALIZE.add("stadion");
        DONT_CAPITALIZE.add("dvor");
        DONT_CAPITALIZE.add("trg");
        DONT_CAPITALIZE.add("starejših");
        DONT_CAPITALIZE.add("c.");
        DONT_CAPITALIZE.add("cerkev");
        DONT_CAPITALIZE.add("park");
        DONT_CAPITALIZE.add("nabrežje");

        KEEP = new HashSet<>();
        KEEP.add("DDC");
        KEEP.add("PS");
        KEEP.add("BTC");
        KEEP.add("MDB");
        KEEP.add("OF");
        KEEP.add("FF");
    }
	public static String formatDistance(Float distance)
	{
		// Using german locale, because slovene locale is not available on all devices
		// and germany uses same number format
		if (distance < 1200)
		{
			return String.format("%,.1f", distance) + " m";
		}
		else
		{
			return String.format("%,.2f", distance / 1000) + " km";
		}
	}

    public static String getProcessedStationName(String rawName) {
        StringTokenizer tokenizer = new StringTokenizer(rawName.replaceAll("-", " \n "), " \t\r\f", false);
        StringBuilder sb = new StringBuilder();

        boolean firstToken = true;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (KEEP.contains(token)) {
                sb.append(token);
            }
            else {
                token = token.toLowerCase();
                if (firstToken || shouldCapitalize(token)) {
                    if (token.equals("\n")) {
                        sb.append("\n");
                        firstToken = true;
                        continue;
                    }

                    token = token.trim().toLowerCase();
                    if (token.length() == 0) continue;
                    char[] tokenArray = token.toCharArray();
                    tokenArray[0] = Character.toUpperCase(tokenArray[0]);
                    sb.append(tokenArray);
                } else {
                    sb.append(token);
                }
            }

            if (tokenizer.hasMoreTokens())
                sb.append(" ");

            firstToken = false;
        }

        return sb.toString();
    }

    public static String extractLetters(String stationName) {
        if (stationName.contains("\n")) {
            return String.valueOf(stationName.charAt(0)) + stationName.charAt(stationName.indexOf("\n") + 1);
        }

        if (stationName.contains(" ")) {
            return String.valueOf(stationName.charAt(0)) + stationName.charAt(stationName.indexOf(" ") + 1);
        }

        return String.valueOf(stationName.charAt(0));
    }

    private static boolean shouldCapitalize(String text) {
        return !DONT_CAPITALIZE.contains(text.trim());
    }

    public static int getColorFromString(String str) {
        return COLORS[Math.abs(str.hashCode()) % COLORS.length];
    }

}
