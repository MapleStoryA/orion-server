package handling.channel.handler.utils;

import client.MapleCharacter;
import client.inventory.IItem;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.MerchItemPackage;
import tools.collection.Pair;

@Slf4j
public class HiredMerchantHandlerUtils {

    public static final byte checkExistance(final int accid, final int charid) {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps =
                    con.prepareStatement("SELECT * from hiredmerch where accountid = ? OR characterid = ?");
            ps.setInt(1, accid);
            ps.setInt(2, charid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ps.close();
                rs.close();
                return 1;
            }
            rs.close();
            ps.close();
            return 0;
        } catch (SQLException se) {
            return -1;
        }
    }

    public static final boolean check(final MapleCharacter chr, final MerchItemPackage pack) {
        if (chr.getMeso() + pack.getMesos() < 0) {
            return false;
        }
        byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
        for (IItem item : pack.getItems()) {
            final MapleInventoryType invtype = GameConstants.getInventoryType(item.getItemId());
            if (invtype == MapleInventoryType.EQUIP) {
                eq++;
            } else if (invtype == MapleInventoryType.USE) {
                use++;
            } else if (invtype == MapleInventoryType.SETUP) {
                setup++;
            } else if (invtype == MapleInventoryType.ETC) {
                etc++;
            } else if (invtype == MapleInventoryType.CASH) {
                cash++;
            }
        }
        return chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= eq
                && chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() >= use
                && chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= setup
                && chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= etc
                && chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() >= cash;
    }

    public static final boolean deletePackage(final int charid, final int accid, final int packageid) {

        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "DELETE from hiredmerch where characterid = ? OR accountid = ? OR" + " packageid = ?");
            ps.setInt(1, charid);
            ps.setInt(2, accid);
            ps.setInt(3, packageid);
            ps.execute();
            ps.close();
            ItemLoader.HIRED_MERCHANT.saveItems(null, packageid, accid, charid);
            return true;
        } catch (SQLException e) {
            log.info("Error while deleting the package" + e.getMessage());
            return false;
        }
    }

    public static final MerchItemPackage loadItemFrom_Database(final int charid, final int accountid) {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps =
                    con.prepareStatement("SELECT * from hiredmerch where characterid = ? OR accountid = ?");
            ps.setInt(1, charid);
            ps.setInt(2, accountid);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                ps.close();
                rs.close();
                return null;
            }
            final int packageid = rs.getInt("PackageId");

            final MerchItemPackage pack = new MerchItemPackage();
            pack.setPackageid(packageid);
            pack.setMesos(rs.getInt("Mesos"));
            pack.setSentTime(rs.getLong("time"));
            ps.close();
            rs.close();

            Map<Integer, Pair<IItem, MapleInventoryType>> items =
                    ItemLoader.HIRED_MERCHANT.loadItems(false, packageid, accountid, charid);
            if (items != null) {
                List<IItem> iters = new ArrayList<IItem>();
                for (Pair<IItem, MapleInventoryType> z : items.values()) {
                    iters.add(z.left);
                }
                pack.setItems(iters);
            }

            return pack;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
