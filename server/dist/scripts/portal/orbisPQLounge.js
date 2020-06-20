load("nashorn:mozilla_compat.js");
importPackage(Packages.server.maps);
importPackage(Packages.net.channel);
importPackage(Packages.tools);


function enter(pi) {
	var eim = pi.getPlayer().getEventInstance();
	var party = pi.getPlayer().getEventInstance().getPlayers();
	var realParty = pi.getParty();
	var playerStatus = pi.isLeader();
	if (playerStatus) { //Leader
		if (eim.getProperty("7stageclear") == null) {
			pi.warp(920010600, 17); //Sealed Room
			return true;
		} else {
			pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You cannot go back to your room."));
			return false;
		}
	} else { //Not leader
		if (party.get(0).getMapId() == 920010600) { //Check what map the leader is in
				pi.warp(920010600, 17); //Sealed Room
				return true;
		} else {
			pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "Voce nao pode ir nesta sala se seu lider nao esta nela."));
			return false;
		}
	}
}