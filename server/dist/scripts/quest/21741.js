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
		qm.sendNext("Have you been advancing your levels? I found an interesting piece of information about the Black Wings. This time, you'll have to travel quite a bit. Do you know a town called #b#m250000000##k? You'll have to head there.");
	} else if (status == 1) {
		qm.sendYesNo("Apparently, #b#p2090004##k in #m250000000# somehow met with the Black Wings. I don't know the details. Please go and find out why the Black Wings contacted #p2090004# and what exactly happened between them.");
	} else if (status == 2) {
		qm.forceStartQuest();
		qm.sendOk("Mr. Do is known to be curt, so you are going to have to remain patient while talking to him. Talk to him with the #bI heard you met the Shadow Knight of the Black Wings#k keyword.");
		qm.dispose();
	}
}