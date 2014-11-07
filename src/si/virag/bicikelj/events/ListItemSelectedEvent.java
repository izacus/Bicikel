package si.virag.bicikelj.events;

public class ListItemSelectedEvent {
    public final int stationId;

    public ListItemSelectedEvent(int stationId) {
        this.stationId = stationId;
    }
}
