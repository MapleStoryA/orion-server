package server.maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReactorDropEntry {

    public int itemId, chance, questid;
    public int assignedRangeStart, assignedRangeLength;

    public ReactorDropEntry(int itemId, int chance, int questid) {
        this.itemId = itemId;
        this.chance = chance;
        this.questid = questid;
    }
}
