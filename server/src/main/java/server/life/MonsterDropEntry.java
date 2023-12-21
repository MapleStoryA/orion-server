package server.life;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonsterDropEntry {

    public final int itemId, chance, Minimum, Maximum, questid, holdMaximum;

    public MonsterDropEntry(int itemId, int chance, int Minimum, int Maximum, int questid, int holdMaximum) {
        this.itemId = itemId;
        this.chance = chance;
        this.questid = questid;
        this.Minimum = Minimum;
        this.Maximum = Maximum;
        this.holdMaximum = holdMaximum;
    }
}
