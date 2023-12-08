package client;

import java.util.concurrent.ScheduledFuture;
import server.MapleStatEffect;

@lombok.extern.slf4j.Slf4j
public class MapleBuffStatValueHolder {

    private MapleStatEffect effect;
    private long startTime;
    private int value;
    private ScheduledFuture<?> schedule;

    public MapleBuffStatValueHolder(
            MapleStatEffect effect, long startTime, ScheduledFuture<?> schedule, int value) {
        super();
        this.setEffect(effect);
        this.setStartTime(startTime);
        this.setSchedule(schedule);
        this.setValue(value);
    }

    public MapleStatEffect getEffect() {
        return effect;
    }

    public void setEffect(MapleStatEffect effect) {
        this.effect = effect;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public ScheduledFuture<?> getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }
}
