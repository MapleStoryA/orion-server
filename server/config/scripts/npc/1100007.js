/* @NPC - Kiriru 
* @description from Empress Road to Ellinia
* @id - 1100003
*/


function start() {
    cm.sendYesNo("Are you thinking about leaving " + cm.getMap().getStreetName() + "? If you board this ship, I'll take you from #b" + cm.getMap().getStreetName()  +"#k to #bEreve#k... But you must pay a fee of #b 800#k Mesos. Would you like to head over to Ereve now? It'll take about a minute to get there.");
}

function action(mode) {
    if (mode == 0) {
        cm.sendNext("Hmm, you don't want to go? Suit yourself. If you ever change your mind, please let me know.");
    } else if (mode == 1) {
        if (cm.getPlayer().getMeso() < 800) {
            cm.sendNext("Hmm... Are you sure you have #b800#k Mesos? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you get on.");
        } else {
            for (var i = 0; i < 10; i++) {
            	var map = 200090020 + (i * 2);
            	cm.getPlayer().setTravelTime(60);
                if (cm.getPlayerCount(map) == 0) {
                	cm.gainMeso(-800);
                    cm.warp(map, 0);
                    cm.sendClock(60);
                    cm.dispose();
                    return;
                }
            } cm.sendNext("Seems all ships are taken, try again in a bit.");
        }
    } cm.dispose();
}