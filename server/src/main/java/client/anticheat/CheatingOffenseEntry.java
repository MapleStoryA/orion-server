package client.anticheat;

@lombok.extern.slf4j.Slf4j
public class CheatingOffenseEntry {

    private final CheatingOffense offense;
    private final int characterid;
    private final long firstOffense;
    private int count = 0;
    private long lastOffense;
    private String param;
    private int dbid = -1;

    public CheatingOffenseEntry(CheatingOffense offense, int characterid) {
        super();
        this.offense = offense;
        this.characterid = characterid;
        firstOffense = System.currentTimeMillis();
    }

    public CheatingOffense getOffense() {
        return offense;
    }

    public int getCount() {
        return count;
    }

    public int getChrfor() {
        return characterid;
    }

    public void incrementCount() {
        this.count++;
        lastOffense = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return lastOffense < (System.currentTimeMillis() - offense.getValidityDuration());
    }

    public int getPoints() {
        return count * offense.getPoints();
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public long getLastOffenseTime() {
        return lastOffense;
    }

    public int getDbId() {
        return dbid;
    }

    public void setDbId(int dbid) {
        this.dbid = dbid;
    }
}
