/*
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


var exitMap;
var map;
var fall = 0;
var eim;

function init() {
    em.setProperty("rescueGaga", "true"); 
}

function setup(eim) {
}

function playerEntry(eim, player) { // this gets looped for every player in the party.
    eim = em.getInstance("rescueGaga");
    map = em.getChannelServer().getMapFactory().getMap(player.getMapId());
    exitMap = em.getChannelServer().getMapFactory().getMap(922240200);
}

function playerDead(eim, player) {
}

function playerDisconnected(eim, player) {
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
    eim.unregisterPlayer(player); 
}

function playerExit(eim, player) {
    player.changeMap(exitMap);
    eim.unregisterPlayer(player);
}

function fall(eim, player) {
    fall++;
    if (fall > 3)
		playerExit(eim, player);
    else
		player.changeMap(map);
}
function timeOut() {
}