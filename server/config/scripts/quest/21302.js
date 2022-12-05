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
	if(!qm.isQuestActive(21302)){
		if (status == 0) {
			qm.sendOk("You remember where the Mirror of Desire is, right? Head west, out of town, until you reach a dead end near the ice cliff, inside Mirror Cave. There, your task is to find out how to make a #bRed Jade#k, and then make it. The #bMaker Skill#k should come in handy for you.");
			qm.forceStartQuest(21302);
		}
	}else{
		if(status == 0){
			qm.sendYesNo("Okay, now that I have the power of Red Jade. I'll restore more of your abilities. Your level has gotten much higher since the last time we met, so I'm sure I can work with my magic a bit more this time!");
			return;
		}
		if(status == 1){
			qm.gainSp(1);
			qm.gainAp(5);
			qm.changeJobById(2111);
			qm.forceCompleteQuest();
			qm.sendNext("Please get back all of your abilities soon. I want to explore with you like we did in the good old days.");
		}
	}
	 
	qm.dispose();
}