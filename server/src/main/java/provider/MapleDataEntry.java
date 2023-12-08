package provider;

/** @author Matze */
public interface MapleDataEntry extends MapleDataEntity {

    String getName();

    int getSize();

    int getChecksum();

    int getOffset();
}
