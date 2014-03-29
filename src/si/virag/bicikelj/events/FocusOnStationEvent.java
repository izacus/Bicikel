package si.virag.bicikelj.events;

/**
 * Created by jernej on 29/03/14.
 */
public class FocusOnStationEvent
{
    private final int id;

    public FocusOnStationEvent(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }
}
