package server.maps;

import client.MapleClient;
import java.awt.*;

public interface MapleMapObject {
    int getObjectId();

    void setObjectId(int id);

    MapleMapObjectType getType();

    Point getPosition();

    void setPosition(Point position);

    void sendSpawnData(MapleClient client);

    void sendDestroyData(MapleClient client);
}
