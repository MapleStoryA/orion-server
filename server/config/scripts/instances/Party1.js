let time = 60 * 30;

function onEventStart(){
  event.log('Starting event[' + name + ']');
  event.schedule('onEventFinish', time);
  event.registerTransferField(103000800, 0);
  event.sendClock(time);
}

function onMobKilled(player, monster){
	event.log('A total of ' + mobKills + ' has been killed');
}

function onPlayerDisconnected (player){
	event.log('Player ' + player.sCharacterName() + ' disconnected');
}

function onEventFinish(){
	event.log('Completing event [' + name + ']. ' + mobKills  + ' has been killed in total.');
	event.destroyClock();
	instance.destroy();
	//event.startNpc(9200000);
}