package si.virag.bicikelj.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {

    private static final String PREF_FAVORITES = "favorites";
    private final Context ctx;

    private final Set<Long> favoriteStations;

    public FavoritesManager(Context context) {
        this.ctx = context.getApplicationContext();
        this.favoriteStations = loadPreferences(context);
    }

    public boolean isFavorite(long id) {
        return favoriteStations.contains(id);
    }

    public void setFavorite(long id) {
        favoriteStations.add(id);
        storePreferences(ctx, favoriteStations);
    }

    public void removeFavorite(long id) {
        favoriteStations.remove(id);
        storePreferences(ctx, favoriteStations);
    }


    private static Set<Long> loadPreferences(Context ctx) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (!pm.contains(PREF_FAVORITES)) {
            return new HashSet<>();
        }

        try {
            JSONArray arr = new JSONArray(pm.getString(PREF_FAVORITES, "[]"));
            HashSet<Long> results = new HashSet<>();
            for (int i = 0; i < arr.length(); i++) {
                results.add(arr.getLong(i));
            }
            return results;
        } catch (JSONException e) {
            pm.edit().remove(PREF_FAVORITES).apply();
            return new HashSet<>();
        }
    }

    private static void storePreferences(Context ctx, Set<Long> ids) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(ctx);
        JSONArray arr = new JSONArray();
        for (Long item : ids) {
            arr.put(item);
        }

        pm.edit().putString(PREF_FAVORITES, arr.toString()).apply();
    }
}
