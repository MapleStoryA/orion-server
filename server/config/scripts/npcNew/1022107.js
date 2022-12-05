let map = target.fieldID();
let sign = target.get("evan_sign");
let mapVar = target.getVar("evan_map_" + map);

if(!sign){
	sign = 0;
}
sign = Number(sign);


if(!target.isQuestActive(22530)){
	self.say("It's a sign.");
	return;
}

if(sign >= 5){
	target.forceCompleteQuest(22530, 1022107);
	target.incSP(1);
	self.say("Congratulations, you've found all the signs.");
	self.sayOk("Now go to your next quest...");
	return;
}

if(mapVar){
	self.say("You completed the check of " + sign + " signs.");
	return;
}

doChat();


function doChat(){
	target.set("evan_sign", sign + 1)
	target.setVar("evan_map_" + map, "1");
	self.say("You are done here, visit the next sign.");
}
