package handling.world.helper;

import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;
import server.MapleStatEffect;

@Slf4j
public class PlayerBuffValueHolder implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    public long startTime;
    public MapleStatEffect effect;

    public PlayerBuffValueHolder(final long startTime, final MapleStatEffect effect) {
        this.startTime = startTime;
        this.effect = effect;
    }
}
