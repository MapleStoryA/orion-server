/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import constants.ServerConstants.PlayerGMRank;
import server.MapleInventoryManipulator;

/**
 * @author Oxysoft
 */
public class DonorCommand {

  public static PlayerGMRank getPlayerLevelRequired() {
    return PlayerGMRank.DONOR;
  }

  public static class buycoco extends CommandExecute {
    @Override
    public int execute(MapleClient c, String[] splitted) {
      final MapleCharacter player = c.getPlayer();
      if (player.getMeso() >= 1600000000) { // 1 B
        if (player.getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
          player.gainMeso(-1600000000, true, true); // item first, then only product LOL
          MapleInventoryManipulator.addById(c, 4000465, (short) 1, "Bought using @buycoco");
          player.dropMessage(5, "You've bought a coconut for 2.1 billion mesos.");
        } else {
          player.dropMessage(5, "Please make some space.");
        }
      } else {
        player.dropMessage(5, "Please make sure that you have 2.1 billion mesos.");
      }
      return 1;
    }
  }

}
