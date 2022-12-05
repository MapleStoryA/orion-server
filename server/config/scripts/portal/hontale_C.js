/* Portal for the LightBulb Map...

**hontale_c.js
@author Jvlaple
*/
load("nashorn:mozilla_compat.js");
importPackage(Packages.server.maps);
importPackage(Packages.net.channel);
importPackage(Packages.tools);

function enter(pi) {

if (pi.isLeader() == true) {
	var eim = pi.getPlayer().getEventInstance();
	var party = eim.getPlayers();
	var target;
	var theWay = eim.getProperty("theWay");
	var target;
	if (theWay != null) {
		if (theWay = "light") {
			target = eim.getMapInstance(240050300); //light
		} else {
			target = eim.getMapInstance(240050310); //dark
		}
	} else {
		pi.playerMessage("Bata o Lampada para determinar o seu destino!");
		return false;
	}
	var targetPortal = target.getPortal("sp");
	//Warp the full party into the map...
	var partyy = pi.getPlayer().getEventInstance().getPlayers();
	for (var i = 0; i < partyy.size(); i++) {
			party.get(i).changeMap(target, targetPortal);
	}
	return true;
	} else {
	pi.playerMessage(6, "Voce nao e o lider do grupo. Apenas o lider do partido pode prosseguir atraves deste portal.");
	return false;
	}
}