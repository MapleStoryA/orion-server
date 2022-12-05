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
		qm.sendNext("What is it? You're not invited, and therefore not welcome, but... I feel a strange aura emanating from you, and I feel like I have to check out what you have to offer.");
	} else if (status == 1) {
		qm.sendNextPrev("#b(You tell her about the Giant Nependeaths.)#k");
	} else if (status == 2) {
		qm.sendNextPrev("Giant Nependeaths? Yes, it's a big problem, but... I don't think it's enough to heavily affect Orbis yet. Wait, where did you say the Giant Nependeaths are again?");
	} else if (status == 3) {
		qm.sendNextPrev("Neglected Strolling Path.");
	} else if (status == 4) {
		qm.sendNextPrev("...Neglected Strolling Path? If the Giant Nependeaths are there, then that must mean someone is trying to enter the Sealed Garden. Who would that be, and why?");
	} else if (status == 5) {
		qm.sendNextPrev("Sealed Garden?");
	} else if (status == 6) {
		qm.sendAcceptDecline("...I can't tell you why the Sealed Garden is important. If you want to know, I'll have to first see if you are worthy of the information. Do you mind if I look into your fate?");
	} else if (status == 7) {
		qm.forceStartQuest();
		qm.sendOk("Well, now let's look into the mirror ball. Hold on one second.");
		qm.dispose();
	}
}