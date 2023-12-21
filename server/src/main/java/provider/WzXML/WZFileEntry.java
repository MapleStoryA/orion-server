package provider.WzXML;

import lombok.extern.slf4j.Slf4j;
import provider.MapleDataEntity;
import provider.MapleDataFileEntry;

@Slf4j
public class WZFileEntry extends WZEntry implements MapleDataFileEntry {

    private int offset;

    public WZFileEntry(String name, int size, int checksum, MapleDataEntity parent) {
        super(name, size, checksum, parent);
    }

    @Override
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
