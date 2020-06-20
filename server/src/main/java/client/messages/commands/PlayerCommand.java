package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.anticheat.ReportType;
import constants.GameConstants;
import constants.MapConstants;
import constants.ServerConstants.PlayerGMRank;
import handling.world.World;
import scripting.NPCScriptManager;
import scripting.v1.NewNpcTalkHandler;
import tools.MaplePacketCreator;

/**
 * @author Emilyx3
 */
public class PlayerCommand {

  public static PlayerGMRank getPlayerLevelRequired() {
    return PlayerGMRank.NORMAL;
  }

  public static class STR extends DistributeStatCommands {

    public STR() {
      stat = MapleStat.STR;
    }
  }

  public static class DEX extends DistributeStatCommands {

    public DEX() {
      stat = MapleStat.DEX;
    }
  }

  public static class INT extends DistributeStatCommands {

    public INT() {
      stat = MapleStat.INT;
    }
  }

  public static class LUK extends DistributeStatCommands {

    public LUK() {
      stat = MapleStat.LUK;
    }
  }

  public abstract static class DistributeStatCommands extends CommandExecute {

    protected MapleStat stat = null;
    private static int statLim = 30000;

    private void setStat(MapleCharacter player, int amount) {
      switch (stat) {
        case STR:
          player.getStat().setStr((short) amount);
          player.updateSingleStat(MapleStat.STR, player.getStat().getStr());
          break;
        case DEX:
          player.getStat().setDex((short) amount);
          player.updateSingleStat(MapleStat.DEX, player.getStat().getDex());
          break;
        case INT:
          player.getStat().setInt((short) amount);
          player.updateSingleStat(MapleStat.INT, player.getStat().getInt());
          break;
        case LUK:
          player.getStat().setLuk((short) amount);
          player.updateSingleStat(MapleStat.LUK, player.getStat().getLuk());
          break;
      }
    }

    private int getStat(MapleCharacter player) {
      switch (stat) {
        case STR:
          return player.getStat().getStr();
        case DEX:
          return player.getStat().getDex();
        case INT:
          return player.getStat().getInt();
        case LUK:
          return player.getStat().getLuk();
        default:
          throw new RuntimeException(); // Will never happen.
      }
    }

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(5, "Invalid number entered.");
        return 0;
      }
      int change = 0;
      try {
        change = Integer.parseInt(splitted[1]);
      } catch (NumberFormatException nfe) {
        c.getPlayer().dropMessage(5, "Invalid number entered.");
        return 0;
      }
      if (change <= 0) {
        c.getPlayer().dropMessage(5, "You must enter a number greater than 0.");
        return 0;
      }
      if (c.getPlayer().getRemainingAp() < change) {
        c.getPlayer().dropMessage(5, "You don't have enough AP for that.");
        return 0;
      }
      if (getStat(c.getPlayer()) + change > statLim) {
        c.getPlayer().dropMessage(5, "The stat limit is " + statLim + ".");
        return 0;
      }
      setStat(c.getPlayer(), getStat(c.getPlayer()) + change);
      c.getPlayer().setRemainingAp((c.getPlayer().getRemainingAp() - change));
      c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, Math.min(199, c.getPlayer().getRemainingAp()));
      c.getPlayer().dropMessage(5, "You've " + c.getPlayer().getRemainingAp() + " remaining ability points.");

      return 1;
    }
  }

  public abstract static class OpenNPCCommand extends CommandExecute {

    protected int npc = -1;
    private static int[] npcs = { // Ish yur job to make sure these are in
        // order and correct ;(
        9270035, 9010017, 9000000, 1013105, 1011101, 9000020, 1012117};

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (npc != 1 && c.getPlayer().getMapId() != 910000000) { // drpcash
        // can
        // use
        // anywhere
        for (int i : GameConstants.blockedMaps) {
          if (c.getPlayer().getMapId() == i) {
            c.getPlayer().dropMessage(5, "You may not use this command here.");
            return 0;
          }
        }
        if (c.getPlayer().getLevel() < 10) {
          c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
          return 0;
        }
        if (c.getPlayer().getMap().getSquadByMap() != null || c.getPlayer().getEventInstance() != null
            || c.getPlayer().getMap().getEMByMap() != null || c.getPlayer()
            .getMapId() >= 990000000) {
          c.getPlayer().dropMessage(5, "You may not use this command here.");
          return 0;
        }
        if ((c.getPlayer().getMapId() >= 680000210 && c.getPlayer().getMapId() <= 680000502)
            || (c.getPlayer().getMapId() / 1000 == 980000 && c.getPlayer().getMapId() != 980000000)
            || (c.getPlayer().getMapId() / 100 == 1030008) || (c.getPlayer().getMapId() / 100 == 922010)
            || (c.getPlayer().getMapId() / 10 == 13003000)) {
          c.getPlayer().dropMessage(5, "You may not use this command here.");
          return 0;
        }
      }
      if (c.getPlayer().getConversation() != 0 || MapConstants.isStorylineMap(c.getPlayer()
          .getMapId())/*
       * && c.getPlayer().getCustomQuestStatus(200000)
       * != 2
       */) {
        c.getPlayer().dropMessage(5, "You may not use this command here.");
        return 0;
      }
      NPCScriptManager.getInstance().start(c, npcs[npc]);
      return 1;
    }
  }

  public static class Event extends OpenNPCCommand {

    public Event() {
      npc = 2;
    }
  }

  public static class ea extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      NPCScriptManager.getInstance().dispose(c);
      c.getSession().write(MaplePacketCreator.enableActions());
      return 1;
    }
  }

  public static class dispose extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      NPCScriptManager.getInstance().dispose(c);
      c.getSession().write(MaplePacketCreator.enableActions());
      return 1;
    }
  }

  public static class toggle extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().setSmega();
      return 1;
    }
  }

  public static class Report extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 3) {
        StringBuilder ret = new StringBuilder("Please use @report [ign] [");
        for (ReportType type : ReportType.values()) {
          ret.append(type.theId).append('/');
        }
        ret.setLength(ret.length() - 1);
        c.getPlayer().dropMessage(6, ret.append(']').toString());
        return 0;
      }

      final MapleCharacter other = c.getPlayer().getMap().getCharacterByName(splitted[1]);
      final ReportType type = ReportType.getByString(splitted[2]);
      if (other == null || type == null || (other.isGM() && !c.getPlayer().isGM())
          || other.getName().equals(c.getPlayer().getName())) {
        c.getPlayer().dropMessage(5, "You've entered the wrong character name.");
        return 0;
      }

      return 1;
    }
  }

  public static class DropNx extends OpenNPCCommand {
    public DropNx() {
      npc = 4;
    }
  }

  public static class ap extends CommandExecute {
    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().dcolormsg(4, "You currently have " + c.getPlayer().getRemainingAp() + " remaining AP");// String.valueOf(chr.getRemainingAp()));
      return 0;
    }
  }

  public static class online extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().dropMessage(6, "Total amount of players connected to er:");
      c.getPlayer().dropMessage(6, "" + World.getConnected() + "");
      c.getPlayer().dropMessage(6, "Characters connected to channel " + c.getChannel() + ":");
      c.getPlayer().dropMessage(6, c.getChannelServer().getPlayerStorage().getOnlinePlayers(true));
      return 0;
    }
  }

  public static class Music extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      NewNpcTalkHandler.startConversation(2121005, c);
      return 0;
    }

  }


  public static class Commands extends CommandExecute {
    @Override
    public int execute(MapleClient c, String[] splitted) {
      final MapleCharacter player = c.getPlayer();
      player.dcolormsg(5, "@str / dex / int / luk <val> : add ap to your stats");
      player.dcolormsg(5, "@dropnx : open the npc to drop nx items");
      player.dcolormsg(5, "@music : open the npc to change the music");
      player.dcolormsg(5, "@toggle : turn of smegas");
      player.dcolormsg(5, "@report <player> <offense> : report a player");
      player.dcolormsg(5, "@online : see the online players");
      return 1;
    }
  }

}