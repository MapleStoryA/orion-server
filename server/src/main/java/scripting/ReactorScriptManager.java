package scripting;

import client.MapleClient;
import database.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
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
            System.err.println("Error executing reactor script. ReactorID: "
                    + reactor.getReactorId()
                    + ", ReactorName: "
                    + reactor.getName()
                    + ":"
                    + e);
            final String msg = "Error executing reactor script. ReactorID: "
                    + reactor.getReactorId()
                    + ", ReactorName: "
                    + reactor.getName()
                    + ":"
                    + e;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        }
    }

    public final List<ReactorDropEntry> getDrops(final int rid) {
        List<ReactorDropEntry> ret = drops.get(rid);
        if (ret != null) {
            return ret;
        }
        ret = new LinkedList<>();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try (var con = DatabaseConnection.getConnection()) {
            ps = con.prepareStatement("SELECT * FROM reactordrops WHERE reactorid = ?");
            ps.setInt(1, rid);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(new ReactorDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("questid")));
            }
            rs.close();
            ps.close();
        } catch (final SQLException e) {
            System.err.println("Could not retrieve drops for reactor " + rid + e);
            return ret;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignore) {
                return ret;
            }
        }
        drops.put(rid, ret);
        return ret;
    }

    public final void clearDrops() {
        drops.clear();
    }
}
