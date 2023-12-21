package handling.world.helper;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapleMessenger implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private final MapleMessengerCharacter[] members = new MapleMessengerCharacter[3];
    private final String[] silentLink = new String[3];
    private int id;

    public MapleMessenger(int id, MapleMessengerCharacter chrfor) {
        this.id = id;
        addMem(0, chrfor);
    }

    public void addMem(int pos, MapleMessengerCharacter chrfor) {
        if (members[pos] != null) {
            return;
        }
        members[pos] = chrfor;
    }

    public boolean containsMembers(MapleMessengerCharacter member) {
        return getPositionByName(member.getName()) < 4;
    }

    public void addMember(MapleMessengerCharacter member) {
        int position = getLowestPosition();
        if (position > -1 && position < 4) {
            addMem(position, member);
        }
    }

    public void removeMember(MapleMessengerCharacter member) {
        final int position = getPositionByName(member.getName());
        if (position > -1 && position < 4) {
            members[position] = null;
        }
    }

    public void silentRemoveMember(MapleMessengerCharacter member) {
        final int position = getPositionByName(member.getName());
        if (position > -1 && position < 4) {
            members[position] = null;
            silentLink[position] = member.getName();
        }
    }

    public void silentAddMember(MapleMessengerCharacter member) {
        for (int i = 0; i < silentLink.length; i++) {
            if (silentLink[i] != null && silentLink[i].equalsIgnoreCase(member.getName())) {
                addMem(i, member);
                silentLink[i] = null;
                return;
            }
        }
    }

    public void updateMember(MapleMessengerCharacter member) {
        for (int i = 0; i < members.length; i++) {
            MapleMessengerCharacter chr = members[i];
            if (chr.equals(member)) {
                members[i] = null;
                addMem(i, member);
                return;
            }
        }
    }

    public int getLowestPosition() {
        for (int i = 0; i < members.length; i++) {
            if (members[i] == null) {
                return i;
            }
        }
        return 4;
    }

    public int getPositionByName(String name) {
        for (int i = 0; i < members.length; i++) {
            MapleMessengerCharacter messengerchar = members[i];
            if (messengerchar != null && messengerchar.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return 4;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return 31 + id;
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
        final MapleMessenger other = (MapleMessenger) obj;
        return id == other.id;
    }

    public Collection<MapleMessengerCharacter> getMembers() {
        return Arrays.asList(members);
    }
}
