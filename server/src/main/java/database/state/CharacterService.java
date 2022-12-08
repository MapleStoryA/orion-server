package database.state;

import client.EvanSkillPoints;
import client.MapleJob;
import database.DatabaseConnection;
import handling.world.guild.GuildManager;
import tools.FileOutputUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CharacterService {

    public static void deleteWhereCharacterId(Connection con, String sql, int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public static void deleteWhereCharacterName(Connection con, String sql, String name) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, name);
        ps.executeUpdate();
        ps.close();
    }

    public static EvanSkillPoints loadEvanSkills(int id) {
        EvanSkillPoints sp = new EvanSkillPoints();
        ResultSet rs = null;
        PreparedStatement ps = null;
        Connection con = DatabaseConnection.getConnection();
        try {
            ps = con.prepareStatement("SELECT * FROM evan_skillpoints WHERE characterid = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                sp.setSkillPoints(MapleJob.EVAN2.getId(), rs.getInt("Evan1"));
                sp.setSkillPoints(MapleJob.EVAN3.getId(), rs.getInt("Evan2"));
                sp.setSkillPoints(MapleJob.EVAN4.getId(), rs.getInt("Evan3"));
                sp.setSkillPoints(MapleJob.EVAN5.getId(), rs.getInt("Evan4"));
                sp.setSkillPoints(MapleJob.EVAN6.getId(), rs.getInt("Evan5"));
                sp.setSkillPoints(MapleJob.EVAN7.getId(), rs.getInt("Evan6"));
                sp.setSkillPoints(MapleJob.EVAN8.getId(), rs.getInt("Evan7"));
                sp.setSkillPoints(MapleJob.EVAN9.getId(), rs.getInt("Evan8"));
                sp.setSkillPoints(MapleJob.EVAN10.getId(), rs.getInt("Evan9"));
                sp.setSkillPoints(MapleJob.EVAN11.getId(), rs.getInt("Evan10"));
            }
            return sp;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void unban(int accId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 0 and banreason = '' WHERE id = ?");
            ps.setInt(1, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error while unbanning" + e);
        }
    }

    public static int deleteCharacter(final int cid, int accId) {
        try {
            final Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT guildid, guildrank, name FROM characters WHERE id = ? AND accountid = ?");
            ps.setInt(1, cid);
            ps.setInt(2, accId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return 9;
            }
            if (rs.getInt("guildid") > 0) { // is in a guild when deleted
                if (rs.getInt("guildrank") == 1) { //cant delete when leader
                    rs.close();
                    ps.close();
                    return 22;
                }
                GuildManager.deleteGuildCharacter(rs.getInt("guildid"), cid);
            }

            final String name = rs.getString("name");
            CharacterService.deleteWhereCharacterName(con, "DELETE FROM `notes` WHERE `to` = ?", name);
            CharacterService.deleteWhereCharacterName(con, "DELETE FROM `notes` WHERE `from` = ?", name);

            rs.close();
            ps.close();

            CharacterService.deleteWhereCharacterId(con, "DELETE FROM characters WHERE id = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM monsterbook WHERE charid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM hiredmerch WHERE characterid = ?", cid);
            //CharacterService.deleteWhereCharacterId(con, "DELETE FROM cheatlog WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid_to = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM buddyentries WHERE owner = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM buddyentries WHERE buddyid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM customqueststatus WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?", cid);
            return 0;
        } catch (Exception e) {
            FileOutputUtil.outputFileError(FileOutputUtil.PacketEx_Log, e);
            e.printStackTrace();
        }
        return 10;
    }

}
