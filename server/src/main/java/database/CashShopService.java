package database;

import client.WishList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CashShopService {

    public static void saveWishList(WishList wishList, int characterId) {

        if (!wishList.isChanged()) {
            return;
        }

        try (var con = DatabaseConnection.getConnection()) {
            CharacterService.deleteWhereCharacterId(
                    con, "DELETE FROM wishlist WHERE characterid = ?", characterId);
            for (var item : wishList.getItems()) {
                var ps =
                        con.prepareStatement("INSERT INTO wishlist(characterid, sn) VALUES(?, ?) ");
                ps.setInt(1, characterId);
                ps.setInt(2, item);
                ps.execute();
                ps.close();
            }
        } catch (Exception e) {
            log.error("Could not save wish list", e);
        }
    }
}
