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

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Geenie travel between Orbis and Ariant
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.6 - Fix for infinity looping [Information]
	1.5 - Fix wrong map ID [Information]
	1.4 - Ship/boat is now showed
	    - Removed temp message[Information]
	    - Credit to Snow/superraz777 for old source
	    - Credit to Titan/Kool for the ship/boat packet
	1.3 - Removing some function since is not needed [Information]
	    - Remove register player menthod [Information]
	1.2 - It should be 2 ships not 1 [Information]
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/
load("nashorn:mozilla_compat.js");
importPackage(Packages.tools);

//Time Setting is in millisecond
var closeTime = 240000; //The time to close the gate
var beginTime = 300000; //The time to begin the ride
var rideTime = 600000; //The time that require move to destination
var Orbis_btf;
var Geenie_to_Orbis;
var Orbis_docked;
var Ariant_btf;
var Geenie_to_Ariant;
var Ariant_docked;

function init() {
	Orbis_btf = em.getChannelServer().getMapFactory().getMap(200000152);
	Ariant_btf = em.getChannelServer().getMapFactory().getMap(260000110);
	Geenie_to_Orbis = em.getChannelServer().getMapFactory().getMap(200090410);
	Geenie_to_Ariant = em.getChannelServer().getMapFactory().getMap(200090400);
	Orbis_docked = em.getChannelServer().getMapFactory().getMap(200000100);
	Ariant_docked = em.getChannelServer().getMapFactory().getMap(260000100);
	Orbis_Station = em.getChannelServer().getMapFactory().getMap(200000151);
	scheduleNew();
}

function scheduleNew() {
	Ariant_docked.setDocked(true);
	Orbis_Station.setDocked(true);
	Ariant_docked.broadcastMessage(MaplePacketCreator.boatPacket(true));
	Orbis_Station.broadcastMessage(MaplePacketCreator.boatPacket(true));
	em.setProperty("docked", "true");
	em.setProperty("entry", "true");
	em.schedule("stopEntry", closeTime);
	em.schedule("takeoff", beginTime);
}

function stopEntry() {
	em.setProperty("entry","false");
}

function takeoff() {
	Ariant_docked.setDocked(false);
	Orbis_Station.setDocked(false);
	Ariant_docked.broadcastMessage(MaplePacketCreator.boatPacket(false));
	Orbis_Station.broadcastMessage(MaplePacketCreator.boatPacket(false));
	em.setProperty("docked","false");
	var temp1 = Orbis_btf.getCharacters().iterator();
	while(temp1.hasNext()) {
		temp1.next().changeMap(Geenie_to_Ariant, Geenie_to_Ariant.getPortal(0));
	}
	var temp2 = Ariant_btf.getCharacters().iterator();
	while(temp2.hasNext()) {
		temp2.next().changeMap(Geenie_to_Orbis, Geenie_to_Orbis.getPortal(0));
	}
	em.schedule("arrived", rideTime);
}

function arrived() {
	var temp1 = Geenie_to_Orbis.getCharacters().iterator();
	while(temp1.hasNext()) {
		temp1.next().changeMap(Orbis_docked, Orbis_docked.getPortal(0));
	}
	var temp2 = Geenie_to_Ariant.getCharacters().iterator();
	while(temp2.hasNext()) {
		temp2.next().changeMap(Ariant_docked, Ariant_docked.getPortal(0));
	}
	scheduleNew();
}