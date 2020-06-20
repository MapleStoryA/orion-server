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
		qm.sendAcceptDecline("Come to think of it, I remember seeing that kid scribbling something on the statue. I was about to yell at the kid, but Chrishrama beat me to it. I tried to see what the kid was writing, but I couldn't see it. Do you think it was the #bpassword#k...?");
	} else if (status == 1) {
		qm.forceStartQuest();
		qm.sendNext("That just about does it for eliminating Zombie Mushrooms. I'm sure there are other violent monsters out there, but I'm inspired by your courage. Please give my thanks to Tru as well.");
	} else if (status == 2) {
		qm.sendPrev("#b(You think there's the password in Mysterious Statue that leads to the Puppeteer's Cave. Should you attack the puppeteer as soon as you find out the password? No, you'll find out what it is and inform Tru first.)#k");
		qm.dispose();
	}
}