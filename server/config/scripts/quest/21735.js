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

function start(mode, type, selection) {
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
	
	if (status == 0) {
		qm.sendNext("Seal Stone of Victoria Island? I got it already. Take a look!\r\n\r\n#i4032323#");
	} else if (status == 1) {
		qm.sendNextPrev("!!\r\n...How did you get this?");
	} else if (status == 2) {
		qm.sendAcceptDecline("After being ambushed by the Puppeteer last time, I used every source of information I could find to look through every single corner of Victoria Island, and that's how I found it. I can't just take it and not dish back, you know? Our goal is to take away what they are looking for first. Wouldn't that be considered a great revenge?");
	} else if (status == 3) {
		qm.forceStartQuest();
		qm.gainItem(4032323, 1);
		qm.sendNext("But the Black Wings already know me. Holding on to this may not be the smartest idea, and you holding on to it might mean losing it in a battle. I think we should let #bLilin#k hold on to it.");
	} else if (status == 4) {
		qm.sendNextPrev("The island of Rien used to be only populated by the Rien race, and it's covered with spells that disable other humans from entering the island, so even someone from the Black Wings will not find it easy to find this place. Tell this to Lilin.");
	} else if (status == 5) {
		qm.sendNextPrev("I will no longer give you tasks that have to do with gathering up information. I think you already know a thing or two about the world of Maple, so... you should now be able to gather up information on your own!");
	} else if (status == 6) {
		qm.sendPrev("If nothing else, I want you to really work on gathering up valuable information on the Black Wings. Furthermore, #bkeep asking around for the existence of the Seal Stone, and let me know if you find anything.#k");
	}
}

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

	if (status == 0) {
		qm.sendNext("I've been receiving updates about the Black Wings from Tru. I heard he even got attacked not too long ago. What about you? Are you alright? Mmm... Is this really the #t4032323#? So Tru did end up finding the #t4032323# before they could.");
	} else if (status == 1) {
		qm.sendYesNo("I don't know what this item even does, but I do know that it has something to do with the Black Mage. As long as they are looking for this, we'll have to protect it. No matter what it takes, you must become stronger.");
	} else if (status == 2) {
		qm.forceCompleteQuest();
		qm.gainItem(4032323, -1);
		qm.teachSkill(21100005, 0, 20);
		qm.sendNext("Okay, the document that was recently deciphered had a new skill called #bCombo Drain#k. You used to use this skill, right? Nowadays, I only need to take just a glimpse of the skills, and I already know if you used it in real combat.");
	} else if (status == 3) {
		qm.sendPrev("Black Wings... I am sure their plan does not end here. Please tell Mr. Truth to keep digging up new information on the Black Wings. As for you, please keep training.");
		qm.dispose();
	}
}