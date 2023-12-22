package database;

import client.MapleCoolDownValueHolder;
import client.MapleJob;
import client.anticheat.ReportType;
import client.inventory.IItem;
import client.inventory.ItemLoader;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.skill.EvanSkillPoints;
import handling.world.buddy.BuddyListEntry;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.result.ResultIterable;
import tools.collection.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static database.LoginResult.ALREADY_LOGGED_IN;
import static database.LoginResult.INCORRECT_PASSWORD;
import static database.LoginResult.NOT_REGISTERED_ID;

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
            handle.execute("UPDATE accounts SET `nxCredit` = ?, `mPoints` = ? WHERE" + " id = ?", nx, maplePoints, accountId);
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
                    h.execute("INSERT INTO reports VALUES(DEFAULT, ?, ?, ?)", characterId, report.getKey().i, report.getValue());
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
}
