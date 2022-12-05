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
		qm.sendNext("Oh, hello there. You've leveled up so much that I didn't even recognize you at first. This task should be a breeze for you then. What task, you ask?");
	} else if (status == 1) {
		qm.sendNextPrev("While you were training, #p1201000# and I have been thoroughly looking into your past and the Seal Stone. And guess what? We received an interesting piece of information just recently. Do you know the town that consists of toys for kids known as #m220000000#?");
	} else if (status == 2) {
		qm.sendNextPrev("There are two clocktowers in #m220000000# that control time. These towers allow time in #m220000000# to remain frozen. I hear the clocks stop time because the toys will become useless if the kids grow up.");
	} else if (status == 3) {
		qm.sendNextPrev("But apparently, one of the clocktowers broke. No one knows why or how, but the broken clocktower has #bcreated a time gap in #m220000000#, allowing people to travel to the past#k. Oh, and this is where it gets interesting...");
	} else if (status == 4) {
		qm.sendAcceptDecline("Based on the information we've collected from people that've entered the time portal, we were able to conclude that the time they traveled to in #p1201000# is #bclose to the time when you were around#k! The way people dressed, the items they used, the surroundings, it all points to that time! We might be able to find more information on the Seal Stone then, don't you think?");
	} else if (status == 5) {
		qm.sendNext("I mean, I am not worried about the Seal Stone itself. I just thought there was a possibility of you meeting someone that knew you back in that time period.");
	} else if (status == 6) {
		qm.sendPrev("#bThe right clocktower#k... Specifically, the Helios Tower, is the broken one. Inside #bthe building that resembles a pink bunny head#k, you will find a device that manages time. #bTake the ladder to the top of Helios Tower and continue going up#k. You'll be able to enter the past from there.");
		qm.forceCompleteQuest();
		qm.dispose();
	}
}