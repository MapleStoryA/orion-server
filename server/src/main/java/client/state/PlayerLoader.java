package client.state;

import database.DatabaseConnection;
import org.jdbi.v3.core.Jdbi;

public class PlayerLoader {


    public static CharacterData loadCharacterData(int characterID) {
        Jdbi jdbi = Jdbi.create(DatabaseConnection.getConnection());
        var result = jdbi.withHandle((h) -> h.select("SELECT * FROM characters WHERE id = ?", characterID));
        return result.mapToBean(CharacterData.class).first();
    }
}
