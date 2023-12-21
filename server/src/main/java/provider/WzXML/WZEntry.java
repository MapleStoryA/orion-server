package provider.WzXML;

import lombok.extern.slf4j.Slf4j;
import provider.MapleDataEntity;
import provider.MapleDataEntry;

@Slf4j
public class WZEntry implements MapleDataEntry {

    private final String name;
    private final int size;
    private final int checksum;
    private final MapleDataEntity parent;
    private int offset;

    public WZEntry(String name, int size, int checksum, MapleDataEntity parent) {
        super();
        this.name = name;
        this.size = size;
        this.checksum = checksum;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getChecksum() {
        return checksum;
    }

    public int getOffset() {
        return offset;
    }

    public MapleDataEntity getParent() {
        return parent;
    }
}
