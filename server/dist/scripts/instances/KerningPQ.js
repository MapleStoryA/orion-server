let mobKills = 0;

function onEventStart(){
  event.log('Starting event[' + name + ']');
  event.schedule('onEventFinish', 5);
  event.sendClock(5);
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
	event.destroyClock();
	//event.registerTransferField(104040000, 0);
	//instance.destroy();
	event.startNpc(9200000);
}