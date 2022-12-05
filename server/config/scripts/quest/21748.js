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
	
	if (status == 0) {
		qm.sendNext("Aran, I heard you went to Mu Lung to investigate the Black Wings. You must have been working so hard. Did the Black Wings trick you again?");
	} else if (status == 1) {
		qm.sendNextPrev("#b(You tell her about the Seal Stone of Mu Lung.)#k");
	} else if (status == 2) {
		qm.sendNextPrev("What...? So you are the one that left the Seal Stone behind a long time ago? It's okay that the Seal Stone of Mu Lung has been taken away. We still learned something valuable from this!");
	} else if (status == 3) {
		qm.sendNextPrev("Something valuable?");
	} else if (status == 4) {
		qm.sendNextPrev("The fact that the heroes had the Seal Stone means that #bwe may be able to find the Seal Stone if we just look for details about the heroes and piece the puzzle together#k, right? Surely, we'll be able to get our hands on the Seal Stone before the Black Wings!");
	} else if (status == 5) {
		qm.sendNextPrev("Ah, that's right! That's brilliant!");
	} else if (status == 6) {
		qm.sendAcceptDecline("Great! I'm filled with hope now! Here, Aran. Please accept this skill!");
	} else if (status == 7) {
		qm.forceCompleteQuest();
		qm.teachSkill(21100002, 0 ,30);
		qm.sendOk("I'll have to retrace the steps of the heroes now! Tru will take care of the information on the Black Wings, so you just concentrate on mastering the #bFinal Charge#k skill! It will help you crush the Black Wings soon!");
		qm.dispose();
	}
}