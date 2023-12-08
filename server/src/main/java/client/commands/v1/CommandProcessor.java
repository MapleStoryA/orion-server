package client.commands.v1;

import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants.CommandType;
import constants.ServerConstants.PlayerGMRank;
import database.DatabaseConnection;
import server.config.ServerEnvironment;

import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@lombok.extern.slf4j.Slf4j
@SuppressWarnings("non-varargs")
public class CommandProcessor {

    private static final HashMap<String, CommandObject> commands =
            new HashMap<String, CommandObject>();
    private static final HashMap<Integer, ArrayList<String>> commandList =
            new HashMap<Integer, ArrayList<String>>();

    static {
        Class<?>[] CommandFiles = {
                PlayerCommand.class, GMCommand.class, AdminCommand.class
        };

        for (Class<?> clasz : CommandFiles) {
            try {
                PlayerGMRank rankNeeded =
                        (PlayerGMRank) clasz.getMethod("getPlayerLevelRequired").invoke(null, null);
                Class<?>[] a = clasz.getDeclaredClasses();
                ArrayList<String> cL = new ArrayList<String>();
                for (Class<?> c : a) {
                    try {
                        if (!Modifier.isAbstract(c.getModifiers()) && !c.isSynthetic()) {
                            Object o = c.newInstance();
                            boolean enabled;
                            try {
                                enabled =
                                        c.getDeclaredField("enabled")
                                                .getBoolean(c.getDeclaredField("enabled"));
                            } catch (NoSuchFieldException ex) {
                                enabled = true; // Enable all coded commands by default.
                            }
                            if (o instanceof CommandExecute && enabled) {
                                cL.add(
                                        rankNeeded.getCommandPrefix()
                                                + c.getSimpleName().toLowerCase());
                                commands.put(
                                        rankNeeded.getCommandPrefix()
                                                + c.getSimpleName().toLowerCase(),
                                        new CommandObject(
                                                rankNeeded.getCommandPrefix()
                                                        + c.getSimpleName().toLowerCase(),
                                                (CommandExecute) o,
                                                rankNeeded.getLevel()));
                            }
                        }
                    } catch (Exception ex) {
                        // ex.printStackTrace();
                        log.info("Log_Script_Except.rtf", ex);
                    }
                }
                Collections.sort(cL);
                commandList.put(rankNeeded.getLevel(), cL);
            } catch (Exception ex) {
                // ex.printStackTrace();
                log.info("Log_Script_Except.rtf", ex);
            }
        }
    }

    private static void sendDisplayMessage(MapleClient c, String msg, CommandType type) {
        if (c.getPlayer() == null) {
            return;
        }
        switch (type) {
            case NORMAL:
                c.getPlayer().dropMessage(6, msg);
                break;
            case TRADE:
                c.getPlayer().dropMessage(-2, "Error : " + msg);
                break;
        }
    }

    public static boolean processCommand(MapleClient c, String line, CommandType type) {

        if (client.commands.v2.CommandProcessor.getInstance().processLine(c, line)) {
            return true;
        }

        if (line.charAt(0) == PlayerGMRank.NORMAL.getCommandPrefix()) {
            String[] splitted = line.split(" ");
            splitted[0] = splitted[0].toLowerCase();

            CommandObject co = commands.get(splitted[0]);
            if (co == null || co.getType() != type) {
                sendDisplayMessage(c, "That player command does not exist.", type);
                return true;
            }
            try {
                co.execute(c, splitted); // Don't really care about the return value. ;D

            } catch (Exception e) {
                sendDisplayMessage(c, "There was an error.", type);
                if (c.getPlayer().isGameMaster()) {
                    sendDisplayMessage(c, "Error: " + e, type);
                }
            }
            return true;
        }

        if (c.getPlayer().getGMLevel() > PlayerGMRank.NORMAL.getLevel()
                || ServerEnvironment.isDebugEnabled()) {
            if (line.charAt(0) == PlayerGMRank.DONOR.getCommandPrefix()
                    || line.charAt(0) == PlayerGMRank.GM.getCommandPrefix()
                    || line.charAt(0)
                    == PlayerGMRank.ADMIN
                    .getCommandPrefix()) { // Redundant for now, but in case we
                // change symbols later. This will become
                // extensible.
                String[] splitted = line.split(" ");
                splitted[0] = splitted[0].toLowerCase();

                if (line.charAt(0) == '!') { // GM Commands
                    CommandObject co = commands.get(splitted[0]);
                    if (co == null || co.getType() != type) {
                        sendDisplayMessage(c, "That command does not exist.", type);
                        return true;
                    }
                    if (c.getPlayer().getGMLevel() >= co.getReqGMLevel()
                            || ServerEnvironment.isDebugEnabled()) {
                        int ret = co.execute(c, splitted);
                        if (ret > 0
                                && c.getPlayer() != null) { // incase d/c after command or something
                            logGMCommandToDB(c.getPlayer(), line);
                        }
                    } else {
                        sendDisplayMessage(
                                c, "You do not have the privileges to use that command.", type);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static void logGMCommandToDB(MapleCharacter player, String command) {
        PreparedStatement ps = null;
        try (var con = DatabaseConnection.getConnection()) {
            ps = con.prepareStatement("INSERT INTO gmlog (cid, command, mapid) VALUES (?, ?, ?)");
            ps.setInt(1, player.getId());
            ps.setString(2, command);
            ps.setInt(3, player.getMap().getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            log.info("Log_Packet_Except.rtf", ex);
            // ex.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {

            }
        }
    }
}
