/*
* @NPC - Puro 
* @description Enter ship from Rien to Lith Harbor.
* @id - 1200003
*/
var duration = 80;

function start() {
    cm.sendYesNo("Are you thinking about leaving Rien? If you board this ship, I'll take you from #bLith Harbor#k to #bRien#k... But you must pay a fee of #b 800#k Mesos. Would you like to head over to Lith Harbor now? It'll take about a minute to get there.");
}

function action(mode) {
    if (mode == 0) {
        cm.sendNext("Hmm, you don't want to go? Suit yourself. If you ever change your mind, please let me know.");
    } else if (mode == 1) {
        if (cm.getPlayer().getMeso() < 800) {
            cm.sendNext("Hmm... Are you sure you have #b800#k Mesos? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you get on.");
        } else {
            for (var i = 0; i < 10; i++) {
                if (cm.getPlayerCount(200090060+i) == 0) {
                    cm.gainMeso(-800);
                    var duration = 60;
                    cm.getPlayer().setTravelTime(duration);
                    cm.warp(200090060+i);
                    cm.sendClock(duration);
                    cm.dispose();
                    return;
                }
            } cm.sendNext("Seems all ships are taken, try again in a bit.");
        }
    } cm.dispose();
}