package handling.world.guild;

@lombok.extern.slf4j.Slf4j
public class MapleGuildSummary implements java.io.Serializable {

    public static final long serialVersionUID = 3565477792085301248L;
    private final String name;
    private final short logoBG;
    private final byte logoBGColor;
    private final short logo;
    private final byte logoColor;
    private final int allianceid;

    public MapleGuildSummary(MapleGuild g) {
        name = g.getName();
        logoBG = (short) g.getLogoBG();
        logoBGColor = (byte) g.getLogoBGColor();
        logo = (short) g.getLogo();
        logoColor = (byte) g.getLogoColor();
        allianceid = g.getAllianceId();
    }

    public String getName() {
        return name;
    }

    public short getLogoBG() {
        return logoBG;
    }

    public byte getLogoBGColor() {
        return logoBGColor;
    }

    public short getLogo() {
        return logo;
    }

    public byte getLogoColor() {
        return logoColor;
    }

    public int getAllianceId() {
        return allianceid;
    }
}
