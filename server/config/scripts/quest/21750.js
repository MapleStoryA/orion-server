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
		qm.sendNext("Aran...? Are my eyes deceiving me? Is it really you, Aran? You're alive? Oh, thank goodness! Thank you, Aran. Thank you!");
	} else if (status == 1) {
		qm.sendNextPrev("I'm very sorry, but I don't remember you.");
	} else if (status == 2) {
		qm.sendNextPrev("What...? What do you mean? Aran, it's you, isn't it? You're Aran. You're the hero that saved us. Aran, don't you remember?");
	} else if (status == 3) {
		qm.sendNextPrev("#b(You explain the situation as well as you can.)#k");
	} else if (status == 4) {
		qm.sendAcceptDecline("I see... I didn't realize you lost your memory. I can't believe you woke up hundreds of years later. This must be the past for you then.");
	} else if (status == 5) {
		qm.forceCompleteQuest();
		qm.sendNext("Let me reintroduce myself then. My name is Athena Pierce. Athena Pierce is #ba good friend of Aran#k. A few months ago, I fled and you left to battle the Black Mage on your own.");
	} else if (status == 6) {
		qm.sendNextPrev("While you were fighting against the Black Mage, the rest of us were able to board an ark and escape to Victoria Island, although we ended up in this forest instead of the south plains due to a dragon attack.");
	} else if (status == 7) {
		qm.sendNextPrev("But we couldn't just sit and do nothing, so we decided to settle and start new lives here. We've been slowly establishing a town in hopes of starting anew.");
	} else if (status == 8) {
		qm.sendNextPrev("Because we're trying to establish a town here in Victoria Island, where we know no one, all of our young men are out pulling their weight. Here, there are only women, children, and the injured.");
	} else if (status == 9) {
		qm.sendPrev("But, Aran, how did you get here anyway?");
		qm.dispose();
	}
}