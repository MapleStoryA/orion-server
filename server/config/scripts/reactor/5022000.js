/**
 * Reactor in Deep Sea Spaceship Crash Site Map
 * Drops Crystanol Fragment. <questId: 100005>
 */

function act() {
	rm.getPlayer().dropMessage(5, "The Antellion Relic combined with the broken tube and a mysterious item dropped.");
	if (rm.isCQActive(100005)) {
		rm.dropSingleItem(4032708);
	}
}