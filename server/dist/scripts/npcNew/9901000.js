self.say("Hello, I am #b#npcname##k, and I am LEVEL 200.");
if(target.isEvan()){
	let mount = 1902040;
	if(target.nLevel() >= 80 && target.nLevel() < 120 && target.haveItem(1902040)){
		mount = 1902041;
	}else if(target.nLevel() >= 120 && target.haveItem(1902041)){
		mount = 1902042;
	}
	if(target.haveItem(mount)){
		self.say("I can see that you already have the dragon mount, come back when you are higher level...");
	}else{
		let ask = self.askAccept("Would you like to ride your dragon? If so I will provide you what you need, just #bdouble#b click it");
		if(ask){
			self.sayOk("Enjoy your new mount!");
			inventory.exchange(0, mount, 1);
		}else{
			self.sayOk("Ok.. let me know when you are ready");
		}
	}	
}

