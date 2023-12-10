package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BanService {

    public static final byte unban(String characterName) {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, characterName);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final int accountId = rs.getInt(1);
            rs.close();
            ps.close();

            ps = con.prepareStatement("UPDATE accounts SET banned = 0 and banreason = '' WHERE id = ?");
            ps.setInt(1, accountId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error while unbanning" + e);
            return -2;
        }
        return 0;
    }

    public static final void banMacs(String[] macs) {
        try (var con = DatabaseConnection.getConnection()) {

            for (String mac : macs) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)"); ) {
                    ps.setString(1, mac);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    // can fail because of UNIQUE key, we don't care
                }
            }

        } catch (SQLException e) {
            log.error("Could not ban list of macs", e);
        }
    }
}
