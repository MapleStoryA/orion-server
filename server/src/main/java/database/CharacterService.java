package database;

import client.CharNameAndId;
import client.FinishedAchievements;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.SavedLocations;
import client.SavedSkillMacro;
import client.skill.EvanSkillPoints;
import client.skill.SkillMacro;
import handling.world.guild.GuildManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.maps.SavedLocationType;

@Slf4j
public class CharacterService {

    public static void deleteWhereCharacterId(Connection con, String sql, int id)
            throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public static void deleteWhereCharacterName(Connection con, String sql, String name)
            throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, name);
        ps.executeUpdate();
        ps.close();
    }

    public static void saveAchievement(
            FinishedAchievements finishedAchievements, int account_id, int characterId) {
        try (var con = DatabaseConnection.getConnection()) {
            if (finishedAchievements.isChanged()) {
                var ps = con.prepareStatement("DELETE FROM achievements WHERE accountid = ?");
                ps.setInt(1, account_id);
                ps.executeUpdate();
                ps.close();
                ps =
                        con.prepareStatement(
                                "INSERT INTO achievements(charid, achievementid, accountid)"
                                        + " VALUES(?, ?, ?)");
                for (Integer achievementid : finishedAchievements.getFinishedAchievements()) {
                    ps.setInt(1, characterId);
                    ps.setInt(2, achievementid);
                    ps.setInt(3, account_id);
                    ps.executeUpdate();
                }
                ps.close();
            }
        } catch (Exception ex) {
            log.error("Error saving achievement", ex);
        }
    }

    public static void saveSkillMacro(SavedSkillMacro savedSkillMacro, int characterId) {
        try (var con = DatabaseConnection.getConnection()) {
            deleteWhereCharacterId(
                    con, "DELETE FROM skillmacros WHERE characterid = ?", characterId);
            for (int i = 0; i < 5; i++) {
                final SkillMacro macro = savedSkillMacro.getSkillMacros()[i];
                if (macro != null) {
                    var ps =
                            con.prepareStatement(
                                    "INSERT INTO skillmacros (characterid, skill1, skill2, skill3,"
                                        + " name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    ps.setInt(1, characterId);
                    ps.setInt(2, macro.getSkill1());
                    ps.setInt(3, macro.getSkill2());
                    ps.setInt(4, macro.getSkill3());
                    ps.setString(5, macro.getName());
                    ps.setInt(6, macro.getShout());
                    ps.setInt(7, i);
                    ps.execute();
                    ps.close();
                }
            }
        } catch (Exception e) {
            log.error("Error when saving skill macros", e);
        }
    }

    public static EvanSkillPoints loadEvanSkills(int id) {
        EvanSkillPoints sp = new EvanSkillPoints();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try (var con = DatabaseConnection.getConnection()) {
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
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps =
                    con.prepareStatement(
                            "UPDATE accounts SET banned = 0 and banreason = '' WHERE id = ?");
            ps.setInt(1, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error while unbanning" + e);
        }
    }

    public static int deleteCharacter(final int cid, int accId) {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps =
                    con.prepareStatement(
                            "SELECT guildid, guildrank, name FROM characters WHERE id = ? AND"
                                    + " accountid = ?");
            ps.setInt(1, cid);
            ps.setInt(2, accId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return 9;
            }
            if (rs.getInt("guildid") > 0) { // is in a guild when deleted
                if (rs.getInt("guildrank") == 1) { // cant delete when leader
                    rs.close();
                    ps.close();
                    return 22;
                }
                GuildManager.deleteGuildCharacter(rs.getInt("guildid"), cid);
            }

            final String name = rs.getString("name");
            CharacterService.deleteWhereCharacterName(
                    con, "DELETE FROM `notes` WHERE `to` = ?", name);
            CharacterService.deleteWhereCharacterName(
                    con, "DELETE FROM `notes` WHERE `from` = ?", name);

            rs.close();
            ps.close();

            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM characters WHERE id = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM monsterbook WHERE charid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM hiredmerch WHERE characterid = ?", cid);
            // CharacterService.deleteWhereCharacterId(con, "DELETE FROM cheatlog WHERE characterid
            // = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM mountdata WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM inventoryitems WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM famelog WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM famelog WHERE characterid_to = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM wishlist WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM buddyentries WHERE owner = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM buddyentries WHERE buddyid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM keymap WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM savedlocations WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM skills WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM mountdata WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM skillmacros WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM queststatus WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM customqueststatus WHERE characterid = ?", cid);
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM inventoryslot WHERE characterid = ?", cid);
            return 0;
        } catch (Exception e) {
            log.info("Log_Packet_Except.rtf", e);
            e.printStackTrace();
        }
        return 10;
    }

    public static List<CharNameAndId> loadCharactersInternal(int serverId, int accountId) {
        List<CharNameAndId> chars = new LinkedList<>();
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps =
                    con.prepareStatement(
                            "SELECT id, name FROM characters WHERE accountid = ? AND world = ?");
            ps.setInt(1, accountId);
            ps.setInt(2, serverId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            log.info("error loading characters internal" + e);
        }
        return chars;
    }

    public static List<MapleCharacter> loadCharacters(
            MapleClient client, final int serverId, int accountId) {
        final List<MapleCharacter> chars = new LinkedList<>();
        final List<Integer> allowedChar = new LinkedList<>();

        for (final CharNameAndId cni : loadCharactersInternal(serverId, accountId)) {
            final MapleCharacter chr = MapleCharacter.loadCharFromDB(cni.id, client, false);
            chars.add(chr);
            allowedChar.add(chr.getId());
        }
        return chars;
    }

    public static boolean checkIfCharacterExist(int accountId, int characterId) {
        return LoginService.loadCharacterData(accountId, characterId) != null;
    }

    public static void saveLocation(SavedLocations savedLocations, int id) {
        if (savedLocations.isChanged()) {
            try (var con = DatabaseConnection.getConnection()) {
                deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?", id);
                var ps =
                        con.prepareStatement(
                                "INSERT INTO savedlocations (characterid, `locationtype`, `map`)"
                                        + " VALUES (?, ?, ?)");
                ps.setInt(1, id);
                for (final SavedLocationType savedLocationType : SavedLocationType.values()) {
                    if (savedLocations.getSavedLocation(savedLocationType.getValue()) != -1) {
                        ps.setInt(2, savedLocationType.getValue());
                        ps.setInt(3, savedLocations.getSavedLocation(savedLocationType.getValue()));
                        ps.execute();
                    }
                }
                ps.close();
            } catch (Exception e) {

            }
        }
    }
}
