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
		qm.sendNext("Welcome back. How did it go with Orbis? Was it related to the Black Wings? And why do you look so down? Please explain it to me in detail.");
	} else if (status == 1) {
		qm.sendNextPrev("#b(You tell him about what happened to the Seal Stone of Orbis.)#k");
	} else if (status == 2) {
		qm.sendNextPrev("Hmmm. So you're saying there was a Seal Stone for Orbis as well. That's valuable information. It stinks that you wound up losing it, but... no no, I'm not blaming you for it. I just think that the Black Wings were ready for you this time.");
	} else if (status == 3) {
		qm.sendNextPrev("...");
	} else if (status == 4) {
		qm.sendAcceptDecline("Keep your head up! It looks like Lilin #bdeciphered a new skill#k. You should go up to #bRien and meet with Lilin#k, seeing that you also have to explain to her what happened at Orbis.");
	} else if (status == 5) {
		qm.forceStartQuest();
		qm.sendOk("Lilin is also a part of this, and no one knows about your past better than her, so it's always important to #bkeep Lilin in the loop in terms of information being passed around here#k.");
		qm.dispose();
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
		qm.sendNext("Hey Aran it's been awhile. How's the training going? I have just found a new skill, so I was going to call you up here anyway. Good thing you dropped by!");
	} else if (status == 1) {
		qm.sendNextPrev("#b(You tell her about the Seal Stone of Orbis.)#k");
	} else if (status == 2) {
		qm.sendNextPrev("Seal Stone of Orbis... okay, this clears up a lot of things. What the Black Wings are looking for are Seal Stones, and there's more than one. This alone is quite a coup for us.");
	} else if (status == 3) {
		qm.sendNextPrev("But I lost the Seal Stone to them...");
	} else if (status == 4) {
		qm.sendYesNo("I am sure the Black Wings had planned this way in advance. If you think of it that way, even acquiring the Seal Stone of Victoria Island becomes quite a success story. More importantly, please take this skill.");
	} else if (status == 5) {
		qm.forceCompleteQuest();
		qm.teachSkill(21100004, 0, 20);
		qm.sendOk("Right now, the most important thing here is for you to get stronger. Mr. Truth and I will look further into the Seal Stones, and you just concentrate on mastering #bCombo Smash#k.");
		qm.dispose();
	}
}