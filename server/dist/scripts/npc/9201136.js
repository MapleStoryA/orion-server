var status = -1;
var randchimney = randomRange(4,7);
var randpbean = randomRange(1,2);
var randzakum = randomRange(6, 10);
var randforestpatience = randomRange(10, 14);

function start() {
	action(1,0,0);
}

function action(m,t,s) {
	if (m > 0) {
		status++;
	} else {
		cm.dispose();
		return;
	}
	if (status == 0) {
		cm.sendNext("Ah you saved me #r<3#k!");
	} else if (status == 1) {
		cm.sendNextNPC("Thanks for saving her!", 5, 9201137);
	} else if (status == 2) {
		if (cm.getPlayer().getMapId() == 682000200) {
			cm.gainJumpXP(randomRange(16,27));
			cm.gainItem(4000465, randchimney + cm.getPlayer().ComputejumpReward());
		} else if (cm.getPlayer().getMapId() == 980042000) {
			cm.gainJumpXP(randomRange(4,9))
			cm.gainItem(4000465, randpbean + cm.getPlayer().ComputejumpReward());
		} else if (cm.getPlayer().getMapId() == 280020000) {
			cm.gainJumpXP(randomRange(10, 15));
			cm.gainItem(4000465, randzakum + cm.getPlayer().ComputejumpReward());
		} else if (cm.getPlayer().getMapId() == 105040316) {
			cm.gainJumpXP(randomRange(20, 33));
			cm.gainItem(4000465, randforestpatience + cm.getPlayer().ComputejumpReward());
		} 
		cm.dispose();
	}
}

function randomRange (min, max) {
    return Math.random() * (max - min) + min|0;
}