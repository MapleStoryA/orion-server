/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*	
	Author : Biscuit
*/
var status = -1;


function end(mode, type, selection) {
    status++;
    if (mode != 1) {
		if(type == 1 && mode == 0) {
			qm.dispose();
			return;
		}else{
			qm.dispose();
			return;
		}
	}
	
  
    if (status == 0 && !qm.isQuestActive(21301)) {
		qm.sendOk("Who's the thief? That's easy! Which bird loves gems?\r\nCrows of course! #rThief Crow#k's the thief!");
		qm.forceStartQuest();
		qm.dispose();
		return;
	}else{
		qm.debug(status);
		if (status == 0) {
			qm.sendYesNo("Did you slay the Thief Crow? Yippy! You're my master, indeed! Now, give me the Red Jade you found! I'll reattach it and... Wait, why aren't you saying anything? Don't tell me you didn't bring it back...");
			return;
		} else if (status == 1) {
			qm.sendNext("What? You didn't bring back the Red Jade?! Why not?! Did you forget?! Yikes, I never though the Black Mage's curse would turn you into a dummy...");
			return;
		} else if (status == 2) {
			qm.sendNext("No, I can't let this drive me to despair. Now more than ever, I must stay optimistic and alert. Argh...");
			return;
		} else if (status == 3) {
			qm.sendNextPrev("You can go back if you want, but I'm sure the thief has already fled the scene. You'll just have to make a new Red Jade. You've made one before, so you remember the required materials, don't you? So hurry it up.");
			return;
		} else if (status == 4) {
			qm.sendNextPrev("#i4001173#");
			return;
		} else if (status == 5) {
			qm.sendNextPrev("No hope. No dreams. Noooo!");
			return;
		} else if (status == 6) {
			qm.sendNextPrev("#b(Maha is becoming volatile. You should probably leave the premise for now. You're sure Lilin can help you somehow.)#k");
			qm.forceCompleteQuest();
			//qm.startQuest(21302);
			qm.dispose();
			return;
		}
		
	} 
	qm.dispose();
}