/* Author: Oxysoft
   Most of the credits for the faction goes to: Soulfist
*/

function start() {
    cm.addPlayerToFaction(0);
	cm.getPlayer().saveToDB(true,true);
	cm.sendSimple("#eHello and welcome to the #gMapleStory faction System#k!\r\n\r\nYour faction is currently #b"+cm.getFactionTeamString(1)+"#k Which faction would you like to choose?!?\r\n\r\n#L1##rRed Faction!#l\r\n#L2##bBlue Faction!");
}

function action(m,t,s) {
    if (m != 1) {
        cm.dispose();
		return;
	}
	cm.addPlayerToFaction(1);
	cm.dispose();
}