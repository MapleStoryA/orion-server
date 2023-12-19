function s4common1() {
    qr = target.questRecord();
    inven = target.inventory();
    if (qr.getState(6192) == 1) {
        if (target.isPartyBoss() == 1) {
            if (target.nJob == 112 ||
                target.nJob == 122
                ||
                target.nJob == 132
            ) {
                if (inven.itemCount(4031495) <= 0) {
                    s4common1_Pcheck;
                    quest = FieldSet("S4common1");
                    res = quest.enter(target.nCharacterID, 0);
                    if (res == -1) self.say("Não podemos partir por um motivo desconhecido. Tente novamente mais tarde.");
                    else if (res == 1) self.say("Você não tem um grupo. Forme um grupo com seus amigos para começar.");
                    else if (res == 3) self.say("Alguém do grupo está em um nível menor que 120.");
                    else if (res == 4) self.say("Outro grupo já começou a missão. Aguarde um momento e tente novamente.");
                    else {
                        quest.resetQuestTime;
                    }
                } else self.say("Você já conquistou minha confiança. Não precisa me proteger novamente.");
            } else
                self.say("O Guerreiro tem de ser o líder do grupo.");
        } else self.say("Somente o líder do grupo pode decidir quando entrar.");
    } else self.say("Acho que não há chance de lhe pedir para me proteger.");
    return;
}


function warrior3() {
    qr = target.questRecord();

    if (target.nLevel() < 50) {
        self.say("Humm... Parece que não há nada que eu possa fazer para ajudar você. Volte aqui quando ficar mais forte.");
        return;
    } else if (target.nLevel() >= 50) {
        text = "Posso #Gajudá-lo:ajudá-la#?\r\n#b";
        text = text + "#L0#Gostaria de fazer o avanço para a terceira classe #l\r\n";
        text = text + "#L1#Por favor, deixe-me fazer a Missão do Zakum #l\r\n";
        nRet = self.askMenu(text);
    }

    cLevel = target.nLevel();
    cJob = target.nJob();

    if (nRet == 0) {
        val = qr.getState(7500);
        info = qr.get(7500);

        if (cLevel >= 70) {
            if (cJob == 110 ||
                cJob == 120
                ||
                cJob == 130
            ) {
                inventory = target.inventory();
                if (val == 0) {
                    nRet = self.askYesNo("Bem-vindo. Sou #bTylus#k, o chefe de todos os guerreiros, encarregado de despertar o melhor nos guerreiros que precisam da minha orientação. Você parece ser o tipo de guerreiro que deseja dar um passo à frente, parece #Gpronto:pronta# para encarar os desafios do 3º nível de carreira. Mas já vi muitos guerreiros prontos para dar o salto, assim como você, somente para fracassar no final. E você? Está #Gpronto:pronta# para tentar o teste e subir para o 3º nível de carreira?");
                    if (nRet == 0) self.say("Não creio que você esteja #Gpronto:pronta# para encarar os desafios que estão por vir. Venha me ver somente quando convencer a si mesmo de que está #Gpronto:pronta# para encarar os desafios que vêm junto com o seu avanço.");
                    else {
                        qr.set(7500, "s");
                        self.say("Bom. Você será #Gtestado:testada# em dois aspectos importantes para os guerreiros: força e sabedoria. Vou lhe explicar agora a parte física do teste. Lembra do #b#p1022000##k de Perion? Vá até ele e ouça sua explicação sobre a primeira parte do teste. Conclua a missão e receba #b#t4031057##k do #p1022000#.");
                        self.say("A porção mental do teste começará somente quando você passar na parte física. #b#t4031057##k será a prova de que você passou no teste. Vou informar o #b#p1022000##k que você está indo até ele. Fique preparado. Não será fácil, mas tenho muita fé em você. Boa sorte!");
                    }
                } else if (val == 1 && (info == "s"
                    ||
                    info == "p1"
                ))
                    self.say("Você não tem #b#t4031057##k com você. Vá ver o #b#p1022000##k de Perion, passe no teste e traga #b#t4031057##k com você. Somente então você poderá fazer o segundo teste. Boa sorte para você.");
                else if (val == 1 &&
                    info == "p2"
                ) {
                    if (inventory.itemCount(4031057) >= 1) {
                        self.say("Parabéns por ter completado a parte física do teste. Eu sabia que você conseguiria. Agora que você passou na primeira parte do teste, poderá fazer a segunda parte. Dê-me o colar primeiro.");
                        ret = inventory.exchange(0, 4031057, -1);
                        if (ret == 0) self.say("Tem certeza de que possui #b#t4031057##k do #b#p1022000##k? Não se esqueça de deixar um espaço no seu inventário de Etc.");
                        else {
                            qr.set(7500, "end1");
                            self.say("Aqui está a 2ª parte do teste. Este teste determinará se você é #Gesperto:esperta# o suficiente para dar o próximo passo em direção à grandiosidade. Existe uma área sombria e coberta de neve chamada de Solo Sagrado no campo de neve em Ossyria. Nem mesmo os monstros conseguem chegar até lá. Na porção central da região, existe uma pedra gigante chamada Pedra Sagrada. Você terá de oferecer um item especial como sacrifício. A Pedra Sagrada testará sua sabedoria lá mesmo.");
                            self.say("Você terá de responder a todas as perguntas com honestidade e convicção. Se você responder a tudo corretamente, a Pedra Sagrada o aceitará formalmente e lhe entregará #b#t4031058##k. Traga o colar de volta e eu ajudarei você a dar o próximo passo. Boa sorte!");
                        }
                    } else self.say("Você não tem #b#t4031057##k com você. Vá ver o #b#p1022000##k de Perion, passe no teste e traga #b#t4031057##k com você. Somente então você passará na primeira parte do teste. Boa sorte!");
                } else if (val == 1 &&
                    info == "end1"
                ) {
                    if (inventory.itemCount(4031058) >= 1) {
                        self.say("Parabéns por ter completado a parte mental do teste. Você respondeu a todas as perguntas corretamente e com sabedoria. Devo dizer que estou bastante impressionado com o nível de sabedoria que você demonstrou. Entregue-me o colar primeiro, antes de darmos o próximo passo.");
                        ret = inventory.exchange(0, 4031058, -1);
                        if (ret == 0) self.say("Tem certeza de que possui #b#t4031058##k concedido pela Pedra Sagrada? Se tiver certeza, não se esqueça de deixar um espaço no seu inventário de Etc.");
                        else {
                            qr.setComplete(7500);
                            changeJob1(cJob);
                        }
                    } else self.say("Você não tem #b#t4031058##k com você. Encontre a área sombria e coberta de neve chamada de Solo Sagrado no campo de neve em Ossyria, ofereça o item especial como sacrifício e responda a todas as perguntas com honestidade e convicção para receber #b#t4031058##k. Traga isso de volta para mim para concluir o teste do 3º nível de carreira. Boa sorte para você...");
                } else if (val == 2) changeJob1(cJob);
            } else if (cJob == 111) self.say("Foi você quem passou dos testes para subir para o 3º nível de carreira. Como é a vida de um #bTemplário#k? Você terá de continuar treinando conforme realiza sua jornada por este lugar. Ossyria está cheia de monstros poderosos que nem mesmo eu conheço. Se tiver alguma dúvida, vá falar comigo no fim desta estrada. Desejo-lhe boa sorte.");
            else if (cJob == 121) self.say("Foi você quem passou nos testes para subir para o 3º nível de carreira. Como é a vida de um #bCavaleiro Branco#k? Você terá de continuar treinando conforme realiza sua jornada por este lugar. Ossyria está cheia de monstros poderosos que nem mesmo eu conheço. Se tiver alguma dúvida, vá falar comigo no fim desta estrada. Desejo-lhe boa sorte.");
            else if (cJob == 131) self.say("Foi você quem passou nos testes para subir para o 3º nível de carreira. Como é a vida de um #bCavaleiro Draconiano#k? Você terá de continuar treinando conforme realiza sua jornada por este lugar. Ossyria está cheia de monstros poderosos que nem mesmo eu conheço. Se tiver alguma dúvida, vá falar comigo no final desta estrada. Desejo-lhe boa sorte.");
            else if (cJob == 112) self.say("Foi você quem conseguiu se tornar o mais forte dos Guerreiros. Você terá de servir de exemplo para todos como #bHerói#k. Ainda existem muito segredos neste mundo. O poder de Guerreiros como você será de grande ajuda. Por isso, continue treinando.");
            else if (cJob == 122) self.say("Foi você quem conseguiu se tornar o mais forte dos Guerreiros. Você terá de servir de exemplo para todos como #bPaladino#k. Ainda existem muito segredos neste mundo. O poder de Guerreiros como você será de grande ajuda. Por isso, continue treinando.");
            else if (cJob == 132) self.say("Foi você quem conseguiu se tornar o mais forte dos Guerreiros. Você terá de servir de exemplo para todos como #bCavaleiro Negro#k. Ainda existem muito segredos neste mundo. O poder de Guerreiros como você será de grande ajuda. Por isso, continue treinando.");
            else self.say("Sou #bTylus#k, o chefe de todos os guerreiros, encarregado de despertar o melhor nos guerreiros que precisam da minha orientação. Mas você não se parece com um guerreiro. Infelizmente, não posso #Gajudá-lo:ajudá-la#. Esta sala está cheia de chefes com suas respectivas classes. Se precisar de alguma coisa, vá falar com um deles.");
        } else self.say("Você ainda não está #Gqualificado:qualificada# para fazer o 3º avanço de carreira. Você precisa ter no mínimo nível 70 para isso. Treine mais e depois venha me ver.");
    } else if (nRet == 1) {
        val2 = qr.get(7000);

        if (val2 == "") {
            if (cLevel >= 50) {
                if (cJob >= 0 &&
                    cJob < 200
                ) {
                    qr.set(7000, "s");
                    self.say("Você quer permissão para realizar a Missão da Masmorra de Zakum, certo? Deve ser #b#p2030008##k... ok, certo! Tenho certeza de que você ficará bem na masmorra. Espero que tome cuidado por lá...");
                } else
                    self.say("Você quer permissão para realizar a Missão da Masmorra de Zakum. Sinto muito, mas você não parece um guerreiro. Vá procurar o chefe da sua profissão.");
            } else self.say("Você quer permissão para realizar a Missão da Masmorra de Zakum. Sinto muito, mas a masmorra é muito difícil para você. Você deve ter no mínimo nível 50 para pelo menos tentar... treine mais e depois volte aqui.");
        } else self.say("Como você está se saindo na Missão da Masmorra de Zakum? Ouvi falar que existe um monstro incrível nas profundezas desse lugar... de qualquer forma, boa sorte. Tenho certeza de que você vai conseguir.");
    } else if (nRet == 2) {
        s4common1();
    } else self.say("Sob construção... Por favor, aguarde...");
}

warrior3();