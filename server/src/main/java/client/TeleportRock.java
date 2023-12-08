package client;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import tools.data.output.OutPacket;

public class TeleportRock {

    @Getter private Set<Integer> map_ids;

    @Getter private boolean changed;

    @Getter private String name;

    private int maxMaps;

    public TeleportRock(boolean vip) {
        this.maxMaps = vip ? 10 : 5;
        this.name = vip ? "trocklocations" : "regrocklocations";
        this.map_ids = new LinkedHashSet<>(this.maxMaps);
    }

    public void deleteMap(int map) {
        map_ids.remove(map);
        changed = true;
    }

    public void addMap(int mapId) {
        if (mapId == 0 || mapId == 999999999) {
            return;
        }
        this.map_ids.add(mapId);
        changed = true;
    }

    public void initMaps(int[] maps) {
        for (var map : maps) {
            addMap(map);
        }
    }

    public boolean hasMap(int mapId) {
        return map_ids.contains(mapId);
    }

    public int[] toArray() {
        return map_ids.stream().mapToInt(Integer::intValue).toArray();
    }

    public void encode(OutPacket packet) {
        var l = new ArrayList<>(map_ids);
        for (int i = 0; i < maxMaps; i++) {
            if (i < l.size()) {
                packet.writeInt(l.get(i));
            } else {
                packet.writeInt(999999999);
            }
        }
    }
}
