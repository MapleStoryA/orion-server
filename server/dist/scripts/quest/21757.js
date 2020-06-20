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
		qm.sendYesNo("How can I help you? If you're not here to become a knight, you're not welcome here. And what is that you have there? Is that for the Empress? I can't let you deliver that. It might be something dangerous even IIji isn't aware of. I'll have to take a look at it.");
	} else if (status == 1) {
		qm.forceStartQuest();
		qm.sendNext("Hmmm, this contains some very interesting tidbits. It even has some stuff about the Teardrop of Shinsoo... Well, anyway, I'll look it over more carefully.");
	} else if (status == 2) {
		qm.sendNext("The Black Wings could be targeting this place next.");
	} else if (status == 3) {
		qm.sendPrev("Even if that's the case, it is a matter that will be handled in Ereve. It's really none of your business. There is no guarentee that you are not one of the Black Wings. Thanks for the information, but I'm going to have to ask you to leave.");
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
		qm.sendYesNo("How can I help you? If you're not here to become a knight, you're not welcome here. And what is that you have there? Is that for the Empress? I can't let you deliver that. It might be something dangerous even IIji isn't aware of. I'll have to take a look at it.");
	} else if (status == 1) {
		qm.gainItem(4032330, -1)
		qm.forceCompleteQuest();
		qm.sendNext("Hmmm, this contains some very interesting tidbits. It even has some stuff about the Teardrop of Shinsoo... Well, anyway, I'll look it over more carefully.");
	} else if (status == 2) {
		qm.sendNext("The Black Wings could be targeting this place next.");
	} else if (status == 3) {
		qm.sendPrev("Even if that's the case, it is a matter that will be handled in Ereve. It's really none of your business. There is no guarentee that you are not one of the Black Wings. Thanks for the information, but I'm going to have to ask you to leave.");
		qm.dispose();
	}
}
