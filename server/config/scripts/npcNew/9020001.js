var stage2rects = Array(new java.awt.Rectangle(-770,-132,28,178),new java.awt.Rectangle(-733,-337,26,105),new java.awt.Rectangle(-601,-328,29,105),new java.awt.Rectangle(-495,-125,24,165));
var stage3rects = Array(new java.awt.Rectangle(608,-180,140,50),new java.awt.Rectangle(791,-117,140,45),new java.awt.Rectangle(958,-180,140,50),new java.awt.Rectangle(876,-238,140,45),new java.awt.Rectangle(702,-238,140,45));
var stage4rects = Array(new java.awt.Rectangle(910,-236,35,5),new java.awt.Rectangle(877,-184,35,5),new java.awt.Rectangle(946,-184,35,5),new java.awt.Rectangle(845,-132,35,5),new java.awt.Rectangle(910,-132,35,5),new java.awt.Rectangle(981,-132,35,5));


function start() {
	field = target.fieldID();
	quest = target.FieldSet("Party1");

	if (quest.getVar("stage") == "clear") {
		party1_reward();
		return;
	}

	if (!target.isPartyBoss()) {
		if (field == 103000800)
			party1_personal();
		else
			party1_help();
	} else {
		if (field == 103000800){
			party1_stage1();
			quest.enablePortal( "next00", "kpq0" );
		}else if (field == 103000801){
			party1_stage2();
			quest.enablePortal( "next00", "kpq1" );
		}else if (field == 103000802){
			party1_stage3();
			quest.enablePortal( "next00", "kpq2" );
		}else if (field == 103000803){
			party1_stage4();
			quest.enablePortal( "next00", "kpq3" );
		}else if (field == 103000804){
			party1_stage5();
			quest.enablePortal( "next00", "kpq4" );
		}
	}
}

function check_stage( st, checkall ) { 
	quest = target.FieldSet( "Party1" ); 
 	stage = quest.getVar( "stage" ); 
 	if ( stage != st ) { 
 		self.say( "You all completed the mission of this stage. Use the portal to move on to the next stage ..." ); 
 		return 0; 
 	} 

 	field = quest; 
 	if ( checkall == 1 || quest.getUserCount() != field.getUserCount() ) { 
 		self.say( "It seems that not all members of the group are together. Everyone in your group needs to come from the previous stage to participate in the mission. Please have all the members ready here ..." ); 
 		return 0; 
 	} 
 	return 1; 
 } 
function area_check(rects){
	let ret = "";
	for(let i = 0; i < rects.length; i++){
		let map = target.field();
		if(map.hasPlayersInRectanble(rects[i])){
			ret += "1";
		}else{
			ret += "0";
		}
	}
	return ret;
} 
function party1_personal() { 
 	charName = target.sCharacterName() + "_"; 
 	
 	quest = target.FieldSet( "Party1" ); 
 	prob = quest.getVar( charName ); 
 	if ( prob == "clear" ) { 
 		self.say( "Wow, you responded well to my challenge. Here is the group pass; delivere it to the leader." ); 
 		return; 
 	} 

 	if ( prob == "" ) q = target.random( 0, 8 ); // Dish out questions. 
 	else q = Number(prob); // Repeat the question. 

 	// Explaining the questions and the answers 
 	desc = ""; 
 	if ( q == 0 ) { desc = "Assignment. The number of coupons you should collect is the same number of experience points required to advance from #rlevel 1 to level 2."; ans = 15; } 
 	else if ( q == 1 ) { desc = "Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível necessário para fazer o primeiro avanço na carreira como feiticeiro."; ans = 8; } 
 	else if ( q == 2 ) { desc = "Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível mínimo de FOR necessária para fazer o primeiro avanço na carreira como guerreiro."; ans = 35; } 
 	else if ( q == 3 ) { desc = "Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível mínimo de INT necessária para fazer o primeiro avanço na carreira como bruxo."; ans = 20; } 
 	else if ( q == 4 ) { desc = "Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível mínimo de DES necessária para fazer o primeiro avanço na carreira como arqueiro."; ans = 25; } 
 	else if ( q == 5 ) { desc = "Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível mínimo de DES necessária para fazer o primeiro avanço na carreira como gatuno."; ans = 25; } 
 	else if ( q == 6 ) { desc = "Here's the question. Collect the same number of coupons as the minimum level required to make the first job advancement as warrior.."; ans = 10; } 
 	else if ( q == 7 ) { desc = "Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível necessário para fazer o primeiro avanço na carreira como arqueiro."; ans = 10; } 
 	else if ( q == 8 ) { desc = "Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível necessário para fazer o primeiro avanço na carreira como gatuno."; ans = 10; } 

 	// If the question is given 
 	if ( prob == "" ) { 
 		quest.setVar( charName, String(q) ); 
 		self.say( "Você precisa coletar o mesmo número de #bcupons#k dos Jacarés que a reposta das minhas charadas individuais." ); 
 		self.say( desc ); 
 		return; 
 	} 

 	// Scoring 
 	inven = inventory; 
 	
 	if ( inven.itemCount( 4001007 ) == ans ) { 
 		if ( inven.exchange( 0, 4001007, -ans, 4001008, 1 ) == 0 ) { 
 			self.say( "You need to collect the number of coupons suggested by the response. Neither more nor less. Make sure you have the same coupons." ); 
 			return; 
 		} 
 		quest.setVar( charName, "clear" ); 
 		self.say( "Right answer! You just won one #bpasse#k. Please deliver it to the leader of your group" ); 
 	} 
 	else { 
 		self.say( "Incorrect answer. I can only give the pass if you collect the #bcupons#k suggested by the answer to the question. Let me repeat the question." ); 
 		self.say( desc ); 
 	} 
 } 

function party1_help() { 
 	field = target.fieldID(); 
 	if ( field == 103000801 ) self.say( "Vou descrever o 2º estágio. Você verá algumas cordas ao meu lado. #b3 delas estão conectadas ao portal para o próximo estágio#k. Tudo o que você precisa é que #b3 membros do grupo encontrem as cordas corretas e se segurem nelas#k.\r\nMAS isto não contará se você se pendurar muito embaixo. Por favor, suba o suficiente para a resposta ser considerada correta. E apenas 3 membros do seu grupo serão permitidos nas cordas. Quando isto acontecer, o líder do grupo deverá #bclicar duas vezes em mim para saber se a resposta está correta ou não#k. Boa sorte para vocês!" ); 
 	else if ( field == 103000802 ) self.say( "Vou descrever o 3º estágio. Você verá um monte de barris com gatinhos dentro no alto das plataformas. #b 3 dessas plataformas estarão conectadas ao portal que leva ao próximo estágio#k. #b3 membros do grupo precisam encontrar as plataformas corretas para subir e completar o estágio.\r\nMAS é preciso ficar firme no centro, e não na beira, para que a resposta seja considerada correta. E apenas 3 membros do seu grupo serão permitidos nas plataformas. Quando os membros estiverem nas plataformas, o líder do grupo deverá #bclicar duas vezes em mim para saber se a resposta está correta ou não#k. Boa sorte para vocês~!" ); 
 	else if ( field == 103000803 ) self.say( "Vou descrever o 4º estágio. Você verá um monte de barris por perto. #b 3 desses barris estarão conectados ao portal que leva ao próximo estágio#k. #b3 membros do grupo precisam encontrar os barris corretos e ficar em cima deles#k para completar o estágio. MAS, para a resposta contar, é preciso ficar bem firme no centro do barril, não na beira. E apenas 3 membros do seu grupo podem ficar em cima dos barris. Quando os membros estiverem em cima, o líder do grupo deverá #bclicar duas vezes em mim para saber se a resposta está correta ou não#k. Boa sorte para vocês!" ); 
 } 



function party1_stage1() { 
 	quest = target.FieldSet( "Party1" ); 
 	stage = quest.getVar( "stage" ); 
 	if ( stage == "" ) { 
 		quest.setVar( "stage", "1" ); 
 		self.say( "Hello. Welcome to the first stage. Look around and you'll see Ligators wandering around. When you defeat them, they will cough up a #bcoupon#k. Every member of the party other than the leader should talk to me, geta  question, and gather up the same number of #bcoupons#k as the answer to the question I'll give to them.\r\nIf you gather up the right amount of #bcoupons#k, I'll give the #bpass#k to that player. Once all the party members other than the leader gather up the #bpasses#k and give them to the leader, the leader will hand over the #bpasses#k to me, clearing the stage in the process. The faster you take care of the stages, the more stages you'll be able to challenge. So I suggest you take care of things quickly and swiftly. Well then, best of luck to you." ); 
 		return; 
 	} 
 	
 	if ( stage != "1" ) { 
 		self.say( "You have completed this stage. Proceed to the next stage using the portal. Caution..." ); 
 		return; 
 	} 

 	// checking the number of passes 
 	users = quest.getUserCount() - 1; // exclude the leader of the party 
 	
 	inven = inventory; 
 	if ( inven.itemCount( 4001008 ) < users) self.say( "Sorry, you do not have enough passes. You need to give me the correct number of passes; Must be the same number of members of your group minus the leader, #b" + users + "passes#k to complete the stage. Tell the members of your group to answer the questions, join the passes, and deliver to you." ); 
 	else { 
 		self.say( "Você juntou #b" + users + "passe#k! Parabéns por completar o estágio! Eu vou criar o portal que envia você para o próximo estágio. Há um limite de tempo para chegar lá, apresse-se. Boa sorte para todos vocês!" ); 
 		if ( inven.exchange( 0, 4001008, -users ) == 0 ) { 
 			self.say( "Você precisa me entregar o número correto de passes; deve ser o mesmo número de membros do seu grupo menos o líder. Nem mais nem menos. Por favor, verifique se você tem a quantidade correta." ); 
 			return; 
 		} 
 		field = quest; 
 		field.effectScreen( "quest/party/clear" ); 
 		field.effectSound( "Party1/Clear" ); 
 		field.effectObject( "gate" ); 
 		field.enablePortal( "next00", "kpq0" ); 
 		quest.setVar( "stage", "2" ); 
 		quest.incExpAll( 100 ); 
 	} 
 } 

function party1_stage2() { 
 	if ( check_stage( "2", 1 ) == 0 ) return; 

 	quest = target.FieldSet( "Party1" ); 
 	question = quest.getVar( "ans2" ); 
 	if ( question == "" ) { 
 		quest.setVar( "ans2", self.shuffle( 1, "1110" ) ); 
 		self.say( "Oi. Bem-vindo ao 2º estágio. Você verá algumas cordas perto de mim. #b3 dessas cordas estarão conectadas ao portal que leva ao próximo estágio#k. Tudo o que você precisa é que #b3 membros do grupo encontrem as cordas e se segurem nelas#k.\r\nMAS isto não conta como resposta correta se você se pendurar muito embaixo. Por favor, suba o suficiente para a resposta ser considerada correta. E apenas 3 membros do seu grupo serão permitidos nas cordas. Quando isto acontecer, o líder do grupo deverá #bclicar duas vezes em mim para saber se a resposta está correta ou não#k. Agora, encontre as cordas certas para se pendurar!" ); 
 		return; 
 	} 

 	field = quest; 
 	answer = area_check( stage2rects ); 
 	
 	if ( answer == "" ) self.say( "Parece que você ainda não encontrou as 3 cordas. Pense numa combinação diferente das cordas. Apenas 3 membros podem se pendurar nas cordas. E não se pendurem muito embaixo ou a resposta não irá contar. Continue!" ); 
 	else if ( question != answer ) { 
 		field.effectScreen( "quest/party/wrong_kor" ); 
 		field.effectSound( "Party1/Failed" ); 
 	} 
 	else { 
 		field.effectScreen( "quest/party/clear" ); 
 		field.effectSound( "Party1/Clear" ); 
 		field.effectObject( "gate" ); 
 		field.enablePortal( "next00", "kpq1" ); 
 		quest.setVar( "stage", "3" ); 
 		quest.incExpAll( 200 ); 
 	} 
 }

function party1_stage3 (){ 
 	if ( check_stage( "3", 1 ) == 0 ) return; 

 	quest = target.FieldSet( "Party1" ); 
 	question = quest.getVar( "ans3" ); 
 	if ( question == "" ) { 
 		quest.setVar( "ans3", self.shuffle( 1, "11100" ) ); 
 		self.say( "Olá. Bem-vindo ao 3º estágio. Em cima das plataformas, vocês verão alguns barris por perto com gatinhos dentro. Destas plataformas, #b3 levarão ao portal para o próximo estágio#k. #b3 membros do grupo precisam encontrar as plataformas corretas para subir e completar o estágio.\r\nMAS é preciso ficar firme no centro, e não na beira, para que a resposta seja considerada correta. E apenas 3 membros do seu grupo serão permitidos nas plataformas. Quando os membros estiverem nas plataformas, o líder do grupo deverá #bclicar duas vezes em mim para saber se a resposta está correta ou não#k. Agora, encontre as plataformas corretas~!" ); 
 		return; 
 	} 

 	field = quest; 
 	answer = area_check( stage3rects ); 
 	
 	if ( answer == "" ) self.say( "Parece que você ainda não encontrou as 3 plataformas. Pense numa combinação diferente das plataformas. E lembre-se de que apenas 3 membros podem ficar nas plataformas, firmes no centro, para que a resposta seja válida. Continue!" ); 
 	else if ( question != answer ) { 
 		field.effectScreen( "quest/party/wrong_kor" ); 
 		field.effectSound( "Party1/Failed" ); 
 	} 
 	else { 
 		field.effectScreen( "quest/party/clear" ); 
 		field.effectSound( "Party1/Clear" ); 
 		field.effectObject( "gate" ); 
 		field.enablePortal( "next00", "kpq2" ); 
 		quest.setVar( "stage", "4" ); 
 		quest.incExpAll( 400 ); 
 	} 
 } 
function party1_stage4 () { 
 	if ( check_stage( "4", 1 ) == 0 ) return; 

 	quest = target.FieldSet( "Party1" ); 
 	question = quest.getVar( "ans4" ); 
 	if ( question == "" ) { 
 		quest.setVar( "ans4", target.shuffle( 1, "111000" ) ); 
 		self.say( "Hi. Welcome to 4th stage. You will see some barrels close by. 3 of these barrels will be connected to the portal leading to the next stage. #b3 group members need to find the correct barrels and stay on top of them#k to complete the stage. BUT, for the answer to come, you have to stand firmly in the center of the barrel, not the edge. And only 3 members of your group can stay on top of the barrels. When members are on top, the leader of the group should #bclick twice on me to know if the awnser is correct#k. Now, find the right barrels~!" ); 
 		return; 
 	} 

 	field = quest; 
 	answer = area_check( stage4rects );  
 	
 	if ( answer == "" ) self.say( "It looks like you have not found the 3 barrels yet. Think of a different combination of barrels. And do not forget that only 3 members can stay on top of the barrels, firm in the center for the answer to count as correct. Continues!" ); 
 	else if ( question != answer ) { 
 		field.effectScreen( "quest/party/wrong_kor" ); 
 		field.effectSound( "Party1/Failed" ); 
 	} 
 	else { 
 		field.effectScreen( "quest/party/clear" ); 
 		field.effectSound( "Party1/Clear" ); 
 		field.effectObject("gate" ); 
 		field.enablePortal( "next00", "kpq3" );  
 		quest.setVar( "stage", "5" ); 
 		quest.incExpAll( 800 ); 
 	} 
 } 

function party1_stage5 (){ 
 	if ( check_stage( "5", 1 ) == 0 ) return; 

 	quest = target.FieldSet( "Party1" ); 

 	inven = inventory; 
 	if ( inven.itemCount( 4001008 ) < 10 ) self.say( "Hello. Welcome to the 5th final stage. Walk around the map and you will see some Chief Monsters. Defeat all and collect 10 #bpasses#k for me. Obtaining your pass, the leader of your group will join them and give me as long as you have all 10. The monsters may look familiar but they are much stronger than you think. So be careful. Good luck!" ); 
 	else { 
 		if ( inven.exchange( 0, 4001008, -10 ) == 0 ) { 
 			self.say( "You have done away with all the Chief Monsters and collected #b10 passes#k. Good work." ); 
 			return; 
 		} 
 		field = quest; 
 		field.effectScreen( "quest/party/clear" ); 
 		field.effectSound( "Party1/Clear" ); 
 		field.effectObject( "gate" ); 
 		quest.setVar( "stage", "clear" ); 
 		quest.incExpAll( 1500 ); 
 		self.say( "Here is the portal that leads to the last stage of bonuses. It is a stage that allows you to defeat ordinary monsters a little more easily. You will have a time limit to defeat as many of them as possible, but you can leave the stage when you want to talk to the NPC. Once again, congratulations on completing all the stages. Caution..." ); 
 		return; 
 	} 
 } 

function party1_reward (){ 
 	self.say( "Amazing! You have completed all the stages to get here. Here is a small reward for the job well done. But before accepting, make sure you have available slots in use inventories and so on." ); 

 	inven = inventory; 
 	if ( inven.slotCount( 2 ) > inven.holdCount( 2 ) || inven.slotCount( 4 ) > inven.holdCount( 4 ) ) { 
 		rnum = self.random( 0, 39 ); 
                 nNewItemID = 0; 
                 nNewItemNum = 0; 
 		if ( rnum == 0 ) { 
 			nNewItemID = 2000004; 
 			nNewItemNum = 5; 
 		} 
 		else if ( rnum == 1 ) { 
 			nNewItemID = 2000001; 
 			nNewItemNum = 100; 
 		} 
 		else if ( rnum == 2 ) { 
 			nNewItemID = 2000002; 
 			nNewItemNum = 70; 
 		} 
 		else if ( rnum == 3 ) { 
 			nNewItemID = 2000003; 
 			nNewItemNum = 100; 
 		} 
 		else if ( rnum == 4 ) { 
 			nNewItemID = 2000006; 
 			nNewItemNum = 50; 
 		} 
 		else if ( rnum == 5 ) { 
 			nNewItemID = 2022000; 
 			nNewItemNum = 15; 
 		} 
 		else if ( rnum == 6 ) { 
 			nNewItemID = 2022003; 
 			nNewItemNum = 15; 
 		} 
 		else if ( rnum == 7 ) { 
 			nNewItemID = 2040002; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 8 ) { 
 			nNewItemID = 2040402; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 9 ) { 
 			nNewItemID = 2040502; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 10 ) { 
 			nNewItemID = 2040505; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 11 ) { 
 			nNewItemID = 2040602; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 12 ) { 
 			nNewItemID = 2040802; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 13 ) { 
 			nNewItemID = 4003000; 
 			nNewItemNum = 30; 
 		} 
 		else if ( rnum == 14 ) { 
 			nNewItemID = 4010000; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 15 ) { 
 			nNewItemID = 4010001; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 16 ) { 
 			nNewItemID = 4010002; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 17 ) { 
 			nNewItemID = 4010003; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 18 ) { 
 			nNewItemID = 4010004; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 19 ) { 
 			nNewItemID = 4010005; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 20 ) { 
 			nNewItemID = 4010006; 
 			nNewItemNum = 5; 
 		} 
 		else if ( rnum == 21 ) { 
 			nNewItemID = 4020000; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 22 ) { 
 			nNewItemID = 4020001; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 23 ) { 
 			nNewItemID = 4020002; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 24 ) { 
 			nNewItemID = 4020003; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 25 ) { 
 			nNewItemID = 4020004; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 26 ) { 
 			nNewItemID = 4020005; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 27 ) { 
 			nNewItemID = 4020006; 
 			nNewItemNum = 8; 
 		} 
 		else if ( rnum == 28 ) { 
 			nNewItemID = 4020007; 
 			nNewItemNum = 3; 
 		} 
 		else if ( rnum == 29 ) { 
 			nNewItemID = 4020008; 
 			nNewItemNum = 3; 
 		} 
 		else if ( rnum == 30 ) { 
 			nNewItemID = 1032002; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 31 ) { 
 			nNewItemID = 1032004; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 32 ) { 
 			nNewItemID = 1032005; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 33 ) { 
 			nNewItemID = 1032006; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 34 ) { 
 			nNewItemID = 1032007; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 35 ) { 
 			nNewItemID = 1032009; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 36 ) { 
 			nNewItemID = 1032010; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 37 ) { 
 			nNewItemID = 1002026; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 38 ) { 
 			nNewItemID = 1002089; 
 			nNewItemNum = 1; 
 		} 
 		else if ( rnum == 39 ) { 
 			nNewItemID = 1002090; 
 			nNewItemNum = 1; 
 		} 
 		ret = inven.exchange( 0, nNewItemID, nNewItemNum ); 
 		if ( ret == 0 ) self.say( "Hmmm ... you are sure that your usage inventory and etc. Have space? I can not reward you for the effort if your inventories are full." ); 
 		else target.registerTransferField( 103000805, "" ); 
 	} 
 	else self.say( "Your usage inventory and etc, need to have at least one empty slot to receive the rewards. Please make the necessary adjustments and talk to me." ); 
 } 





start();
