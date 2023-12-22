package database;

import static database.LoginResult.ALREADY_LOGGED_IN;
import static database.LoginResult.INCORRECT_PASSWORD;
import static database.LoginResult.NOT_REGISTERED_ID;

import client.MapleCoolDownValueHolder;
import client.MapleJob;
import client.MapleQuestStatus;
import client.anticheat.ReportType;
import client.inventory.IItem;
import client.inventory.ItemLoader;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.skill.EvanSkillPoints;
import client.skill.ISkill;
import client.skill.SkillEntry;
import handling.world.buddy.BuddyListEntry;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.result.ResultIterable;
import server.quest.MapleQuest;
import tools.collection.Pair;

@Slf4j
public class LoginService {

    public static CharacterData loadCharacterData(int characterId) {
        try (var handle = DatabaseConnection.getConnector().open()) {
            var result = handle.select("SELECT * FROM characters WHERE id = ?", characterId);
            ResultIterable<CharacterData> accountData = result.mapToBean(CharacterData.class);
            return accountData.findOne().orElse(null);
        }
    }

    public static CharacterData loadCharacterData(int accountId, int characterId) {
        try (var handle = DatabaseConnection.getConnector().open()) {
            var result =
                    handle.select("SELECT * FROM characters WHERE accountid = ? AND id = ?", accountId, characterId);
            ResultIterable<CharacterData> accountData = result.mapToBean(CharacterData.class);
            return accountData.findOne().orElse(null);
        }
    }

    public static CharacterListResult loadCharacterList(int accountId, int world) {
        try (var handle = DatabaseConnection.getConnector().open()) {
            var result =
                    handle.select("SELECT * FROM characters WHERE accountid = ? AND world =" + " ?", accountId, world);
            var characterDataList =
                    result.mapToBean(CharacterData.class).stream().collect(Collectors.toList());
            for (var characterData : characterDataList) {
                var inventory = new MapleInventory[MapleInventoryType.values().length];
                for (MapleInventoryType type : MapleInventoryType.values()) {
                    inventory[type.ordinal()] = new MapleInventory(type);
                }
                try {
                    for (Pair<IItem, MapleInventoryType> mit : ItemLoader.INVENTORY
                            .loadItems(true, characterData.getId())
                            .values()) {
                        var current = inventory[mit.getRight().ordinal()];
                        current.addFromDB(mit.getLeft());
                    }
                    characterData.setInventory(inventory);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            return new CharacterListResult(characterDataList);
        }
    }

    public static void updateAccountCash(int accountId, int nx, int maplePoints) {
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.execute(
                    "UPDATE accounts SET `nxCredit` = ?, `mPoints` = ? WHERE" + " id = ?", nx, maplePoints, accountId);
        }
    }

    public static void saveSkillCoolDowns(int characterId, List<MapleCoolDownValueHolder> cd) {
        var query = "INSERT INTO skills_cooldowns (charid, SkillID, StartTime, length)" + " VALUES (?, ?, ?, ?)";
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.inTransaction(h -> {
                for (final MapleCoolDownValueHolder cooling : cd) {
                    h.execute(query, characterId, cooling.getSkillId(), cooling.getStartTime(), cooling.getLength());
                }
                return true;
            });
        }
    }

    public static void saveEvanSkills(int characterId, EvanSkillPoints evanSkillPoints) {
        var deleteQuery = "DELETE FROM evan_skillpoints where characterid = ?";
        String query = evanSkillPoints.prepareSkillQuery(characterId);
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.inTransaction(h -> {
                h.execute(deleteQuery, characterId);
                h.execute(query);
                return true;
            });
        } catch (Exception e) {
            log.error("Cannot save evan skills", e);
        }
    }

    public static void saveBuddyEntries(int ownerId, Collection<BuddyListEntry> buddyList) {
        var deleteQuery = "DELETE FROM buddyentries WHERE owner = ?";
        String query = "INSERT INTO `buddyentries` (owner, `buddyid`, `groupName`) VALUES (?, ?, ?) ";
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.inTransaction(h -> {
                h.execute(deleteQuery, ownerId);
                for (BuddyListEntry entry : buddyList) {
                    h.execute(query, ownerId, entry.getCharacterId(), entry.getGroup());
                }
                return true;
            });
        } catch (Exception e) {
            log.error("Cannot save buddy entries", e);
        }
    }

    public static void saveReports(int characterId, Map<ReportType, Integer> reports) {
        var deleteQuery = "DELETE FROM reports WHERE characterid = ?";
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.inTransaction((h -> {
                h.execute(deleteQuery, characterId);
                for (Map.Entry<ReportType, Integer> report : reports.entrySet()) {
                    h.execute(
                            "INSERT INTO reports VALUES(DEFAULT, ?, ?, ?)",
                            characterId,
                            report.getKey().i,
                            report.getValue());
                }
                return true;
            }));
        } catch (Exception e) {
            log.error("Cannot save reports", e);
        }
    }

    public static AccountData loadAccountDataById(int accountId) {
        try (var handle = DatabaseConnection.getConnector().open()) {
            var result = handle.select("SELECT * FROM accounts WHERE id = ?", accountId);
            ResultIterable<AccountData> accountData = result.mapToBean(AccountData.class);
            return accountData.findOne().orElse(null);
        }
    }

    public static AccountData loadAccountDataByName(String accountName) {
        try (var handle = DatabaseConnection.getConnector().open()) {
            var result = handle.select("SELECT * FROM accounts WHERE name = ?", accountName);
            ResultIterable<AccountData> accountData = result.mapToBean(AccountData.class);
            return accountData.findOne().orElse(null);
        }
    }

    public static LoginResult checkPassword(String name, String password) {
        try (var handle = DatabaseConnection.getConnector().open()) {
            var result = handle.select("SELECT * FROM accounts WHERE name = ?", name)
                    .mapToBean(AccountData.class)
                    .findOne();
            if (result.isEmpty()) {
                return new LoginResult(NOT_REGISTERED_ID, null);
            }

            AccountData accountData = result.get();
            if (accountData.isOnline()) {
                return new LoginResult(ALREADY_LOGGED_IN, accountData);
            }

            if (accountData.getBanned() > 0) {
                return new LoginResult(9, accountData);
            }

            int loginStatus = -1;
            var encryptor = new PasswordEncryptor();
            var passwordMatches = encryptor.verifyPassword(password, accountData.getPassword(), accountData.getSalt());
            if (passwordMatches) {
                loginStatus = 0;
            } else {
                loginStatus = INCORRECT_PASSWORD;
            }
            return new LoginResult(loginStatus, accountData);
        }
    }

    private static EvanSkillPoints loadEvanSkills(int characterId) {
        EvanSkillPoints sp = new EvanSkillPoints();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try (var con = DatabaseConnection.getConnection()) {
            ps = con.prepareStatement("SELECT * FROM evan_skillpoints WHERE characterid = ?");
            ps.setInt(1, characterId);
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

    public static MapleInventory[] loadInventory(int charId) {
        var inventory = new MapleInventory[MapleInventoryType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values()) {
            inventory[type.ordinal()] = new MapleInventory(type);
        }
        try {
            for (Pair<IItem, MapleInventoryType> mit :
                    ItemLoader.INVENTORY.loadItems(false, charId).values()) {
                var current = inventory[mit.getRight().ordinal()];
                current.addFromDB(mit.getLeft());
            }
            return inventory;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setClientAccountLoginState(AccountData data, LoginState loginState, String sessionIp) {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin ="
                    + " CURRENT_TIMESTAMP() WHERE id = ?");
            ps.setInt(1, loginState.getCode());
            ps.setString(2, sessionIp);
            ps.setInt(3, data.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("error updating login state", e);
        }
    }

    public static boolean ban(String id, String reason, boolean useAccountId, int gmlevel) {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps;
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.execute();
                ps.close();
                return true;
            }
            if (useAccountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }
            boolean ret = false;
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int z = rs.getInt(1);
                PreparedStatement psb = con.prepareStatement(
                        "UPDATE accounts SET banned = 1, banreason = ? WHERE id = ? AND gm" + " < ?");
                psb.setString(1, reason);
                psb.setInt(2, z);
                psb.setInt(3, gmlevel);
                psb.execute();
                psb.close();

                if (gmlevel > 100) { // admin ban
                    PreparedStatement psa = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                    psa.setInt(1, z);
                    ResultSet rsa = psa.executeQuery();
                    if (rsa.next()) {
                        String sessionIP = rsa.getString("sessionIP");
                        if (sessionIP != null && sessionIP.matches("/[0-9]{1,3}\\..*")) {
                            PreparedStatement psz = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                            psz.setString(1, sessionIP);
                            psz.execute();
                            psz.close();
                        }
                        if (rsa.getString("macs") != null) {
                            String[] macData = rsa.getString("macs").split(", ");
                            if (macData.length > 0) {
                                BanService.banMacs(macData);
                            }
                        }
                    }
                    rsa.close();
                    psa.close();
                }
                ret = true;
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
            log.error("error while banning", ex);
        }
        return false;
    }

    public static void updateQuestStatus(int id, Map<MapleQuest, MapleQuestStatus> quests) {
        String deleteQuery = "DELETE FROM queststatus WHERE characterid = ?";
        String questStatusQuery = "INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`,"
                + " `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT," + " ?, ?, ?, ?, ?, ?)";
        String questMobsQuery = "INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)";
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.inTransaction(h -> {
                h.execute(deleteQuery, id);
                for (final MapleQuestStatus q : quests.values()) {
                    var update = h.createUpdate(questStatusQuery)
                            .bind(0, id)
                            .bind(1, q.getQuest().getId())
                            .bind(2, q.getStatus())
                            .bind(3, (int) (q.getCompletionTime() / 1000))
                            .bind(4, q.getForfeited())
                            .bind(5, q.getCustomData());
                    Long generatedKey = update.executeAndReturnGeneratedKeys("queststatusid")
                            .mapTo(Long.class)
                            .one();
                    update.close();
                    if (q.hasMobKills()) {
                        for (int mob : q.getMobKills().keySet()) {
                            h.execute(questMobsQuery, generatedKey, mob, q.getMobKills(mob));
                        }
                    }
                }
                return true;
            });
        } catch (Exception ex) {
            log.error("error updating quest status", ex);
        }
    }

    public static void updateSkills(int id, Map<ISkill, SkillEntry> skills) {
        String deleteQuery = "DELETE FROM skills WHERE characterid = ?";
        String skillQuery = "INSERT INTO skills (characterid, skillid, skilllevel, masterlevel,"
                + " expiration) VALUES (?, ?, ?, ?, ?)";
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.inTransaction(h -> {
                h.execute(deleteQuery, id);
                for (final Map.Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
                    int skillId = skill.getKey().getId();
                    var update = h.createUpdate(skillQuery)
                            .bind(0, id)
                            .bind(1, skillId)
                            .bind(2, skill.getValue().skillevel)
                            .bind(3, skill.getValue().masterlevel)
                            .bind(4, skill.getValue().expiration);
                    update.execute();
                    update.close();
                }
                return true;
            });
        } catch (Exception ex) {
            log.error("error updating quest status", ex);
        }
    }

    public static void updateQuestInfo(int id, Map<Integer, String> questInfo) {
        String deleteQuery = "DELETE FROM questinfo WHERE characterid = ?";
        String insertQuery = "INSERT INTO questinfo (`characterid`, `quest`, `customData`)" + " VALUES (?, ?, ?)";
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.inTransaction(h -> {
                h.execute(deleteQuery, id);
                for (final Map.Entry<Integer, String> q : questInfo.entrySet()) {
                    h.execute(insertQuery, id, q.getKey(), q.getValue());
                }
                return true;
            });
        } catch (Exception ex) {
            log.error("error updating quest info", ex);
        }
    }

    public static void saveInventorySlot(int id, MapleInventory[] inventory) {
        var equip = inventory[MapleInventoryType.EQUIP.ordinal()].getSlotLimit();
        var use = inventory[MapleInventoryType.USE.ordinal()].getSlotLimit();
        var setup = inventory[MapleInventoryType.SETUP.ordinal()].getSlotLimit();
        var etc = inventory[MapleInventoryType.ETC.ordinal()].getSlotLimit();
        var cash = inventory[MapleInventoryType.CASH.ordinal()].getSlotLimit();
        String deleteQuery = "DELETE FROM inventoryslot WHERE characterid = ?";
        String insertQuery =
                "INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`,`etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)";
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.inTransaction(h -> {
                h.execute(deleteQuery, id);
                h.execute(insertQuery, id, equip, use, setup, etc, cash);
                return true;
            });
        } catch (Exception ex) {
            log.error("error saving inventory slot", ex);
        }
    }

    public static void saveDefaultInventorySlot(int id) {
        String insertQuery =
                "INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`,`etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)";
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.inTransaction(h -> {
                h.execute(insertQuery, id, 60, 60, 60, 60, 60);
                return true;
            });
        } catch (Exception ex) {
            log.error("error saving inventory slot", ex);
        }
    }

    public static void saveDefaultMountData(int id) {
        String insertQuery = "INSERT INTO mountdata (characterid, `Level`, `Exp`, `Fatigue`) VALUES" + " (?, ?, ?, ?)";
        try (var handle = DatabaseConnection.getConnector().open()) {
            handle.inTransaction(h -> {
                h.execute(insertQuery, id, 1, 0, 0);
                return true;
            });
        } catch (Exception ex) {
            log.error("error saving default mount data", ex);
        }
    }
}
