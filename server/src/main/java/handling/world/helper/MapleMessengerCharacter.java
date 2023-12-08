package handling.world.helper;

import client.MapleCharacter;
import java.io.Serializable;

@lombok.extern.slf4j.Slf4j
public class MapleMessengerCharacter implements Serializable {

    private static final long serialVersionUID = 6215463252132450750L;
    private String name = "";
    private int id = -1;
    private int channel = -1;
    private boolean online = false;

    public MapleMessengerCharacter(MapleCharacter maplechar) {
        this.name = maplechar.getName();
        this.channel = maplechar.getClient().getChannel();
        this.id = maplechar.getId();
        this.online = true;
    }

    public MapleMessengerCharacter() {}

    public int getChannel() {
        return channel;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapleMessengerCharacter other = (MapleMessengerCharacter) obj;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }
}
