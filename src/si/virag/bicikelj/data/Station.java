package si.virag.bicikelj.data;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

import si.virag.bicikelj.util.DisplayUtils;

public class Station {
    @SerializedName("name")
    private String name;

    @SerializedName("latitude")
    private double lat;

    @SerializedName("longitude")
    private double lng;

    @SerializedName("extra")
    private StationExtra extra;

    @SerializedName("free_bikes")
    private int bikes;

    @SerializedName("empty_slots")
    private int free;

    // Current distance - transient state
    @Nullable
    private Float distance = null;

    @Nullable
    private String abbreviation;

    @Nullable
    private Location location;

    @Nullable
    private String prettyName;

    public void setDistance(@Nullable Location currentLocation) {
        if (currentLocation == null) {
            distance = null;
            return;
        }

        distance = getLocation().distanceTo(currentLocation);
    }

    @Nullable
    public Float getDistance() {
        return distance;
    }

    public Location getLocation() {
        if (location == null) {
            location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lng);
        }

        return location;
    }

    public int getId() {
        return extra.id;
    }

    public String getName() {
        if (prettyName == null)
            prettyName = DisplayUtils.getProcessedStationName(name);
        return prettyName;
    }

    public String getAddress() {
        return extra.address;
    }

    public boolean isOpen() {
        return extra.status.equalsIgnoreCase("open");
    }

    public int getTotalSpaces() {
        return extra.totalSpaces;
    }

    public int getFreeSpaces() {
        return free;
    }

    public int getAvailableBikes() {
        return bikes;
    }

    public Calendar getUpdated() {
        return extra.updated;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Station))
            return false;

        Station s = (Station) o;
        return (s.hashCode() == this.hashCode());
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        temp = Double.doubleToLongBits(lat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @NonNull
    public String getAbbreviation() {
        if (abbreviation == null)
            this.abbreviation = DisplayUtils.extractLetters(this.name).toUpperCase();
        return abbreviation;
    }

    private static class StationExtra {
        @SerializedName("address")
        public String address;

        @SerializedName("uid")
        public int id;

        @SerializedName("slots")
        public int totalSpaces;

        @SerializedName("status")
        public String status;

        @SerializedName("last_update")
        public Calendar updated;
    }
}
