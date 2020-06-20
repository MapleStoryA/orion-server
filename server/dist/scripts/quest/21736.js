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
		qm.sendNext("Long time no see! You've leveled up a lot since the last time we met. You must be training really hard. Always hard-working. I'm not surprised. It's exactly what a hero like you would do. I'm sure #p1201000# will be happy to hear about your progress.");
	} else if (status == 1) {
		qm.sendNextPrev("Anyway, enough small talk. I realized that it might be more effective to search for information in places outside Victoria Island as well, so I've begun investigating in Ossyria. I began with #b#m200000000##k and immediately hit the jackpot.");
	} else if (status == 2) {
		qm.sendAcceptDecline("It seems like something strange is happening in #m200000000# in Ossyria. It's a bit different from when we were dealing with the puppeteer, but my instincts tell me it has to do with the Black Wings. Please head over to #m200000000#.");
	} else if (status == 3) {
		qm.forceStartQuest();
		qm.dispose();
	}
}