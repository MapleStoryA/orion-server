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
		qm.sendNext("Well, I'm not really busy or anything, but I don't feel like concocting medicine. Can you come back later? If you don't mind, move.");
	} else if (status == 1) {
		qm.sendNextPrev("I heard you met the Shadow Knight of the Black Wings...");
	} else if (status == 2) {
		qm.sendNextPrev("Ah, you mean that guy dressed in black with a menacing wrinkle in his forehead? Why, yes I did. I did meet him. I even got an item for him. He asked me to deliver it to that old man, Mu Gong.");
	} else if (status == 3) {
		qm.sendNextPrev("An item?");
	} else if (status == 4) {
		qm.sendNextPrev("Yes, a big #bHanging Scroll#k. He gave it to me without saying much. He just asked me to deliver it. He looked scary, as if he would chase me down if I didn't do as he said. Wheeeeeeeew, that was an experience.");
	} else if (status == 5) {
		qm.sendNextPrev("So did you deliever the Hanging Scroll to him?");
	} else if (status == 6) {
		qm.sendAcceptDecline("Well, the thing is... There is a slight problem. Care to listen?");
	} else if (status == 7) {
		qm.forceStartQuest();
		qm.gainItem(4220151, -1);
		qm.sendNext("So what happened was... I was making a new type of medicine, so I filled a pot with water and started boiling some herbs. That's when I made the mistake of...dropping the Hanging Scroll right into the pot. Oh gosh, I pulled it out as fast as I could, but the Hanging Scroll was already soaked and the writing on it had already disappeared.");
	} else if (status == 8) {
		qm.sendAcceptDecline("So then I thought, well what's the point of delivering it to Mu Gong? I must first restore the writing on the Hanging Scroll. That's why I need you to do something for me. I already have some Special Ink to revive it, but I was going to use that ink for something else that is very important. If you'll give me #r150,000 mesos#k to replace this Special Ink, everything will be fine.");
	} else if (status == 9) {
		if (qm.getPlayer().getMeso() >= 150000) {
			qm.getPlayer().gainMeso(-150000, true);
			qm.forceCompleteQuest();
			qm.forceCompleteQuest(21743);
			qm.dispose();
		} else {
			qm.sendOk("Please come back when you have #r150,000 mesos#k.");
			qm.dispose();
		}
	}
}