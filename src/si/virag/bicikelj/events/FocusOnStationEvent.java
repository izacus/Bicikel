package si.virag.bicikelj.events;

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
