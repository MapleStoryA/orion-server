/*self.say("Hello #b#gender##k, I'm really #npcname# from #bWizet.#k");
self.sayUser("Is it cool to work with #rMaple Story#k?");
self.sayOk("Ohhh~ yea, #b#gender##k.");
*/

inven = inventory;

list = "";

aItem1 = inventory.itemCount ( 4031761 );
aItem2 = inventory.itemCount ( 4031822 ); 
aItem3 = inventory.itemCount ( 4031823 ); 
aItem4 = inventory.itemCount ( 4031824 ); 
aItem5 = inventory.itemCount ( 4031825 ); 
aItem6 = inventory.itemCount ( 4031826 ); 
aItem7 = inventory.itemCount ( 4031827 ); 
aItem8 = inventory.itemCount ( 4031828 ); 
aItem9 = inventory.itemCount ( 4031829 ); 

	if ( aItem1 > 0 ) list = list + "\r\n#b#L0#I have something called a #b#t4031761##l#k";
	if ( aItem6 > 0 || aItem7 > 0 || aItem8 > 0 || aItem9 > 0 ) list = list + "\r\n#b#L1#Foxwit gave me a Manual and said you could build this for me.#l#k";
	if ( aItem2 > 0 || aItem3 > 0 || aItem4 > 0 || aItem5 > 0 ) list = list + "\r\n#b#L2#John Barricade gave me a Manual and said you could build this for me.#l#k";
	
	if ( list == "" ) {
		e1 = self.askMenu ( "Hey, there. The name's Spindle. I'm good at fixing things when they break, and building things so they won't. Need something? If not, I'm kinda busy here...\r\n#b#L0#Nothing, really. I was just curious who you are.#l" );
		if ( e1 == 0 ) self.say ( "Doesn't look like you've got anything from John Barricade or Professor Foxwit. When you get a chance, check out the MapleStory Trading Card Game... it has more in there than you think! See ya!" );
		else self.say ( "Technical Error" );
	}

	else { 
		v1 = self.askMenu ( "Hey, there. The name's Spindle. I'm good at fixing things when they break, and building things so they won't. Need something? If not, I'm kinda busy here..." + list ); 
		if ( v1 == 0 ) {
			v2 = self.askMenu ( "A #b#t4031761##k, huh? Well, only one person could've made you this...so I guess you've been speaking to the Glimmer Man. <frowns> And I can guess what you want. I knew that his help would come with strings attached! Well, tell that wacko magician I said ''Fiddlesticks!'' Anyways, I suppose you'll be wanting me to turn this orb here into an extra-dimensional weapon of destruction.\r\n#b#L0#Yes, please#l\r\n#L1#An extra-dimensional weapon of destruction? Hmm, maybe I'll give peace a chance.#l#k");
			if ( v2 == 0 ) { 
				v3 = self.askMenu ( "Well...hmph. I'll do it because I owe him one, but you tell that crazy old coot he can't be handing out Materia Orbs willy-nilly, to just any 'ol potential psycho on the street! No offense. I mean, I know he's an arms dealer and all, but if this stuff got in the wrong hands...Well, I guess it's none of my business now. In any case, I can form your Materia Orb into any shape. Glimmer has given me several designs for possible weapons, all of them quite amazing and readily capable of inflicting bodily harm. What form do you want your weapon to take?\r\n#b#L0#- One-handed Sword#l\r\n#L1#- LUK Dagger#l\r\n#L2#- STR Dagger#l\r\n#L3#- Staff#l\r\n#L4#- Two-handed Axe#l\r\n#L5#- Spear#l\r\n#L6#- Polearm#l\r\n#L7#- Bow#l\r\n#L8#- Crossbow#l\r\n#L9#- Claw#l\r\n#L10#- One-handed Blunt Weapon#l" );
				if ( v3 == 0 ) chat_message1( 1, "#t1302079#: #v1302079:#" );
				else if ( v3 == 1 ) chat_message1( 2, "#t1332064#: #v1332064:#" ); 
				else if ( v3 == 2 ) chat_message1( 3, "#t1332065#: #v1332065:#" ); 
				else if ( v3 == 3 ) chat_message1( 4, "#t1382053#: #v1382053:#" ); 
				else if ( v3 == 4 ) chat_message1( 5, "#t1412032#: #v1412032:#" ); 
				else if ( v3 == 5 ) chat_message1( 6, "#t1432045#: #v1432045:#" ); 
				else if ( v3 == 6 ) chat_message1( 7, "#t1442060#: #v1442060:#" ); 
				else if ( v3 == 7 ) chat_message1( 8, "#t1452052#: #v1452052:#" ); 
				else if ( v3 == 8 ) chat_message1( 9, "#t1462046#: #v1462046:#" ); 
				else if ( v3 == 9 ) chat_message1( 10, "#t1472062#: #v1472062:#" ); 
				else if ( v3 == 10 ) chat_message1( 11, "#t1322059#: #v1322059:#" ); 
			}
			else self.say( "Well, I never thought I'd hear someone actually say that. More people should be like you!" ); 
		}
		
		if ( v1 == 1 ) {
			list1 = "";
			if ( aItem6 > 0 ) list1 = list1 + "\r\n#b#L0#- #t1102146##k#l";
			if ( aItem7 > 0 ) list1 = list1 + "\r\n#b#L1#- #t1102145##k#l";
			if ( aItem8 > 0 ) list1 = list1 + "\r\n#b#L2#- #t1032049##k#l";
			if ( aItem9 > 0 ) list1 = list1 + "\r\n#b#L3#- #t1092052##K#l";
			if ( list1 == "" ) self.say ( "Listing Error" );
			else {
				v4 = self.askMenu ( "He did, did he?  Well, I'll get right on it!  What are we making today?" + list1 );
				if ( v4 == 0 ) chat_message2( 1, "#v1102146# : #t1102146#" , "#v4004000:# 5 #t4004000#s\r\n#v4010006:# 5 #t4010006#s\r\n#v4021004:# 5 #t4021004#s" );
				else if ( v4 == 1 ) chat_message2( 2, "#v1102145# : #t1102145#" , "#v4011006:# 5 #t4011006#s\r\n#v4021005:# 5 #t4021005#s\r\n#v4021007:# 2 #t4021007#s" );
				else if ( v4 == 2 ) chat_message2( 3, "#v1032049# : #t1032049#" , "#v4020008:# 10 #t4020008#s\r\n#v4004000:# 10 #t4004000#s" );
				else if ( v4 == 3 ) chat_message2( 4, "#v1092052# : #t1092052#" , "#v4011002:# 10 #t4011002#s\r\n#v4021005:# 2 #t4021005#s\r\n#v4004004:# 1 #t4004004#" );
			}
		}		

		if ( v1 == 2 ) {
			list2 = "";
			if ( aItem2 > 0 ) list2 = list2 + "\r\n#b#L0#- #t1002676##k#l";
			if ( aItem3 > 0 ) list2 = list2 + "\r\n#b#L1#- #t1002675##k#l";
			if ( aItem4 > 0 ) list2 = list2 + "\r\n#b#L2#- #t1082223##k#l";
			if ( aItem5 > 0 ) list2 = list2 + "\r\n#b#L3#- #t1032048##k#l";
			if ( list2 == "" ) self.say ( "Listing Error" );
			else {
				v5 = self.askMenu ( "Barricade sent you?  Hmmm... I hate working with Ancient tech, all that mystical mojo stuff.  Gives me the willies when it all works but I don't understand why.  But John is a good guy and I owe him a few favors so I'll do it.  What did he tell you I could make for you?" + list2 );
				if ( v5 == 0 ) chat_message2( 5, "#v1002676# : #t1002676#" , "#v4011004:# 5 #t4011004#s\r\n#v4005000:# 1 #t4005000#\r\n#v4005001:# 1 #t4005001#" );
				else if ( v5 == 1 ) chat_message2( 6, "#v1002675# : #t1002675#" , "#v4011006:# 5 #t4011006#s\r\n#v4011001:# 5 #t4011001#s\r\n#v4003000:# 20 #t4003000#s" );
				else if ( v5 == 2 ) chat_message2( 7, "#v1082223# : #t1082223#" , "#v4005000:# 2 #t4005000#s\r\n#v4000021:# 15 #t4000021#s" );
				else if ( v5 == 3 ) chat_message2( 8, "#v1032048# : #t1032048#" , "#v4021007:# 2 #t4021007#s" );
			}
		}
			
	}
	
	function chat_message1( index, makeItem ) {
		nRet = self.askYesNo( "Are you sure you want a " + makeItem + "?, This is highly dangerous in the wrong hands. Are you sure you want to make one?" ); 
			if ( nRet == 0 ) self.say( "Big decision, I know. Take your time! Come back when you're ready. <grumble> Or not at all..." ); 
			else { 
				if ( index == 1 ) ret = inventory.exchange( 0, 4031761, -1, 1302079, 1 ); 
				else if ( index == 2 ) ret = inventory.exchange( 0, 4031761, -1, 1332064, 1 );
				else if ( index == 3 ) ret = inventory.exchange( 0, 4031761, -1, 1332065, 1 );
				else if ( index == 4 ) ret = inventory.exchange( 0, 4031761, -1, 1382053, 1 );
				else if ( index == 5 ) ret = inventory.exchange( 0, 4031761, -1, 1412032, 1 );
				else if ( index == 6 ) ret = inventory.exchange( 0, 4031761, -1, 1432045, 1 );
				else if ( index == 7 ) ret = inventory.exchange( 0, 4031761, -1, 1442060, 1 );
				else if ( index == 8 ) ret = inventory.exchange( 0, 4031761, -1, 1452052, 1 );
				else if ( index == 9 ) ret = inventory.exchange( 0, 4031761, -1, 1462046, 1 );
				else if ( index == 10 ) ret = inventory.exchange( 0, 4031761, -1, 1472062, 1 );
				else if ( index == 11 ) ret = inventory.exchange( 0, 4031761, -1, 1322059, 1 );
			if ( ret == 0 ) self.say( "Please check and see if you have all the items you need, or if your ETC. inventory is full or not." );
			else self.say( "Here, take this " + makeItem + ". Well, there you go!  Don't go hurting everything you see!" );
		}
	}			

	function chat_message2 ( index, makeItem, needItem ) {
		inventory = inventory; 
		nRet = self.askYesNo( "Are you sure you want a " + makeItem + "?, Let me see the manual. <skims through the Manual> It looks like you will need these additional items below to make" + makeItem + ". Are you sure you want to make one?\r\n\r\n#b" + needItem ); 
			if ( nRet == 0 ) self.say( "Big decision, I know. Take your time! Come back when you're ready." ); 
				else { 
					if ( index == 1 ) ret = inventory.exchange( 0, 4004000, -5, 4010006, -5, 4021004, -5, 4031826, -1, 1102146, 1 );
					else if ( index == 2 ) ret = inventory.exchange( 0, 4011006, -5, 4021005, -5, 4021007, -2, 4031827, -1, 1102145, 1 );
					else if ( index == 3 ) ret = inventory.exchange( 0, 4020008, -10, 4004000, -10, 4031828, -1, 1032049, 1 );
					else if ( index == 4 ) ret = inventory.exchange( 0, 4011002, -10, 4021005, -2, 4004004, -1, 4031829, -1, 1092052, 1 );
					else if ( index == 5 ) ret = inventory.exchange( 0, 4011004, -5, 4005000, -1, 4005001, -1, 4031822, -1, 1002676, 1 );
					else if ( index == 6 ) ret = inventory.exchange( 0, 4011006, -5, 4011001, -5, 4003000, -20, 4031823, -1, 1002675, 1 );
					else if ( index == 7 ) ret = inventory.exchange( 0, 4005000, -2, 4000021, -15, 4031824, -1, 1082223, 1 );
					else if ( index == 8 ) ret = inventory.exchange( 0, 4021007, -2, 4031825, -1, 1032048, 1 );
					if ( ret == 0 ) self.say( "Please check and see if you have all the items you need, or if your ETC. inventory is full or not." );
			else self.say( "Hmmmm...ok, hand me that Manual and those materials will you? <peruses Manual> That's easy enough to understand. Okay, let me grab my tools... <bang, bang, whrrr, whrrr, clank, clank>  Here, take this " + makeItem + ". Good luck with it!" );
		}
	}
	