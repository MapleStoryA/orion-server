let mobKills = 0;

function onEventStart() {
    event.log('Starting event[' + name + ']');
    event.schedule('onEventFinish', 60 * 1);
    event.sendClock(60 * 1);
}

function spawnMobs() {

}

function spawnNpc() {

}

function removeNpc() {

}

function onMobKilled(player, monster) {

}

function onPlayerExitMap(player, map) {

}

function onPlayerDisconnected(player) {

}

function onEventFinish() {
    event.destroyClock();
}