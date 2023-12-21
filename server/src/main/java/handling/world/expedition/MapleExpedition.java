package handling.world.expedition;

import handling.world.party.MapleParty;
import handling.world.party.PartyManager;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapleExpedition {

    private final List<Integer> parties;
    private final ExpeditionType et;
    private final int id;
    private int leaderId;

    public MapleExpedition(ExpeditionType ett, int leaderId, int id) {
        this.et = ett;
        this.id = id;
        this.leaderId = leaderId;
        this.parties = new ArrayList<>(ett.maxParty);
    }

    public ExpeditionType getType() {
        return et;
    }

    public int getLeader() {
        return leaderId;
    }

    public void setLeader(int newLead) {
        this.leaderId = newLead;
    }

    public List<Integer> getParties() {
        return parties;
    }

    public int getId() {
        return id;
    }

    public int getAllMembers() {
        int ret = 0;
        for (int i = 0; i < parties.size(); i++) {
            MapleParty pp = PartyManager.getParty(parties.get(i));
            if (pp == null) {
                parties.remove(i);
            } else {
                ret += pp.getMembers().size();
            }
        }
        return ret;
    }

    public int getFreeParty() {
        for (int i = 0; i < parties.size(); i++) {
            MapleParty pp = PartyManager.getParty(parties.get(i));
            if (pp == null) {
                parties.remove(i);
            } else if (pp.getMembers().size() < 6) {
                return pp.getId();
            }
        }
        if (parties.size() < et.maxParty) {
            return 0;
        }
        return -1;
    }

    public int getIndex(int partyId) {
        for (int i = 0; i < parties.size(); i++) {
            if (parties.get(i) == partyId) {
                return i;
            }
        }
        return -1;
    }
}
