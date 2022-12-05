/*
* @NPC - Kiriru 
* @description from Empress Road to Ellinia
* @id - 1100003
*/
var duration = 80;

function start() {
    cm.sendYesNo("Are you thinking about leaving " + cm.getMap().getStreetName() + "? If you board this ship, I'll take you from #b" + cm.getMap().getStreetName()  +"#k to #bOrbis#k... But you must pay a fee of #b 800#k Mesos. Would you like to head over to Orbis now? It'll take about a minute to get there.");
}
var maps = [
200090021,
200090023,
200090035,
200090027,
200090029,
200090041,
200090043,
200090045,
200090047,
200090049
];
function action(mode) {
    if (mode == 0) {
        cm.sendNext("Hmm, you don't want to go? Suit yourself. If you ever change your mind, please let me know.");
    } else if (mode == 1) {
        if (cm.getPlayer().getMeso() < 800) {
            cm.sendNext("Hmm... Are you sure you have #b800#k Mesos? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you get on.");
        } else {
            for (var i = 0; i < maps.length; i++) {
            	var map = maps[i];
                if (cm.getPlayerCount(map) == 0) {
                	cm.getPlayer().setTravelTime(60);
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