package server.life;

@lombok.extern.slf4j.Slf4j
public class OverrideMonsterStats {

    private long hp;
    private int exp, mp;

    public OverrideMonsterStats() {
        hp = 0;
        exp = 0;
        mp = 0;
    }

    public OverrideMonsterStats(long hp, int mp, int exp, boolean change) {
        this.hp = /*change ? (hp * 3L / 2L) : */ hp;
        this.mp = mp;
        this.exp = exp;
    }

    public OverrideMonsterStats(long hp, int mp, int exp) {
        this(hp, mp, exp, true);
    }

    public int getExp() {
        return exp;
    }

    public void setOExp(int exp) {
        this.exp = exp;
    }

    public long getHp() {
        return hp;
    }

    public void setOHp(long hp) {
        this.hp = hp;
    }

    public int getMp() {
        return mp;
    }

    public void setOMp(int mp) {
        this.mp = mp;
    }
}
