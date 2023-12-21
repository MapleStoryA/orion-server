/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package handling.channel.handler.admin;

import client.MapleClient;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;

public final class AdminCommandHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        if (!c.getPlayer().isGameMaster()) {
            return;
        }
        byte mode = packet.readByte();
        switch (mode) {
            case 66:
                c.getPlayer().gainMeso(packet.readInt(), true);
                break;
        }
        /*
        byte mode = packet.readByte();
        switch (mode) {
        	case 0x00:
        	case 0x02:
        		c.getPlayer().setExp(packet.readInt());
        		break;
        	case 18:
        		c.getPlayer().isHidden();
        }

        c.enableActions();
        byte mode = packet.readByte();
        String victim;
        MapleCharacter target;
        switch (mode){
        	case 0x00: // Level1~Level8 & Package1~Package2
        		List<Pair<Integer, Integer>> pList = MapleItemInformationProvider.getInstance().getItemData(packet.readInt()).mobs;
        		for(Pair<Integer, Integer> p : pList){
        			if(Randomizer.nextInt(101) <= p.right){
        				c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(p.left), c.getPlayer().getPosition());
        			}
        		}
        		c.enableActions();
        		break;
        	case 0x01:{ // /d (inv)
        		byte type = packet.readByte();
        		MapleInventory in = c.getPlayer().getInventory(MapleInventoryType.getByType(type));
        		for(short i = 1; i <= in.getSlotLimit(); i++){
        			if(in.getItem(i) != null){
        				//MapleInventoryManipulator.removeItem(c, MapleInventoryType.getByType(type), i, in.getItem(i).getQuantity(), true, false);
        			}
        		}
        		break;
        	}
        	case 0x02: // Exp
        		c.getPlayer().setExp(packet.readInt());
        		break;
        	case 0x03: // /ban <name>
        		//c.getPlayer().yellowMessage("Please use !ban <IGN> <Reason>");
        		break;
        	case 0x04: // /block <name> <duration (in days)> <HACK/BOT/AD/HARASS/CURSE/SCAM/MISCONDUCT/SELL/ICASH/TEMP/GM/IPROGRAM/MEGAPHONE>
        		//c.getPlayer().yellowMessage("Please use !block <IGN> <duration> <GReason>");
        		break;
        	case 0x12: // /h, information by vana (and tele mode f1) ... hide ofcourse
        		// c.getPlayer().toggleHide(false);
        		//c.getPlayer().gmFlyMode = !c.getPlayer().gmFlyMode;
        		//if(c.getPlayer().uiToggle){
        		//	c.sendPacket(UserLocal.disableUI(c.getPlayer().gmFlyMode));
        			//	c.sendPacket(UserLocal.lockUI(c.getPlayer().gmFlyMode));// Make ui invis
        		//}
        		break;
        	case 19: // Entering a map
        		switch (packet.readByte()){
        			case 0:// /u
        				StringBuilder sb = new StringBuilder("USERS ON THIS MAP: ");
        				for(MapleCharacter mc : c.getPlayer().getMap().getCharacters()){
        					sb.append(mc.getName());
        					sb.append(" ");
        				}
        				//c.getPlayer().message(sb.toString());
        				break;
        			case 15:// /uclip and entering a map
        				break;
        		}
        		break;
        	case 0x10: // Send, used to be 0x12
        		victim = packet.readMapleAsciiString();
        		int mapId = packet.readInt();
        		c.getChannelServer().getPlayerStorage().getCharacterByName(victim).changeMap(c.getChannelServer().getMap(mapId));
        		break;
        	case 0x15: // Kill
        		int mobToKill = packet.readInt();
        		int amount = packet.readInt();
        		List<MapleMapObject> monsterx = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        		for(int x = 0; x < amount; x++){
        			MapleMonster monster = (MapleMonster) monsterx.get(x);
        			if(monster.getId() == mobToKill){
        				//	c.getPlayer().getMap().killMonster(monster, c.getPlayer(), true);
        				monster.giveExpToCharacter(c.getPlayer(), monster.getExp(), true, 1);
        			}
        		}
        		break;
        	case 0x16: // Questreset
        		MapleQuest.getInstance(packet.readShort()).reset(c.getPlayer());
        		break;
        	case 0x17: // Summon
        		int mobId = packet.readInt();
        		int quantity = packet.readInt();
        		for(int i = 0; i < quantity; i++){
        			c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), c.getPlayer().getPosition());
        		}
        		break;
        	case 0x18: // Maple & Mobhp
        		int mobHp = packet.readInt();
        		//c.getPlayer().dropMessage("Monsters HP");
        		List<MapleMapObject> monsters = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        		for(MapleMapObject mobs : monsters){
        			MapleMonster monster = (MapleMonster) mobs;
        			if(monster.getId() == mobHp){
        				c.getPlayer().dropMessage(monster.getName() + ": " + monster.getHp());
        			}
        		}
        		break;
        	case 0x1E: // Warn
        		victim = packet.readMapleAsciiString();
        		String message = packet.readMapleAsciiString();
        		target = c.getChannelServer().getPlayerStorage().getCharacterByName(victim);
        		if(target != null){
        			target.getClient().sendPacket(MaplePacketCreator.serverNotice(1, message));
        			c.sendPacket(MaplePacketCreator.getGMEffect(0x1E, (byte) 1));
        		}else{
        			c.sendPacket(MaplePacketCreator.getGMEffect(0x1E, (byte) 0));
        		}
        		break;
        	case 49:// /Artifact Ranking
        		break;
        	case 0x77: // Testing purpose
        		if(packet.available() == 4){
        			log.info(packet.readInt());
        		}else if(packet.available() == 2){
        			log.info(packet.readShort());
        		}else{
        			System.exit(0);
        		}
        		break;
        	case 66: // Testing purpose
        		c.getPlayer().gainMeso(packet.readInt(), true);
        		break;
        	default:
        		break;
        }*/
    }
}
