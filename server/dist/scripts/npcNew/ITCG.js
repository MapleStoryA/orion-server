/*self.say("Hello #b#gender##k, I'm really #npcname# from #bWizet.#k");
self.sayUser("Is it cool to work with #rMaple Story#k?");
self.sayOk("Ohhh~ yea, #b#gender##k.");
*/

inven = inventory;
	

	Pre_val = 2;
	list1 = "";
	list2 = "";
	list3 = "";

	aItem1 = inven.itemCount ( 4031755 ) ;
	aItem2 = inven.itemCount ( 4031756 ) ;
	aItem3 = inven.itemCount ( 4031757 ) ;
	aItem4 = inven.itemCount ( 4031758 ) ;
	aItem5 = inven.itemCount ( 4031759 ) ;

	if ( aItem1 > 0 ) list1 = list1 + "\r\n#L0#Can you tell me more about this #b#t4031755##k?#l";
	if ( aItem2 > 0 ) list1 = list1 + "\r\n#L1#Can you tell me more about this #b#t4031756##k?#l";
	if ( aItem3 > 0 ) list1 = list1 + "\r\n#L2#Can you tell me more about this #b#t4031757##k?#l";
	if ( aItem4 > 0 ) list1 = list1 + "\r\n#L3#Can you tell me more about this #b#t4031758##k?#l";
	if ( aItem5 > 0 ) list1 = list1 + "\r\n#L4#Can you tell me more about this #b#t4031759##k?#l";


	if ( aItem4 > 0 && aItem5 > 0 ) list2 = list2 + "\r\n#b#L0#- #t1002676##k#l";
	if ( aItem2 > 0 && aItem3 > 0 ) list2 = list2 + "\r\n#b#L1#- #t1002675##k#l";
	if ( aItem1 > 0 && aItem3 > 0 && aItem5 > 0 ) list2 = list2 + "\r\n#b#L2#- #t1082223##k#l";
	if ( aItem1 > 0 && aItem2 > 0 && aItem4 > 0 ) list2 = list2 + "\r\n#b#L3#- #t1032048##k#l";

	if ( aItem1 > 0 ) list3 = list3 + "#t4031755# ";
	if ( aItem2 > 0 ) list3 = list3 + "#t4031756# ";
	if ( aItem3 > 0 ) list3 = list3 + "#t4031757# ";
	if ( aItem4 > 0 ) list3 = list3 + "#t4031758# ";
	if ( aItem5 > 0 ) list3 = list3 + "#t4031759# ";


	if ( aItem1 > 0 || aItem2 > 0 || aItem3 > 0 || aItem4 > 0 || aItem5 > 0 ) {
		if ( Pre_val == 2 ) {
			x1 = self.askMenu ( "Artifacts are ancient items of power, usually created by an ancient civilization for some special purpose.  There are several types of them.  I've been exploring the breadth of Maple World, trying to learn what each of them was used for.  While their original use currently still eludes us, with the right know-how, the energies inside an Artifact can be unlocked and transferred into the formation of a mundane object such as a sword or a helmet, imbuing it with great power.\r\n#b#L0#Can you tell me more about the Artifacts that I have?#l\r\n#L1#Can I make anything with the Artifacts in my inventory?#l" );
			if ( x1 == 0 ) {
				x2 = self.askMenu ( "A thirst for knowledge coupled with a thirst for adventure... that's the winning combination!  What can I tell you about, kid?" + list1 );
				if ( x2 == 0 ) self.say ( "The Taru were ancient mystical warriors that once inhabited what is now known as the Krakian Jungle.  They were fierce fighters, but warriors who lived as one with the creatures they shared their jungle with.  However, they seem to have been driven to extinction, I surmise by the fearsome Krakians themselves.  The spirit forms of their greatest warriors were carved into totems to honor their greatness and valor after they fell in battle.  These Artifacts are imbued with the essence of their strength." );
				else if ( x2 == 1 ) {
					self.say ( "Deep in the middle of Phantom Forest outside of New Leaf City lies Crimsonwood Keep.  In the days of ancient Masteria, warriors and mages would journey from all over the world to this fortress, traveling thousands of miles over mountains and oceans to train with the Masters of the Keep, in the hopes of perfecting their skills and becoming Masters themselves." );
					self.say ( "The Mystic Astrolabes were created by the Grandmaster Mage, one of the Masters of the Keep, and her acolytes.  They say that many of the secrets of the universe can only be gleamed through the stars.  The Mystic Astrolabe calls upon the unseen powers of the heavens, helping its user divine the path to what they're seeking... whatever that may be.  Don't ask me how.  Some say the Astrolabes did more than navigate, that they could actually transport you to anywhere you wanted to go.  My guess is that these Artifacts were tied with the object known as the Antellion.  In any case, the Grandmaster Mage used these Astrolabes to guide the training of her charges, and rule Crimsonwood Keep with wisdom." );
				}
				else if ( x2 == 2 ) self.say ( "Ah... the Antellion.  The name is still a mystery to me.  What was it?  From what I've been able to discern, it was a large crystalline statue or monument of some sort.  The only clue I have to go on in regards to this was a tablet I found.  Sad to say, one of my former assistants dropped the tablet and it shattered into a thousand pieces... one of the reasons I decided to carry out my discoveries alone.  In any case, the part I was able to decipher seemed to indicate that simply through touching the monument, the Antellion could transport you to distant locations in the blink of an eye.  Some sort of ancient teleportation device if you will.  This Antellion Relic you have in your possession is but a part of the actual Antellion itself.  Whatever this Antellion object actually was, it was located somewhere on this continent." );
				else if ( x2 == 3 ) {
					self.say ( "That's an extremely rare jewel you've got there... and an extremely ominous Artifact as well.  This Jewel bears the mark of Naricain.  Naricain was a powerful demon, and from what I gather, not from this world.  What I know of him is limited, gleamed from a few ancient scrolls and carvings.  In these, he is often represented as a many-armed beast... not exactly a friendly-looking fellow.  Legend says that he was once a mortal sorcerer, and that his dark pursuits of power transformed him into something more." );
					self.say ( "Unfortunately, it seems that this demon may be more than just an urban myth.  Naricain would often imbue his essence into items to tempt people to his dark power.  The Dark Scrolls are believed to be his creations... They do bear his touch: gambling with power at the risk of self destruction.  The Naricain Jewel is another such item.  I'm not entirely sure why such a gem was created, or what its purpose is, but it can't be good.  Treat this object with caution and keep it safe!" );
				}
				else if ( x2 == 4 ) {
					self.say ( "My, my, an actual Subani Ankh!  This is one of the very things that has brought me to the continent of Masteria!  Years ago, deep in the bowels of one of the Nihalian Pyramids, I discovered a door to a locked chamber, encircled with engravings of these Ankhs.  In the center of that huge stone door was a bas relief image of some sort of a mystical being... a being bearing the name 'Subani.'" );
					self.say ( "I was convinced that a great treasure lay inside that chamber, and for years have been searching for a clue as to how I could open it.  Not too long ago, I received a letter from my brother Jack, who had come to NLC and was exploring the wilds of Masteria.  In his letters, he drew this very symbol of this Ankh, stating that he'd seen it here!  I've come here to find him, and together, solve one of the mysteries of the Maple World." );
					self.say ( "In his last communique to me, Jack also described to me a huge tableau that he found depicting Subani locked in a fierce battle with a dark demon who we believe is Naricain.  I hesitate to call Subani good and Naricain evil because we don't know much yet about the civilization that worshipped these beings.  But rather, where Naricain embodies darkness, Subani embodies light... this much is clear. Accordingly, this Subani Ankh you hold is imbued with the power of light... and the ability to ward off darkness." );
				}
			}
			else if ( x1 == 1 ) {
				if ( list2 == "" ) self.say ( "Hmmm... it doesn't look like you can make anything yet from the Artifacts you currently have.  Well, keep searching, and come see me when you find another new Artifact!" );
				else {
					x3 = self.askMenu ( "Zounds!  You've got something!  With the Artifacts in your possession( " + list3 + ") I think you can use them to synthesize items below.  If you want to know more about the inventions, you can click on the names." + list2 + "\r\n\r\n#b#L4#Skip that...I'm ready to make an item from my Artifacts!#k#l");
					 if ( x3 == 0 )	{
						self.say ( "Whew... This is one of the most powerful Ancient Masterian Relic items I know of!  These were bestowed only upon the wisest Masters of Crimsonwood Keep, those who had seen both the depths of the light and the dark, and achieved true balance in their mind and soul.  Likewise, the Infinity Circlet was forged with both the powers of light and darkness within it... and its insight into both grants its wearer additional wisdom." );
						self.say ( "Since you have both a #bSubani Ankh#k and a #bNaricain Jewel#k in your possession, it looks like you may be able to create one!  This is tremendously exciting! You will need some additional materials as well but these two rare Artifacts are the crucial parts of creating the Circlet." );
						x4 = self.askMenu ( "Do you want me to draw up a Manual to create an #bInfinity Circlet#k using the Artifacts in your possession?  Just a warning though... I will have to tinker with the Artifacts to do this, so once we start this, there's no turning back!\r\n#b#L0#Create #t1002676##k#l\r\n#b#L1#No...I haven't made my mind up yet.  I think I'll hold on to these Artifacts for now and see what else I can make.#k#l" ) ;
							if ( x4 == 0 ) {
								ret = inven.exchange( 0, 4031759, -1, 4031758, -1, 4031822, 1 );
								if ( ret == 0 ) self.say( "Please check and see if you have all the items you need, or if your etc. inventory is full or not." );
								else self.say ( "''Let me see those Artifacts...''\r\nBarricade takes the artifacts from you. As John excitedly examines and fiddles with the Artifacts, he does some quick calculations and scribbles up a rough manual for building the staff.\r\n''Here you go... take this #b#t4031822##k! Take it to Spindle in the Omega Sector. You still need some other materials to create this item and Spindle will explain to you what you need. He'll be able to decipher my schematics and assemble everything for you.''" );
							}
							else if ( x4 == 1 ) self.say ( "You're a cautious fellow... good.  That's always the wiser course of action when dealing with unknown relics of power.  Keep hunting." );
					}
					 else if ( x3 == 1 ) {
						self.say ( "Whatever the Antellion was, it was more than just a holy monument.  It had powers considerable enough that the Masters of Crimsonwood Keep sought to keep it safe.  A guild of elite knights known as the Antellion Guard were entrusted with the monument's protection.  These knights wore these magical gold helms called #bAntellion Miters#k, to distinguish them of their charge.  These Miters were imbued with protective energies, and extended the wearer's life force and mana." );
						x4 = self.askMenu ( "Do you want me to draw up a Manual to create one of these helms using the Artifacts in your possession?  Just a warning though... I will have to tinker with the Artifacts to do this, so once we start this, there's no turning back!\r\n#b#L0#Create #t1002675##k#l\r\n#b#L1#No...I haven't made my mind up yet.  I think I'll hold on to these Artifacts for now and see what else I can make.#k#l" ); 
							if ( x4 == 0 ) {
								ret = inven.exchange( 0, 4031757, -1, 4031756, -1, 4031823, 1 );
								if ( ret == 0 ) self.say( "Please check and see if you have all the items you need, or if your etc. inventory is full or not." );
								else self.say ( "''Let me see those Artifacts...''\r\nBarricade takes the artifacts from you. As John excitedly examines and fiddles with the Artifacts, he does some quick calculations and scribbles up a rough manual for building the staff.\r\n''Here you go... take this #b#t4031823##k! Take it to Spindle in the Omega Sector. You still need some other materials to create this item and Spindle will explain to you what you need. He'll be able to decipher my schematics and assemble everything for you.''" );	
							}
							else if ( x4 == 1 ) self.say ( "You're a cautious fellow... good.  That's always the wiser course of action when dealing with unknown relics of power.  Keep hunting." );
					}
					else if ( x3 == 2 )	{
						self.say ( "The Stormcasters were an order of awesome Lightning Mage-Warriors formed in the ancient days of Crimsonwood Keep.  They created and wore these extremely powerful gloves that enhanced both their weapon and magic attack powers, making them extremely fearsome foes in combat." );
						self.say ( "You have a #bSubani Ankh#k, right?  Yes!  And a #bTaru Totem#k and an #bAntellion Relic#k?!  Perfect!  I think I know how the Stormcasters were made... and it was with these three objects!" );
						x4 = self.askMenu ( "Do you want me to draw up a Manual to create a pair of these gloves using the Artifacts in your possession?  Just a warning though... I will have to tinker with the Artifacts to do this, so once we start this, there's no turning back!\r\n#b#L0#Create #t1082223##k#l\r\n#b#L1#No...I haven't made my mind up yet.  I think I'll hold on to these Artifacts for now and see what else I can make.#k#l" ); 						
							if ( x4 == 0 ) {
								ret = inven.exchange( 0, 4031755, -1, 4031757, -1, 4031759, -1, 4031824, 1 );
								if ( ret == 0 ) self.say( "Please check and see if you have all the items you need, or if your etc. inventory is full or not." );
								else self.say ( "''Let me see those Artifacts...''\r\nBarricade takes the artifacts from you. As John excitedly examines and fiddles with the Artifacts, he does some quick calculations and scribbles up a rough manual for building the staff.\r\n''Here you go... take this #b#t4031824##k! Take it to Spindle in the Omega Sector. You still need some other materials to create this item and Spindle will explain to you what you need. He'll be able to decipher my schematics and assemble everything for you.''" );						
							}
							else if ( x4 == 1 ) self.say ( "You're a cautious fellow... good.  That's always the wiser course of action when dealing with unknown relics of power.  Keep hunting." );
					}
					else if ( x3 == 3 )	{
						self.say ( "These magical earrings were highly coveted by mages, as they enhanced both magical offensive and defensive powers.  I've come across some ancient texts describing their creation... so I think I might know how to recreate a pair.   You'll need a #bNaricain Jewel#k.  And let me think... also the powers infused inside a #bTaru Totem#k and a #bMystic Astrolabe#k.  Finally, you're going to need some synthesis materials to create the earrings." );
						x4 = self.askMenu ( "Do you want me to draw up a manual to create a pair of these earrings with the Artifacts in your possession?  Just a warning though... I will have to tinker with the Artifacts to do this, so once we start this, there's no turning back!\r\n#b#L0#Create #t1032048##k#l\r\n#b#L1#No...I haven't made my mind up yet.  I think I'll hold on to these Artifacts for now and see what else I can make.#k#l" ) ;
							if ( x4 == 0 ) {
								ret = inven.exchange( 0, 4031755, -1, 4031756, -1, 4031758, -1, 4031825, 1 );					
								if ( ret == 0 ) self.say( "Please check and see if you have all the items you need, or if your etc. inventory is full or not." );
								else self.say ( "''Let me see those Artifacts...''\r\nBarricade takes the artifacts from you. As John excitedly examines and fiddles with the Artifacts, he does some quick calculations and scribbles up a rough manual for building the staff.\r\n''Here you go... take this #b#t4031825##k! Take it to Spindle in the Omega Sector. You still need some other materials to create this item and Spindle will explain to you what you need. He'll be able to decipher my schematics and assemble everything for you." );
							}
							else if ( x4 == 1 ) self.say ( "You're a cautious fellow... good.  That's always the wiser course of action when dealing with unknown relics of power.  Keep hunting." );
					}				
					else if ( x3 == 4 ) {
						x4 = self.askMenu ( "Ok...  Just a warning.  Once I start tinkering with these Artifacts, there's no turning back.  What do you want to make?" + list2 );
						if ( x4 == 0 ) {
							ret = inven.exchange( 0, 4031759, -1, 4031758, -1, 4031822, 1 );
							if ( ret == 0 ) self.say( "Please check and see if you have all the items you need, or if your etc. inventory is full or not." );
							else self.say ( "''Let me see those Artifacts...''\r\nBarricade takes the artifacts from you. As John excitedly examines and fiddles with the Artifacts, he does some quick calculations and scribbles up a rough manual for building the staff.\r\n''Here you go... take this #b#t4031822##k! Take it to Spindle in the Omega Sector. You still need some other materials to create this item and Spindle will explain to you what you need. He'll be able to decipher my schematics and assemble everything for you.''" );
						}
						else if ( x4 == 1 ) {
							ret = inven.exchange( 0, 4031757, -1, 4031756, -1, 4031823, 1 );
							if ( ret == 0 ) self.say( "''Let me see those Artifacts...''\r\nBarricade takes the artifacts from you. As John excitedly examines and fiddles with the Artifacts, he does some quick calculations and scribbles up a rough manual for building the staff.\r\n''Here you go... take this #b#t4031823##k! Take it to Spindle in the Omega Sector. You still need some other materials to create this item and Spindle will explain to you what you need. He'll be able to decipher my schematics and assemble everything for you.''" );
						}
						else if ( x4 == 2 ) {
							ret = inven.exchange( 0, 4031755, -1, 4031757, -1, 4031759, -1, 4031824, 1 );
							if ( ret == 0 ) self.say( "Please check and see if you have all the items you need, or if your etc. inventory is full or not." );
							else self.say ( "''Let me see those Artifacts...''\r\nBarricade takes the artifacts from you. As John excitedly examines and fiddles with the Artifacts, he does some quick calculations and scribbles up a rough manual for building the staff.\r\n''Here you go... take this #b#t4031824##k! Take it to Spindle in the Omega Sector. You still need some other materials to create this item and Spindle will explain to you what you need. He'll be able to decipher my schematics and assemble everything for you.''" );
						}
						else if ( x4 == 3 ) {
							ret = inven.exchange( 0, 4031755, -1, 4031756, -1, 4031758, -1, 4031825, 1 );
							if ( ret == 0 ) self.say( "Please check and see if you have all the items you need, or if your etc. inventory is full or not." );
							else self.say ( "''Let me see those Artifacts...''\r\nBarricade takes the artifacts from you. As John excitedly examines and fiddles with the Artifacts, he does some quick calculations and scribbles up a rough manual for building the staff.\r\n''Here you go... take this #b#t4031825##k! Take it to Spindle in the Omega Sector. You still need some other materials to create this item and Spindle will explain to you what you need. He'll be able to decipher my schematics and assemble everything for you.''" );
						}
					}
				}
			}
		}
		else {
			v1 = self.askMenu ( "What have you got there?!  I see you've been exploring MapleStory offline!  Good work!  You've found a very valuable object!  The item you've found is from a bygone era... we treasure hunters call them Artifacts.\r\n#b#L0#Tell me more about Artifacts!#l\r\n#L1#This old thing?  I'm sure it's worth as much as my sock!#l#k");
			if ( v1 == 0 ) {
				self.say ( "Artifacts are ancient items of power, usually created by an ancient civilization for some special purpose.  There are several types of them.  I've been exploring the breadth of Maple World, trying to learn what each of them was used for.  While their original use currently still eludes us, with the right know-how, the energies inside an Artifact can be unlocked and transferred into the formation of a mundane object such as a sword or a helmet, imbuing it with great power." ) ;
				nRet1 = self.askYesNo ( "Say, are you interested on making one for yourself?" ) ;
					if ( nRet1 == 0 ) self.say( "You don't trust me, I suppose... haha..." );
					else {
						//qr.set( 4927, "1" );
						//qr.setComplete( 4927 );	
					}
				
			}
			else if ( v1 == 1 ) self.say( "What?!  Look, kid... you don't realize what you've got on your hands!  But hey, suit yourself.  It's your loss." );
		}
	}
	else self.say ( "What have you got there?! Hmm? Oh sorry, my mistake. I thought you had something...");	