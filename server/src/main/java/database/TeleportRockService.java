package database;

import client.TeleportRock;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TeleportRockService {

    public static void save(TeleportRock teleportRock, int characterId) {
        if (!teleportRock.isChanged()) {
            return;
        }
        try (var con = DatabaseConnection.getConnection()) {
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM " + teleportRock.getName() + " WHERE characterid = ?", characterId);
            for (var map_id : teleportRock.getMap_ids()) {
                var ps = con.prepareStatement(
                        "INSERT INTO " + teleportRock.getName() + " (characterid, mapid) VALUES(?, ?) ");
                ps.setInt(1, characterId);
                ps.setInt(2, map_id);
                ps.execute();
                ps.close();
            }
        } catch (SQLException e) {
            log.error("Could not save teleport rocks", e);
        }
    }
}
