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
		qm.sendAcceptDecline("Who would have thought that the heroe's successor would reappear after hundreds of years...? Will you bring prosperity to the Maple World or will you end its existence? I suppose it really doesn't matter. Alright, I'll tell you what I know about the Seal Stone of Mu Lung.");
	} else if (status == 1) {
		qm.forceStartQuest();
		qm.sendNext("The Seal Stone of Mu Lung is located at the Sealed Temple. You will find the entrance deep inside the Mu Lung Temple. You can enter the Sealed Temple if you find the pillar with the word 'Entrance' written on it. The password is: #bActions speak louder than words#k.");
	} else if (status == 2) {
		qm.sendNext("Maybe that Shadow Knight is already at the Sealed Temple. But someone who poses this kind of challenge probably isn't there just for the item. He's there to see me, but I think it would be best for the hero's successor to face the Shadow Knight.");
	} else if (status == 3) {
		qm.sendNextPrev("Please do everything you can to stop the Shadow Knight from bringing doom to our temple. Please continue the legacy of your predecessors.");
	} else if (status == 4) {
		qm.sendPrev("#b(He has mistaken you for a hero's successor. But what does he mean when he mentions continuing the legacy of the heroes? You'll have to stop the Shadow Knight first before asking him.)#k");
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
		qm.sendYesNo("Were you able to defeat the Shadow Knight? But you don't look too happy. I'm guessing it's not because you lost the battle.");
	} else if (status == 1) {
		qm.forceCompleteQuest();
		qm.sendNext("I see. So you lost the Seal Stone of Mu Lung... That's unfortunate, but there is nothing you can do about it. I, too, do not have a clue why the heroes left the Seal Stone in Mu Lung.");
	} else if (status == 2) {
		qm.sendNext("Are you sure the heroes left the Seal Stone in Mu Lung?");
	} else if (status == 3) {
		qm.sendNextPrev("Yes, I suppose you weren't aware of that. #bA long, long time ago, the heroes left the Seal Stone in Mu Lung, and the chief made the Sealed Temple to keep it safe#k.");
	} else if (status == 4) {
		qm.sendNextPrev("The heroes...");
	} else if (status == 5) {
		qm.sendNextPrev("Hardly anyone knows the existence of something like that nowadays. Honestly, #bI'm not even sure that losing the Seal Stone will have any negative effects on Mu Lung#k. We just valued it since it was something the heroes left us for safe keeping.");
	} else if (status == 6) {
		qm.sendNextPrev("It's unfortunate that we lost something the heroes left us, but it's comforting to know that the hero's successor is here. Please complete what the heroes couldn't.");
	} else if (status == 7) {
		qm.sendPrev("#b(Mu Lung has been lost... You better consult with Tru.)#k");
		qm.dispose();
	}
}