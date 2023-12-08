package server.maplevar;

import client.MapleCharacter;
import database.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class SimpleMapleVar implements MapleVar {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMapleVar.class);
    private static final String UPDATE =
            "UPDATE maple_var SET value = ? WHERE maple_key = ? AND character_id = ?";
    private static final String INSERT =
            "INSERT INTO maple_var(character_id, maple_key, value) VALUES(?, ?, ?);";
    private static final String SELECT =
            "SELECT maple_key, value FROM maple_var WHERE character_id = ? AND maple_key = ?";
    private final MapleCharacter player;

    public SimpleMapleVar(MapleCharacter player) {
        super();
        this.player = player;
    }

    @Override
    public void set(String key, String value) {
        if (get(key) != null) {
            try (var con = DatabaseConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement(UPDATE);
                ps.setString(1, value);
                ps.setString(2, key);
                ps.setInt(3, player.getId());
                ps.execute();
                return;
            } catch (SQLException ex) {
                logger.info("Error updating maple var", ex);
                return;
            }
        }
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(INSERT);
            ps.setInt(1, player.getId());
            ps.setString(2, key);
            ps.setString(3, value);
            ps.execute();
        } catch (SQLException ex) {
            logger.info("Error setting maple var", ex);
        }
    }

    @Override
    public String get(String key) {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement st = con.prepareStatement(SELECT);
            st.setInt(1, player.getId());
            st.setString(2, key);
            ResultSet rs = st.executeQuery();
            // it will never return more than one, since key is a PK.
            if (rs.next()) {
                return rs.getString("value");
            }
        } catch (SQLException ex) {
            logger.info("Error setting maple var", ex);
        }
        return null;
    }
}
