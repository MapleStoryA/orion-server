let mins = 1;
let seconds = 10
function onEventStart(){
  event.log('Starting event[' + name + ']');
  event.schedule('onEventFinish', mins * seconds);
  event.sendClock(mins * seconds);
  event.setCurrentMap(925110000);
  field.spawnSpecialMonsters();
}
function onPlayerExitMap(player, map){
	event.log('Player ' + player.sCharacterName() + '  exit map');
	event.clear();
	event.destroyClock();
	field.killAllMonsters();
}

function onPlayerDisconnected (player){
	event.log('Player ' + player.sCharacterName() + '  disconnected');
	event.clear();
	field.killAllMonsters();
}

function onEventFinish(){
	event.log('Completing event [' + name + '].');
	event.destroyClock();
	event.registerTransferField(251010403, "out00");
	event.clear();
	field.killAllMonsters();
}