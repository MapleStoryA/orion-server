package handling.world.buddy;

import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@lombok.extern.slf4j.Slf4j
public class MapleBuddyList implements Serializable {

    //
    private static final long serialVersionUID = 1413738569L;
    private final Map<Integer, BuddyListEntry> buddies;
    private byte capacity;

    public MapleBuddyList(byte capacity) {
        this.capacity = capacity;
        this.buddies = new LinkedHashMap<>();
    }

    public boolean contains(int characterId) {
        return buddies.containsKey(Integer.valueOf(characterId));
    }

    public byte getCapacity() {
        return capacity;
    }

    public void setCapacity(byte capacity) {
        this.capacity = capacity;
    }

    public BuddyListEntry get(int characterId) {
        return buddies.get(Integer.valueOf(characterId));
    }

    public BuddyListEntry get(String characterName) {
        String lowerCaseName = characterName.toLowerCase();
        for (BuddyListEntry ble : buddies.values()) {
            if (ble.getName().toLowerCase().equals(lowerCaseName)) {
                return ble;
            }
        }
        return null;
    }

    public void put(BuddyListEntry entry) {
        buddies.put(Integer.valueOf(entry.getCharacterId()), entry);
    }

    public void remove(int characterId) {
        buddies.remove(Integer.valueOf(characterId));
    }

    public Collection<BuddyListEntry> getBuddies() {
        return buddies.values();
    }

    public boolean isFull() {
        return buddies.size() >= capacity;
    }

    public int[] getBuddyIds() {
        int[] buddyIds = new int[buddies.size()];
        int i = 0;
        for (BuddyListEntry ble : buddies.values()) {
            buddyIds[i++] = ble.getCharacterId();
        }
        return buddyIds;
    }

    public void loadFromTransfer(final List<BuddyListEntry> buddies) {
        for (BuddyListEntry ii : buddies) {
            put(new BuddyListEntry(ii.getName(), ii.getCharacterId(), ii.getGroup(), -1));
        }
    }

    public void loadFromDb(int characterId) throws SQLException {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps =
                    con.prepareStatement("SELECT b.buddyid, c.name as buddyname, b.groupname FROM buddyentries as b,"
                            + " characters as c WHERE c.id = b.buddyid AND b.owner = ?");
            ps.setInt(1, characterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                put(new BuddyListEntry(rs.getString("buddyname"), rs.getInt("buddyid"), rs.getString("groupname"), -1));
            }
            rs.close();
            ps.close();
        }
    }

    public enum BuddyAddResult {
        BUDDYLIST_FULL,
        ALREADY_ON_LIST,
        OK,
        NOT_FOUND
    }

    public enum BuddyDelResult {
        NOT_ON_LIST,
        IN_CASH_SHOP,
        OK,
        ERROR
    }
}
