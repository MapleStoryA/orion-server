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
		qm.sendNext("Are you busy? I have been looking all over Victoria Island in search of valuable information and found something that might intrigue you. It's about #o9300346#...");
	} else if (status == 1) {
		qm.sendNextPrev("I don't know if you know this, but ever since you taught #o9300346# a lesson, the entrance to the Evil Eye Cave doesn't work. It looks like #o9300346# has moved to a new hideout.");
	} else if (status == 2) {
		qm.sendAcceptDecline("I received a report that someone witnessed  #o9300346# entering a #bsmall cabin#k in #b#m105040200##k of #m105040300#. I heard it from a reliable source, so it's probably true. Rush over and defeat #r#o9300346##k.");
	} else if (status == 3) {
		qm.forceStartQuest();
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
		qm.forceCompleteQuest();
		qm.sendNext("You must have come back after defeating #o9300346#... But what's with the long face? Did something happen?", 9);
	} else if (status == 1) {
		qm.sendNextPrev("#b(You explain there wasn't any information on #t4032323#.)#k", 3);
	} else if (status == 2) {
		qm.sendPrev("Ah, that's what's bothering you. Hahaha, you don't have to worry about that.", 9);
		qm.dispose();
	}
}