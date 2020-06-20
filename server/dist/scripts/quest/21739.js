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
		qm.sendYesNo("Did you wind up preventing the intruder? But you don't look too happy for someone that did just that. What? You lost the Seal Stone?");
	} else if (status == 1) {
		qm.forceCompleteQuest();
		qm.sendNext("Really... so you wound up losing the Seal Stone? In that case, there's nothing we can do about it. I don't know exactly what the Seal Stone is for, either. Just because it is missing, doesn't mean #bOrbis is in imminent danger#k. That, I'm sure of.");
	} else if (status == 2) {
		qm.sendNext("But it does feel like a prelude to something catastrophic, perhaps. It's just born out of instinct, no fortune telling required. Here's wishing you good luck, because you'll need it.");
	} else if (status == 3) {
		qm.sendNextPrev("#b(I lost the Seal Stone of Orbis. What should I do? I better go talk to Tru.)#k");
		qm.dispose();
	}
}