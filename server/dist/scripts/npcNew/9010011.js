function intro() {
	self.say("Hello evan, I'm the #bOrange Mushroom#k");
	self.say("If you are level 20 or less, I would like to challenge you for a special event, the #rOrange Mushroom #r");
	self.say("You will have 5 minutes to defeat #b150#k #rCynical Orange Mushrooms#k.");
	self.sayOk("If you complete this challenge, a special monster will appear. Will you be able to defeat it?");
}
const introVar = target.getVar("intro");
if(!introVar){
	intro();
	target.setVar("intro", "1");
	return;
}
if(target.nLevel() > 25){
	self.say("You are too high level for my challenges..");
	return;
}
if(!target.isOnParty()){
	self.say("Please create a party first.");
	return;
}
if(!target.isPartyBoss()){
	self.say("You must be the leader of the party in order to start the challenge.");
	return;
}
let ret = self.askYesNo("Would you like to try the challenge?");
if(ret === 0){
	self.say("Let me know when you are ready...");
	return;
}
let char = target.getPlayer();
let eventCenter = char.getChannelServer().getEventCenter();
let map = target.fieldID();
let event = eventCenter.registerParty('OrangeMush', char);
event.addEventMap(map);
event.onEventStart();







