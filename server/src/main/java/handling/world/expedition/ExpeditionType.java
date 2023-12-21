package handling.world.expedition;

public enum ExpeditionType {
    Easy_Balrog(6, 2000, 50, 70),
    Normal_Balrog(15, 2001, 50, 200),
    Zakum(30, 2002, 50, 200),
    Horntail(30, 2003, 80, 200),
    Chaos_Zakum(30, 2005, 100, 200),
    ChaosHT(30, 2006, 110, 200),
    Pink_Bean(30, 2004, 140, 200);
    public int maxMembers, maxParty, exped, minLevel, maxLevel;

    ExpeditionType(int maxMembers, int exped, int minLevel, int maxLevel) {
        this.maxMembers = maxMembers;
        this.exped = exped;
        this.maxParty = (maxMembers / 2 + (maxMembers % 2 > 0 ? 1 : 0));
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public static ExpeditionType getById(int id) {
        for (ExpeditionType pst : values()) {
            if (pst.exped == id) {
                return pst;
            }
        }
        return null;
    }
}
