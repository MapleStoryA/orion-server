package provider;

import java.util.List;

/** @author Matze */
public interface MapleDataDirectoryEntry extends MapleDataEntry {

    List<MapleDataDirectoryEntry> getSubdirectories();

    List<MapleDataFileEntry> getFiles();

    MapleDataEntry getEntry(String name);
}
