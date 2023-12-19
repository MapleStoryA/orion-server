import './global';
if ( target.nJob() == 100 && target.nLevel() >= 30 ) {
    let nBlack = inventory.itemCount( 4031013 );
    if ( inventory.itemCount( 4031008 ) >= 1 ) {
        if ( nBlack == 0 ) {
            self.say( "Hummm... definitivamente � uma carta do #b#p1022000##k... ent�o voc� veio at� aqui para tentar o teste e subir para o 2� n�vel de classe como Guerreiro. Certo, vou explicar o teste para voc�. N�o se preocupe, n�o � dif�cil." );
            self.say( "Vou mandar voc� para um mapa secreto. Voc� ver� monstros que ainda n�o conhece. Eles se parecem com criaturas comuns, mas t�m um comportamento totalmente diferente. Eles n�o aprimoram seu n�vel de experi�ncia nem fornecem itens." );
            self.say( "Voc� receber� um item chamado #b#t4031013##k quando derrubar esses monstros. � uma bolinha de vidro especial feita por mentes malignas e sinistras. Junte 30 e v� falar com um colega meu que est� no mapa. � assim que voc� passar� no teste." );
            let nRet = self.askYesNo( "Assim que estiver dentro do mapa, voc� n�o poder� sair sem concluir a miss�o. Se voc� morrer, seu n�vel de experi�ncia ser� reduzido... ent�o � melhor voc� apertar os cintos e se preparar... bom, deseja come�ar agora?" );
            if ( nRet == 0 ) self.say( "Voc� n�o parece muito #Gpreparado:preparada# para isso. Procure-me s� quando estiver #GPRONTO:PRONTA#. N�o h� portais ou lojas por l�, ent�o � melhor voc� estar 100% #Gpreparado:preparada#." );
            else if ( nRet == 1 ) {
                self.say( "Certo, vou deixar voc� entrar! Derrote os monstros l� dentro, junte 30 itens do tipo #t4031013# e inicie uma conversa com um colega meu que tamb�m est� no mapa. Ele lhe dar� a #b#t4031012##k, provando de que voc� passou no teste. Boa sorte para voc�." );
                target.registerTransferField( 108000300, "" );
            }
        }
        else if ( nBlack > 0 ) {
            let nRet = self.askYesNo( "Ent�o voc� j� desistiu uma vez. N�o se preocupe, voc� sempre poder� refazer o teste. Agora... deseja voltar l� e tentar mais uma vez?" );
            if ( nRet == 0 ) self.say( "Voc� n�o parece muito #Gpreparado:preparada# para isso. Procure-me quando estiver #GPRONTO:PRONTA#. N�o h� portais ou lojas por l�, ent�o � melhor voc� estar 100% #Gpreparado:preparada#." );
            else if ( nRet == 1 ) {
                self.say( "Certo! Vou deixar voc� entrar! Sinto muito, mas terei de remover todas as suas bolinhas de gude antes. Derrote todos os monstros l� dentro, junte 30 itens do tipo #t4031013# e inicie uma conversa com um colega meu que tamb�m est� no mapa. Voc� receber� a #b#t4031012##k comprovando que passou no teste. Boa sorte para voc�." );
                inventory.exchange( 0, 4031013, -nBlack );
                target.registerTransferField( 108000300, "" );
            }
        }
    }
    else self.say( "Deseja se tornar um Guerreiro muito mais forte do que j� �? Deixe-me tomar conta disso. Voc� parece estar mais do que #Gqualificado:qualificada#. V� procurar o #b#p1022000##k de #m102000000# primeiro..." );
}
else if ( target.nJob() == 100 && target.nLevel() < 30 ) self.say( "Deseja se tornar um Guerreiro muito mais forte do que j� �? Deixe-me tomar conta disso, ent�o, mas... voc� parece #Gfraco:fraca# demais. Inicie um treinamento, torne-se mais #Gpoderoso:poderosa# e depois volte aqui." );
else if ( target.nJob() == 110 || target.nJob() == 120 || target.nJob() == 130 ) self.say( "Hummm... foi voc� quem passou no meu teste outro dia! O que voc� acha? Tornou-se mais forte? Bom! Agora consigo definitivamente notar seu porte de Guerreiro." );
