let mobKills = 0;

function onEventStart(){
  event.log('Starting event[' + name + ']');
  event.registerTransferField(910600000, "out00");
  event.schedule('onEventFinish', 5 * 60);
  event.sendClock(5 * 60);
  event.setCurrentMap(910600000);
  spawnMobs();
  event.schedule('spawnNpc', 1);
}

function spawnMobs(){
	if(field.getMonsterCount() <= 0){
	  field.spawnMonster(9300387, -45, 305);
	  field.spawnMonster(9300387, -45, 305);
	  field.spawnMonster(9300387, -45, 305);
	  field.spawnMonster(9300387, 305, 305);
	  field.spawnMonster(9300387, 305, 305);
	  field.spawnMonster(9300387, 305, 305);
	}
}

function spawnNpc(){
	field.spawnNpcWithEffect(1013201, -179, 305);
}

function removeNpc(){
	field.removeNpcWithEffect(1013201);
}

function onMobKilled(player, monster){
	if(field.getMonsterCount() <= 1){
		event.log('saved camilla');
		event.destroyClock();
		removeNpc();
		event.clear();
		leader.incSP(1);
		leader.forceCompleteQuest(22557, 1012003);
	}
}

function onPlayerExitMap(player, map){
	event.log('Player ' + player.sCharacterName() + '  exit map');
	field.killAllMonsters();
	removeNpc();
	event.clear();
}

function onPlayerDisconnected (player){
	event.log('Player ' + player.sCharacterName() + ' disconnected');
	field.killAllMonsters();
	removeNpc();
	event.clear();
}

function onEventFinish(){
	event.log('Completing event [' + name + ']. ' + mobKills  + ' has been killed in total.');
	removeNpc();
	field.killAllMonsters();
	event.destroyClock();
	event.clear();
	
}