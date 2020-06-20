function start() {
	cm.gainFactionPoints(10);
	//cm.sendBrowser("www.google.com");
	cm.sendOk("#eYou have gained 10 points for your faction. Your team has "+cm.getFactionPoints(cm.getPlayer().getFactionId()+" points")
	cm.dispose();
}