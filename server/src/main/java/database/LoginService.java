package database;

import client.EvanSkillPoints;
import client.MapleJob;
import client.inventory.IItem;
import client.inventory.ItemLoader;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

import static database.LoginResult.ALREADY_LOGGED_IN;
import static database.LoginResult.INCORRECT_PASSWORD;
import static database.LoginResult.NOT_REGISTERED_ID;


public class LoginService {


    public static CharacterData loadCharacterData(int characterId) {
        var jdbi = Jdbi.create(DatabaseConnection.getConnection());
        var result = jdbi.withHandle((h) -> h.select("SELECT * FROM characters WHERE id = ?", characterId));
        ResultIterable<CharacterData> accountData = result.mapToBean(CharacterData.class);
        return accountData.findOne().orElse(null);
    }

    public static CharacterData loadCharacterData(int accountId, int characterId) {
        var jdbi = Jdbi.create(DatabaseConnection.getConnection());
        var result = jdbi.withHandle((h) -> h.select("SELECT * FROM characters WHERE accountid = ? AND id = ?", accountId, characterId));
        ResultIterable<CharacterData> accountData = result.mapToBean(CharacterData.class);
        return accountData.findOne().orElse(null);
    }

    public static CharacterListResult loadCharacterList(int accountId, int world) {
        var jdbi = Jdbi.create(DatabaseConnection.getConnection());
        var result = jdbi.withHandle((h) -> h.select(
                "SELECT * FROM characters WHERE accountid = ? AND world = ?",
                accountId, world));
        var characterDataList = result.mapToBean(CharacterData.class)
                .stream()
                .collect(Collectors.toList());
        for (var characterData : characterDataList) {
            if (characterData.isEvan()) {
                characterData.setEvanSkillPoints(loadEvanSkills(characterData.getId()));
            }
            MapleInventory[] inventory = loadInventory(characterData.getId());
            characterData.setInventory(inventory);
        }
        return new CharacterListResult(characterDataList);
    }


    public static AccountData loadAccountDataById(int accountId) {
        var jdbi = Jdbi.create(DatabaseConnection.getConnection());
        var result = jdbi.withHandle((h) -> h.select("SELECT * FROM accounts WHERE id = ?", accountId));
        ResultIterable<AccountData> accountData = result.mapToBean(AccountData.class);
        return accountData.findOne().orElse(null);
    }

    public static AccountData loadAccountDataByName(String accountName) {
        var jdbi = Jdbi.create(DatabaseConnection.getConnection());
        var result = jdbi.withHandle((h) -> h.select("SELECT * FROM accounts WHERE name = ?", accountName));
        ResultIterable<AccountData> accountData = result.mapToBean(AccountData.class);
        return accountData.findOne().orElse(null);
    }


    public static LoginResult checkPassword(String name, String password) {
        var jdbi = Jdbi.create(DatabaseConnection.getConnection());
        var query = jdbi.withHandle((h) -> h.select("SELECT * FROM accounts WHERE name = ?", name));
        var result = query.mapToBean(AccountData.class).findFirst();

        if (result.isEmpty()) {
            return new LoginResult(NOT_REGISTERED_ID, null);
        }

        AccountData accountData = result.get();

        if (accountData.isOnline()) {
            return new LoginResult(ALREADY_LOGGED_IN, null);
        }

        int loginStatus = -1;
        boolean passwordOk = accountData.getName().equals(name); // TODO: Add password salt
        if (passwordOk) {
            loginStatus = 0;
        } else {
            loginStatus = INCORRECT_PASSWORD;
        }

        return new LoginResult(loginStatus, accountData);
    }

    private static EvanSkillPoints loadEvanSkills(int characterId) {
        EvanSkillPoints sp = new EvanSkillPoints();
        ResultSet rs = null;
        PreparedStatement ps = null;
        Connection con = DatabaseConnection.getConnection();
        try {
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
            for (Pair<IItem, MapleInventoryType> mit : ItemLoader.INVENTORY.loadItems(false, charId).values()) {
                var current = inventory[mit.getRight().ordinal()];
                current.addFromDB(mit.getLeft());
            }
            return inventory;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static int updateLoginStatus(LoginState loginState, int accountID, String sessionIP) {
        Jdbi jdbi = Jdbi.create(DatabaseConnection.getConnection());
        Integer result = jdbi.withHandle(j -> j.execute("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?", loginState.getCode(), sessionIP, accountID));
        return result;
    }
}
