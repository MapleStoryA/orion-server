function party1_takeawayitem(){
	inven = inventory; 
 	count = inven.itemCount( 4001007 ); 
 	if ( count > 0 ) inven.exchange( 0, 4001007, -count ); 
 	count = inven.itemCount( 4001008 ); 
 	if ( count > 0 ) inven.exchange( 0, 4001008, -count ); 
}

function start() {
	if (target.isPartyBoss() != 1) {
		self.say("Que tal você e seu grupo terminarem uma missão juntos? Aqui você vai encontrar obstáculos e problemas que só poderão ser resolvidos em equipe. Se quiser tentar, peça ao #blíder do seu grupo#k para falar comigo.");
		return;
	}

	setParty = target.FieldSet("Party1");
	res = setParty.enter(target.nCharacterID(), 0);
	if (res == -1)
		self.say("Tenho motivos para não deixar seu grupo entrar. Tente mais tarde, por favor.");
	else if (res == 1)
		self.say("Você não está no grupo. Você só pode fazer esta missão quando estiver no grupo.");
	else if (res == 2)
		self.say("Seu grupo não possui quatro membros. Volte quando tiver quatro membros.");
	else if (res == 3)
		self.say("Alguém no seu grupo não está entre os níveis 21 ~30. Por favor, verifique novamente.");
	else if (res == 4)
		self.say("Um outro grupo já entrou para completar a missão. Por favor, tente mais tarde.");
	else {
		party1_takeawayitem();
	}

};

start();
