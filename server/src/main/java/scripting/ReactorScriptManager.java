package scripting;

import client.MapleClient;
import database.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import lombok.extern.slf4j.Slf4j;
import server.maps.MapleReactor;
import server.maps.ReactorDropEntry;

@Slf4j
public class ReactorScriptManager extends AbstractScriptManager {

    private static final ReactorScriptManager instance = new ReactorScriptManager();

    private final Map<Integer, List<ReactorDropEntry>> drops = new HashMap<Integer, List<ReactorDropEntry>>();

    public static final ReactorScriptManager getInstance() {
        return instance;
    }

    public final void act(final MapleClient c, final MapleReactor reactor) {
        try {
            final Invocable iv = getInvocable("reactor", String.valueOf(reactor.getReactorId()), c);
            if (iv == null) {
                return;
            }
            final ScriptEngine scriptengine = (ScriptEngine) iv;
            ReactorActionManager rm = new ReactorActionManager(c, reactor);
            scriptengine.put("rm", rm);
            iv.invokeFunction("act");
        } catch (Exception e) {
            log.error(
                    "Error executing reactor id: {} name: {} mapId: {}",
                    reactor.getReactorId(),
                    reactor.getName(),
                    c.getPlayer().getMapId(),
                    e);
        }
    }

    public final List<ReactorDropEntry> getDrops(final int reactorId) {
        List<ReactorDropEntry> ret = drops.get(reactorId);
        if (ret != null) {
            return ret;
        }
        List<ReactorDropEntry> drops = loadDrops(reactorId);
        this.drops.put(reactorId, drops);
        return drops;
    }

    public List<ReactorDropEntry> loadDrops(int reactorID) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ReactorDropEntry> listOfDrops = new ArrayList<>();
        try (var con = DatabaseConnection.getConnection()) {
            ps = con.prepareStatement("SELECT * FROM reactordrops WHERE reactorid = ?");
            ps.setInt(1, reactorID);
            rs = ps.executeQuery();
            while (rs.next()) {
                listOfDrops.add(new ReactorDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("questid")));
            }
            rs.close();
            ps.close();
            return listOfDrops;
        } catch (final SQLException e) {
            log.info("Could not retrieve drops for reactor {}", reactorID, e);
            return List.of();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignore) {
                return listOfDrops;
            }
        }
    }

    public final void clearDrops() {
        drops.clear();
    }
}
