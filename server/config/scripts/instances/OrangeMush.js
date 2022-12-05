let mobKills = 0;
let MINS = 5;
let KILLS = 150;

function onEventStart(){
  event.log('Starting event[' + name + ']');
  event.schedule('onEventFinish', 60 * MINS);
  event.sendClock(60 * MINS);
}

function onPlayerExitMap(player, map){
	event.log('Player ' + player.sCharacterName() + '  exit map');
	event.clear();
}

function onMobKilled(player, monster){
	mobKills++;
	event.log('A total of ' + mobKills + ' has been killed');
}

function onPlayerDisconnected (player){
	event.log('Player ' + player.sCharacterName() + ' disconnected');
}

function onEventFinish(){
	event.log('Completing event [' + name + ']. ' + mobKills  + ' has been killed in total.');
	if(mobKills > 250){
		event.log("Spawning mushroom");
		field.spawnMonster(9500326, 654, 185);
	}
	if(mobKills >= KILLS){
		event.gainPartyExp(500 + (mobKills - 100));
	}else{
		event.gainPartyExp(mobKills * 2);
	}
	
	event.destroyClock();
	event.clear();
	
}