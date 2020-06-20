/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

//Spindle

var status = 0;
var minLevel = 10;
var maxLevel = 999;
var minPlayers = 1;
var maxPlayers = 6;
//var PQItems = new Array(4001022, 4001023);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1) {
			status++;
		} else if (mode == 0 && status == 2) {
			cm.sendOk("Your'e scared? So am I.");
			cm.dispose();
			return;
		} else {
			status--;
		}
		if (cm.getPlayer().getEventInstance() == null) {
			if (status == 0) {
				cm.sendNext("Recently, there has been an attack on #bOmega Sector#k. All the bosses from the HurricaneMS world have entered this dimension, become much stronger, have more HP and are invading #bOmega Sector#k. We need a brave team of people to stop them.");
			} else if (status == 1) {
				cm.sendNextPrev("These monsters come with the power of the #bBoss Hunter#k, a legendary power that comes with the armor they wear. If you are lucky, one of them might cough one up!");
			}else if (status == 2) {
				cm.sendYesNo("Will you help #bOmega Sector?");
			}else if (status == 3) {
				// Slate has no preamble, directly checks if you're in a party
				if (cm.getParty() == null) { // no party
					cm.sendOk("Please talk to me again after you've formed a party.");
					cm.dispose();
	                                return;
				}
				if (!cm.isLeader()) { // not party leader
					cm.sendOk("Please ask your party leader to talk to me.");
					cm.dispose();
				} else {
					// Check teh partyy
					var party = cm.getParty().getMembers();
					var mapId = cm.getChar().getMapId();
					var next = true;
					var levelValid = 0;
					var inMap = 0;
					// Temp removal for testing
					if (party.size() < minPlayers || party.size() > maxPlayers) 
						next = false;
					else {
						for (var i = 0; i < party.size() && next; i++) {
							if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
								levelValid += 1;
							if (party.get(i).getMapid() == mapId)
								inMap += 1;
						}
						if (levelValid < minPlayers || inMap < minPlayers)
							next = false;
					}
					if (next) {
						// Kick it into action.  Slate says nothing here, just warps you in.
						var em = cm.getEventManager("OmegaPQ");
						if (em == null) {
							cm.sendOk("unavailable");
							cm.dispose();
						}
						else {
							// Begin the PQ.
							var eim = em.startInstance(cm.getParty(),cm.getChar().getMap());
                                                       //gabmat007
                                                      // eim.schedule("spawnNextBoss", 1000);
                                                       eim.schedule("spawnNextBoss", 1);
							
						}
						cm.dispose();
					}
					else {
						cm.sendOk("Your party is not a party of one to six.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
						cm.dispose();
					}
				}
			}
			else {
				cm.sendOk("RAWR!?!?!?");
				cm.dispose();
			}
		} else {
			if (status == 0) {
				cm.sendYesNo("Do you want to leave? Is this too much for you?");
			} else if (status == 1) {
				var eim = cm.getPlayer().getEventInstance();
				eim.finishPQ();
				cm.dispose();
			}
		}
	}
}
					
					
