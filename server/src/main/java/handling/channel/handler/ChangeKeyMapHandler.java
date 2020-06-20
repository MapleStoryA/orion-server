package handling.channel.handler;

import client.*;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.LinkedList;
import java.util.List;

public class ChangeKeyMapHandler extends AbstractMaplePacketHandler {


  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr == null || slea.available() < 8) {
      return;
    }
    final int actiontype = slea.readInt();
    switch (actiontype) {
      case 0: { // Normal Key Map
        final List<Integer> toRemove = new LinkedList<>();
        final int numChanges = slea.readInt();
        for (int i = 0; i < numChanges; i++) {
          final int key = slea.readInt();
          final byte type = slea.readByte();
          final int action = slea.readInt();

          if (ExcludedKeyMap.fromKeyValue(action) != null) {
            continue;
          }

          if (type == 1 && action >= 1000) { // skill
            final ISkill skil = SkillFactory.getSkill(action);
            if (skil != null && ((!skil.isFourthJob() && !skil.isBeginnerSkill() && skil.isInvisible()
                && chr.getSkillLevel(skil) <= 0) || (GameConstants.isLinkedAranSkill(action))
                || (action % 10000 < 1000) || (action >= 91000000))) {
              continue;
            }
            if (!chr.isSkillBelongToJob(action)) {
              continue;
            }
          } else if (type == 2) { // item (All except equip)
            if (GameConstants.getInventoryType(action) == MapleInventoryType.EQUIP) { // impossible
              continue;
            } else if (chr.getItemQuantity(action, false) <= 0) {
              continue;
            }
          }
          if (type >= 0 && type <= 8) { // 0 = none, 1 = skill, 2 = item,
            // 4 = UI, 5 = (attack, loot,
            // sit, jump, npc chat), 6 =
            // emotion
            if (type == 1) { // I don't want to parse the keymap twice.
              chr.changeKeybinding(key, type, action, (byte) 0);
            } else if (type != 0) {
              chr.changeKeybinding(key, type, action, (byte) 0);
            } else { // Type is 0
              toRemove.add(key);
            }
          }
        }
        if (!toRemove.isEmpty()) {
          chr.removeAllKey(toRemove);
        }
        // chr.resetKeyMap();
        break;
      }
      case 1: // Pet Auto HP
      case 2: { // Pet Auto MP
        final int data = slea.readInt();
        boolean isHp = actiontype == 1;

        if (data == 0) {
          return;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!ii.getItemEffect(data).isPotion() || ii.getItemEffect(data).isSkill()) {
          return;
        }
        if (isHp) {
          c.getPlayer().set("PET_HP", String.valueOf(data));
        } else {
          c.getPlayer().set("PET_MP", String.valueOf(data));
        }

        break;
      }
    }

  }

}
