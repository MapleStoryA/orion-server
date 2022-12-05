/*self.say("Hello #b#gender##k, I'm really #npcname# from #bWizet.#k");
self.sayUser("Is it cool to work with #rMaple Story#k?");
self.sayOk("Ohhh~ yea, #b#gender##k.");
*/
  	val = target.get( "8824" );
	
	
		if (( val == "" || val == null)) {
			nRet = self.askYesNo( "Hello there, Mapler! We'll be conducting a small Thanksgiving event here. Are you interested?" );
				if(nRet==0) self.say( "Oh I see. Hurry, though, because this event ends soon.");
		  else{
			  self.say( "Cool! Okay, here's the deal. A number of monsters will randomly drop alphabet letters, and your job is to collect all the letters of the word #bMAPLESTORY#k. Once you collect them all, bring them to me and I'll put you in our Thanksgiving event. From this pool, we will randomly select winners to receive MaplePoints. Good luck, and hurry, because this event ends soon!" );
			  target.set( "8824", "s" );
			  }
			  }
			  
			  
		else if (( val == "s" )) {
		  if (inventory.itemCount(3994012) >= 1 || inventory.itemCount(3994000) >= 1 || inventory.itemCount(3994006) >= 1 || inventory.itemCount(3994003) >= 1 || inventory.itemCount(3994001) >= 1 || inventory.itemCount(3994013) >= 1 || inventory.itemCount(3994008) >= 1 || inventory.itemCount(3994005) >= 1 || inventory.itemCount(3994007) >= 1 || inventory.itemCount(3994010) >= 1){
			nRet1 = self.askYesNo( "Thanks. Great job. It must have been difficult for you to collect all the letters, but you managed to pull it off!! Alright, so do you want to turn in the letters and participate in the event?" );
				if(nRet1!=0){
					ret = inventory.exchange(0, 3994012, -1, 3994000, -1, 3994006, -1, 3994003, -1, 3994001, -1, 3994013, -1, 3994008, -1, 3994005, -1, 3994007, -1, 3994010, -1);
					if(ret==1){
			    target.set( "8824", "end" );
			    self.say( "Good choice. Now all you need to do is wait until we select the winners. It'll be posted on #bNexon.net#k soon, so please check the website regularly. I'll see you around. Ciao~" );
			}
			else self.say( "I don't think there's any open slot for this Pumpkin Basket in your inventory...");
			}
			else self.say( "Really? I don't see what those letters can be put to good use for other than on this event. It's your call, though. Come back and talk to me if you have a change of heart." );
		}
		else self.say( "I don't think you have all the letters. The letters you need are #bM A P L E S T O R Y#k. Check and see if you have them all.");
		}
		
		else if(val=="end") {
			self.say( "You have already participated in the event. Now all you need to do is to wait until we select the winners. It'll be posted on 'Nexon.net' soon, so please check the website regularly. I'll see you around. Ciao~" );		
		}