/* Event 3rdjob.js

    Created by Matthew Prince of MapleRoyals.com - matt@mapleroyals.com
*/

load("nashorn:mozilla_compat.js");
importPackage(Packages.client);
importPackage(Packages.client.inventory);
importPackage(Packages.server.life);
importPackage(Packages.tools);


function init() {
}


function monsterValue(eim, mobId) {
    return 1;
}


function setClassVars(player) {
    var returnMapId;
    var monsterId;
    var mapId;
    if (player.getJob().equals(MapleJob.FP_WIZARD) ||
        player.getJob().equals(MapleJob.IL_WIZARD) ||
        player.getJob().equals(MapleJob.CLERIC)) {
        mapId = 108010200;
        returnMapId = 100040106;
        monsterId = 9001001;
    } else if (player.getJob().equals(MapleJob.FIGHTER) ||
        player.getJob().equals(MapleJob.PAGE) ||
        player.getJob().equals(MapleJob.SPEARMAN)) {
        mapId = 108010300;
        returnMapId = 105070001;
        monsterId = 9001000;
    } else if (player.getJob().equals(MapleJob.ASSASSIN) ||
        player.getJob().equals(MapleJob.BANDIT)) {
        mapId = 108010400;
        returnMapId = 107000402;
        monsterId = 9001003;
    } else if (player.getJob().equals(MapleJob.HUNTER) ||
        player.getJob().equals(MapleJob.CROSSBOWMAN)) {
        mapId = 108010100;
        returnMapId = 105040305;
        monsterId = 9001002;
    } else if (player.getJob().equals(MapleJob.BRAWLER) ||
        player.getJob().equals(MapleJob.GUNSLINGER)) {
        mapId = 108010500;
        returnMapId = 105040305;
        monsterId = 9001008;
    }
    return new Array(mapId, returnMapId, monsterId);
}


function playerEntry(eim, player) {
    var info = setClassVars(player);
    var mapId = info[0];
    var map = eim.getMapFactory().getMap(mapId, false, false);
    map.toggleDrops();
    player.changeMapScripting(map);
}


function playerDead(eim, player) {
    var info = setClassVars(player);
    var mapId = info[0];
    var returnMapId = info[1];
    var monsterId = info[2];
    player.setHp(1);
    var returnMap = em.getChannelServer().getMapFactory().getMap(returnMapId);
    player.changeMapScripting(returnMap);
    eim.unregisterPlayer(player);
    eim.dispose();
}


function playerDisconnected(eim, player) {
    var info = setClassVars(player);
    var mapId = info[0];
    var returnMapId = info[1];
    var monsterId = info[2];
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    var returnMap = em.getChannelServer().getMapFactory().getMap(returnMapId);
    player.setMap(returnMap);
    eim.dispose();
}


function allMonstersDead(eim) {
    var price = new Item(4031059, 0, 1);
    var winner = eim.getPlayers().get(0);
    var info = setClassVars(winner);
    var mapId = info[0];
    var returnMapId = info[1];
    var monsterId = info[2];
    var map = eim.getMapFactory().getMap(mapId + 1, false, false);
    map.spawnItemDrop(winner, winner, price, winner.getPosition(), true, false);
    eim.dispose();
}


function cancelSchedule() {
}


function warpOut(eim) {
    eim.dispose();
}


function leftParty(eim, player) {

}


function disbandParty(eim, player) {


}


function dispose() {


}