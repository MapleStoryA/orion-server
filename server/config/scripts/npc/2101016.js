/*
* @autor Java
* LeaderMS MapleStory Private Server
* APQ
*/

var status = 0;
var copns;
var PQItems = 4031868;
var teste = 1;

importPackage(Packages.client);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			joias = cm.getPlayer().countItem(4031868);
			if (joias <= 5) {
				cm.removeAll(4031868);
				cm.sendNext("                                  #e<LeaderMS APQ>#n\r\n\r\nTraga mais #eJoias#n da proxima vez, se quiser ganhar mais #eexperiencia#n.");
			} else {
				cm.removeAll(PQItems);
				cm.sendNext("                                  #e<LeaderMS APQ>#n\r\n\r\nObrigado pelas #b#eJoias#k#n.");
                                cm.gainExp(100 * cm.getC().getChannelServer().getExpRate() * joias);
                             //   cm.gainPQPoints(joias / 3.5 * teste);
			}
		} else if (status == 1) {
			cm.warp(980010020, 0);
			cm.dispose();
		}
	}
}