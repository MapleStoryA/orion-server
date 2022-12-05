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
		if (eim.getProperty("4stageclear") == null) {
			pi.warp(920010300, 1); //Storage
			return true;
		} else {
			pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "Voce nao pode voltar para o quarto."));
			return false;
		}
	} else { //Not leader
		if (party.get(0).getMapId() == 920010300) { //Check what map the leader is in
				pi.warp(920010300, 1); //Storage
				return true;
		} else {
			pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "Voce nao pode ir nesta sala se seu lider nao esta nela."));
			return false;
		}
	}
}