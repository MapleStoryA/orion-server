package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.layout.ExcludedKeyMap;
import client.layout.KeyMapBinding;
import client.skill.ISkill;
import client.skill.SkillFactory;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import networking.data.input.InPacket;
import server.MapleItemInformationProvider;

@lombok.extern.slf4j.Slf4j
public class ChangeKeyMapHandler extends AbstractMaplePacketHandler {

    public static final int NORMAL_KEY_MAP = 0;
    public static final int PET_AUTO_HP = 1;
    public static final int PET_AUTO_MP = 2;

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null || packet.available() < 8) {
            return;
        }
        final int actionType = packet.readInt();
        switch (actionType) {
            case NORMAL_KEY_MAP: {
                final int numChanges = packet.readInt();
                for (int i = 0; i < numChanges; i++) {
                    final int key = packet.readInt();
                    final byte type = packet.readByte();
                    final int action = packet.readInt();

                    if (ExcludedKeyMap.fromKeyValue(action) != null) {
                        continue;
                    }

                    if (type == 1 && action >= 1000) {
                        final ISkill skill = SkillFactory.getSkill(action);
                        if (skill != null
                                && ((!skill.isFourthJob()
                                                && !skill.isBeginnerSkill()
                                                && skill.isInvisible()
                                                && chr.getSkillLevel(skill) <= 0)
                                        || (GameConstants.isLinkedAranSkill(action))
                                        || (action % 10000 < 1000)
                                        || (action >= 91000000))) {
                            continue;
                        }
                        if (!chr.getJob().isSkillBelongToJob(action, chr.isGameMaster())) {
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
                        var binding = new KeyMapBinding(chr.getId(), key, type, action, 0);
                        if (type > 0) { // User changes a skill key
                            binding.setChanged(true);
                            chr.getKeyLayout().setBinding(binding);
                        } else { // User unmaps a key
                            binding.setDeleted(true);
                            chr.getKeyLayout().setBinding(binding);
                        }
                    }
                }
                chr.getKeyLayout().saveKeys();
                break;
            }
            case PET_AUTO_HP:
            case PET_AUTO_MP: {
                final int data = packet.readInt();
                boolean isHp = actionType == 1;

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
