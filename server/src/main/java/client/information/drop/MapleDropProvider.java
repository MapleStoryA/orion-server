package client.information.drop;

import server.life.MapleMonster;

import java.util.List;

public interface MapleDropProvider {

  List<MapleDropData> search(MapleMonster monster);
}
