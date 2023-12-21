package client.inventory;

import database.DatabaseConnection;
import java.awt.*;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import server.MapleItemInformationProvider;
import server.movement.Elem;
import server.movement.MovePath;

@Slf4j
public class MaplePet implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private final int petitemid;
    private String name;
    private int Fh = 0;
    private int stance = 0;
    private int uniqueid;
    private int secondsLeft = 0;
    private Point pos;
    private byte fullness = 100, level = 1;
    private short inventorypos = 0, closeness = 0;
    private boolean summoned = false;

    private MaplePet(final int petitemid, final int uniqueid) {
        this.petitemid = petitemid;
        this.uniqueid = uniqueid;
    }

    private MaplePet(final int petitemid, final int uniqueid, final short inventorypos) {
        this.petitemid = petitemid;
        this.uniqueid = uniqueid;
        this.inventorypos = inventorypos;
    }

    public static MaplePet loadFromDb(final int itemid, final int petid, final short inventorypos) {
        try (var con = DatabaseConnection.getConnection()) {
            final MaplePet ret = new MaplePet(itemid, petid, inventorypos);

            PreparedStatement ps = con.prepareStatement("SELECT * FROM pets WHERE petid = ?"); // Get pet details..
            ps.setInt(1, petid);

            final ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }

            ret.setName(rs.getString("name"));
            ret.setCloseness(rs.getShort("closeness"));
            ret.setLevel(rs.getByte("level"));
            ret.setFullness(rs.getByte("fullness"));
            ret.setSecondsLeft(rs.getInt("seconds"));
            ret.setSummoned(rs.getBoolean("summoned"));

            rs.close();
            ps.close();

            return ret;
        } catch (SQLException ex) {
            Logger.getLogger(MaplePet.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static MaplePet createPet(final int itemid, final int uniqueid) {
        return createPet(
                itemid,
                MapleItemInformationProvider.getInstance().getName(itemid),
                1,
                0,
                100,
                uniqueid,
                itemid == 5000054 ? 18000 : 0);
    }

    public static MaplePet createPet(
            int itemid, String name, int level, int closeness, int fullness, int uniqueid, int secondsLeft) {
        if (uniqueid <= -1) { // wah
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement pse =
                    con.prepareStatement("INSERT INTO pets (petid, name, level, closeness, fullness, seconds,"
                            + " summoned) VALUES (?, ?, ?, ?, ?, ?, ?)");
            pse.setInt(1, uniqueid);
            pse.setString(2, name);
            pse.setByte(3, (byte) level);
            pse.setShort(4, (short) closeness);
            pse.setByte(5, (byte) fullness);
            pse.setInt(6, secondsLeft);
            pse.setBoolean(7, false);
            pse.executeUpdate();
            pse.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        final MaplePet pet = new MaplePet(itemid, uniqueid);
        pet.setName(name);
        pet.setLevel(level);
        pet.setFullness(fullness);
        pet.setCloseness(closeness);
        pet.setSecondsLeft(secondsLeft);

        return pet;
    }

    public final void saveToDb() {
        try (var con = DatabaseConnection.getConnection()) {
            final PreparedStatement ps =
                    con.prepareStatement("UPDATE pets SET name = ?, level = ?, closeness = ?, fullness = ?,"
                            + " seconds = ?, summoned = ? WHERE petid = ?");
            ps.setString(1, name); // Set name
            ps.setByte(2, level); // Set Level
            ps.setShort(3, closeness); // Set Closeness
            ps.setByte(4, fullness); // Set Fullness
            ps.setInt(5, secondsLeft);
            ps.setBoolean(6, summoned);
            ps.setInt(7, uniqueid); // Set ID

            ps.executeUpdate(); // Execute statement

            ps.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final boolean getSummoned() {
        return summoned;
    }

    public final void setSummoned(final boolean summoned) {
        this.summoned = summoned;
    }

    public final short getInventoryPosition() {
        return inventorypos;
    }

    public final void setInventoryPosition(final short inventorypos) {
        this.inventorypos = inventorypos;
    }

    public int getUniqueId() {
        return uniqueid;
    }

    public void setUniqueId(int id) {
        this.uniqueid = id;
    }

    public final short getCloseness() {
        return closeness;
    }

    public final void setCloseness(final int closeness) {
        this.closeness = (short) closeness;
    }

    public final byte getLevel() {
        return level;
    }

    public final void setLevel(final int level) {
        this.level = (byte) level;
    }

    public final byte getFullness() {
        return fullness;
    }

    public final void setFullness(final int fullness) {
        this.fullness = (byte) fullness;
    }

    public final int getFh() {
        return Fh;
    }

    public final void setFh(final int Fh) {
        this.Fh = Fh;
    }

    public final Point getPos() {
        return pos;
    }

    public final void setPos(final Point pos) {
        this.pos = pos;
    }

    public final int getStance() {
        return stance;
    }

    public final void setStance(final int stance) {
        this.stance = stance;
    }

    public final int getPetItemId() {
        return petitemid;
    }

    public final boolean canConsume(final int itemId) {
        final MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        for (final int petId : mii.petsCanConsume(itemId)) {
            if (petId == petitemid) {
                return true;
            }
        }
        return false;
    }

    public final void updatePosition(final MovePath path) {
        for (Elem elem : path.lElem) {
            if (elem.x != 0 || elem.y != 0) {
                setPos(new Point(elem.x, elem.y));
            }
            setStance(elem.bMoveAction);
        }
    }

    public final int getSecondsLeft() {
        return secondsLeft;
    }

    public final void setSecondsLeft(int sl) {
        this.secondsLeft = sl;
    }
}
