package si.virag.bicikelj.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.StringTokenizer;

public class DisplayUtils
{
    private static final HashSet<String> DONT_CAPITALIZE;
    private static final HashSet<String> KEEP;

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

    private static boolean shouldCapitalize(String text) {
        return !DONT_CAPITALIZE.contains(text.trim());
    }
}
