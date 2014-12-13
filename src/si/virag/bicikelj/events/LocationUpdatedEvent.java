package si.virag.bicikelj.events;

import android.location.Location;

public class LocationUpdatedEvent {

    public final Location location;

    public LocationUpdatedEvent(Location location) {
        this.location = location;
    }
}
