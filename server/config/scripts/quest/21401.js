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
		qm.sendNext("Why do I look like this, you ask? I don't want to talk about it, but I suppose I can't hide from you since you're my master...");
	} else if (status == 1) {
		qm.sendNextPrev("While you were trapped inside ice for hundreds of years, I, too, was frozen. It was a long time to be away from you. That's when the seed of darkness was planted in my heart.");
	} else if (status == 2) {
		qm.sendNextPrev("But since you awoke, I thought the darkness had gone away. I thought things would return to the way they were, but I was mistaken.");
	} else if (status == 3) {
		qm.sendAcceptDecline("Please, Aran. Please stop me from becoming enraged. Only you can control me. It's out of my hands now. Please do whatever it takes to #rstop me from going berserk#k!");
	} else if (status == 4) {
		qm.forceStartQuest();
		qm.warp(914020000);
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
		qm.sendNext("Thank you, Aran. If it weren't for you, I would have become enraged and who knows what could have happened. Thank you. NOT! It's only your duty as my master...");
	} else if (status == 1) {
		qm.sendYesNo("Anyway, I just noticed how high of a level you've reached. If you were able to control me in my state of rage, I think you're ready to handle more abilities.");
	} else if (status == 2) {
		qm.forceCompleteQuest();
		qm.changeJobById(2112);
		qm.teachSkill(21121000, 0, 30);
		qm.teachSkill(21120001, 0, 30);
		qm.teachSkill(21120002, 0, 30);
		qm.teachSkill(21120004, 0, 30);
		qm.teachSkill(21120005, 0, 30);
		qm.teachSkill(21120006, 0, 30);
		qm.teachSkill(21120007, 0, 30);
		qm.teachSkill(21121003, 0, 30);
		qm.teachSkill(21121008, 0, 5);
		qm.sendNext("Your skills have been restored. Those skills have been dormant for so long that you'll have to retrain yourself, but you'll be as good as new once you complete your training.");
	} else if (status == 3) {
		qm.sendNext("Oh, and I've given you a skill called Maple Hero. It isn't one of the skills you had in the past, but it could come in handy sometime.");
	} else if (status == 4) {
		qm.sendNextPrev("Even with all that, however, you still have a long way to go until you return to the old you. Continue training!");
		qm.dispose();
	}
}