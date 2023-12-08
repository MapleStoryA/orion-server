package client.drop;

import java.util.List;
import server.life.MapleMonster;

public interface MapleDropProvider {

    List<MapleDropData> search(MapleMonster monster);
}
