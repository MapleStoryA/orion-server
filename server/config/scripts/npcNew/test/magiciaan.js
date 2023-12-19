function magicianAction() {
    qr = target.questRecord;
    val = qr.get(7500);
    cJob = target.nJob;
    inventory = target.inventory;

    if (val == "s" && (cJob == 210
        ||
        cJob == 220
        ||
        cJob == 230
    )) {
        qr.set(7500, "p1");
        self.say("Estava esperando você. Alguns dias atrás, #bRobeira#k de Ossyria me falou de você. Bem... Eu gostaria de testar sua força. Existe uma passagem secreta perto da floresta de Ellinia. Só você poderá atravessá-la. Quando estiver lá dentro, você encontrará meu outro eu. Derrote-o e traga o #b#t4031059##k para mim.");
        self.say("Meu outro eu é bastante forte. Ele usa muitas habilidades especiais e você deverá travar uma luta corpo a corpo com ele. Entretanto, não é possível permanecer muito tempo na passagem secreta. É essencial que você o derrote o mais rápido possível. Bem... Boa sorte! Fico aguardando você trazer o #b#t4031059##k para mim.");
    } else if (val == "p1") {
        if (inventory.itemCount(4031059) >= 1) {
            self.say("Uau... Você derrotou meu outro eu e trouxe o #b#t4031059##k para mim. Muito bom! Isso certamente prova sua força. Em termos de força, você está #Gpronto:pronta# para o 3º nível de classe. Conforme prometido, darei #b#t4031057##k a você. Entregue este colar a #bRobeira#k de Ossyria e poderá fazer um segundo teste para o 3º nível de classe. Boa sorte~.");
            ret = inventory.exchange(0, 4031059, -1, 4031057, 1);
            if (ret == 0) self.say("Hum... que estranho. Tem certeza de que está com o #b#t4031059##k? Se estiver, certifique-se de que possui um slot vazio na guia de itens.");
            else qr.set(7500, "p2");
        } else self.say("Existe uma passagem secreta perto da floresta de Ellinia. Só você poderá atravessá-la. Quando estiver lá dentro, você encontrará meu outro eu. Derrote-o e traga o #b#t4031059##k para mim. Meu outro eu é bastante forte. Ele usa muitas habilidades especiais e você deverá travar uma luta corpo a corpo com ele. Entretanto, não é possível permanecer muito tempo na passagem secreta. É essencial que você o derrote o mais rápido possível. Bem... Boa sorte! Fico aguardando você trazer o #b#t4031059##k para mim.");
    } else if (val == "p2") {
        if (inventory.itemCount(4031057) <= 0) {
            self.say("Ahh... você perdeu #b#t4031057##k, hein? Eu disse que deveria tomar cuidado... Pelo amor de Deus, vou te dar outro... DE NOVO. Por favor, tenha cuidado desta vez. Sem isto, você não poderá fazer o teste para o 3º nível de classe.");
            ret = inventory.exchange(0, 4031057, 1);
            if (ret == 0) self.say("Hum... que estranho. Certifique-se de que possui um slot vazio na guia de itens.");
        } else self.say("Entregue este colar a #bRobeira#k de Ossyria e poderá fazer um segundo teste para o 3º nível de classe. Boa sorte~!");
    } else {
        if (target.nJob == 200) {
            if (target.nLevel >= 30) {
                if (inventory.itemCount(4031009) >= 1) self.say("Ainda não o viu? Vá encontrar o #b#p1072001##k que está perto de #b#m101020000##k próximo a #m101000000#... entregue-lhe esta carta e ele vai te dizer o que você terá que fazer...");
                else if (inventory.itemCount(4031012) >= 1) {
                    self.say("Você voltou inteiro. Muito bem. Eu sabia que você passaria nos testes com facilidade... certo, agora vou tornar você ainda mais forte. Mas antes disso... você terá de escolher um dos três caminhos que te serão oferecidos. Não vai ser uma decisão fácil, mas... se tiver alguma pergunta, manda ver.");
                    v1 = self.askMenu("Certo, quando tomar sua decisão, clique em [Quero escolher minha classe!] na parte inferior...\r\n#b#L0#Explique-me as características do Feiticeiro do Fogo e Veneno.#k#l\r\n#b#L1#Explique-me as características do Feiticeiro do Gelo e Luz.#k#l\r\n#b#L2#Explique-me as características do Clérigo.#k#l\r\n#b#L3#Quero escolher minha classe!#k#l");
                    if (v1 == 0) {
                        self.say("Permita-me explicar sobre o Feiticeiro do Fogo e Veneno. Ele se especializa em mágicas de fogo e veneno. habilidades como #b#q2101001##k, que permite que a sua magia e a de todo o seu grupo seja melhorada por um certo tempo, e #b#q2100000##k, que concede a você uma certa probabilidade de absorver um pouco do MP do inimigo, são essenciais para os Bruxos encarregados do ataque.");
                        self.say("Vou explicar a você um ataque mágico chamado #b#q2101004##k. Ele dispara flechas em chamas contra os inimigos, o que torna esse ataque a mais poderosa habilidade disponível entre as habilidades de 2º nível. Ele funcionará melhor contra os inimigos que são imunes a fogo no geral, pois o dano será bem maior. Por outro lado, se utilizá-lo contra inimigos que são resistentes a fogo, o dano será reduzido pela metade. Não se esqueça disso.");
                        self.say("Vou explicar a você um ataque mágico chamado #b#q2101005##k. Ele dispara bolhas venenosas contra os inimigos, e assim eles ficam envenenados. Depois disso, o HP do inimigo se reduzirá cada vez mais com o passar do tempo. Se a mágica não funcionar muito bem ou se o monstro tiver HP elevado, poderá ser uma boa idéia disparar quantas vezes for necessário para matá-lo com uma overdose de veneno...");
                    } else if (v1 == 1) {
                        self.say("Permita-me explicar sobre o Feiticeiro do Gelo e Luz. Ele se especializa em mágicas de gelo e Luz. habilidades como #b#q2101001##k, que permite que a sua magia e a de todo o seu grupo seja melhorada por um certo tempo, e #b#q2100000##k, que concede a você uma certa probabilidade de absorver um pouco do MP do inimigo, são essenciais para os Bruxos encarregados do ataque.");
                        self.say("Vou explicar a você um ataque mágico chamado #b#q2201004##k. Ele dispara estilhaços de gelo contra os inimigos e, embora não seja tão poderoso quanto #q2101004#, aqueles que forem atingidos pelo ataque ficarão congelados por um breve período de tempo. O dano será muito maior se o inimigo for imune a gelo. O oposto também vale, ou seja, se o inimigo estiver acostumado com gelo, o dano não será tão grande. Não se esqueça disso.");
                        self.say("Vou explicar a você um ataque mágico chamado #b#q2201005##k. É a única habilidade de 2º nível para Bruxos que pode ser considerado um Feitiço Completo e afeta vários monstros de uma vez. Pode não causar muito dano, mas a vantagem é causar dano a vários monstros ao seu redor. Entretanto, você pode atacar somente seis monstros de uma vez. Mesmo assim, é um ataque incrível.");
                    } else if (v1 == 2) {
                        self.say("Permita-me explicar sobre o Clérigo. Os Clérigos usam mágicas religiosas contra os monstros através de rezas e encantamentos. habilidades como #b#q2301004##k, que aprimora temporariamente a defesa de arma, a defesa de magia, precisão e esquiva, e #b#q2301003##k, que reduz uma certa quantidade do dano com arma, ajudam os Bruxos a compensarem suas fraquezas...");
                        self.say("O Clérigo é o único Feiticeiro capaz de realizar magias de recuperação. Os Clérigos são os únicos capazes de realizar magias de recuperação. Ela é chamada de #b#q2301002##k e, quanto maior for o valor de MP, INT e o nível desta habilidade, mais HP você irá recuperar. Ela também afeta os membros do seu grupo que estão próximos a você, portanto, é uma habilidade bastante útil, permitindo que você continue caçando sem a ajuda de uma poção.");
                        self.say("Os Clérigos também possuem um ataque mágico chamado #b#q2301005##k. É um feitiço que permite que o Clérigo dispare flechas fantasmas contra monstros. O efeito não é muito grande, mas pode causar um dano enorme em zumbis e outros monstros malignos. Esses monstros são totalmente imunes a ataques sagrados. O que você acha? Não é interessante?");
                    } else if (v1 == 3) {
                        v2 = self.askMenu("Bom, já se decidiu? Escolha a classe para a sua 2º mudança de classe.\r\n#b#L0#O Feiticeiro do Fogo e Veneno#k#l\r\n#b#L1#O Feiticeiro do Gelo e Luz#k#l\r\n#b#L2#Clérigo#k#l");
                        if (v2 == 0) {
                            mJob = self.askYesNo("Então você quer subir para o 2º nível de classe como #bFeiticeiro do Fogo e Veneno#k? Depois de tomar sua decisão, você não poderá voltar atrás e mudar sua classe. Está certo sobre sua decisão?");
                            if (mJob == 0) self.say("Mesmo? Precisa pensar melhor, né? Não se apresse, não se apresse. Não é algo que se deva fazer de qualquer jeito... venha falar comigo quando tomar sua decisão...");
                            else if (mJob == 1) {
                                nPSP = (target.nLevel - 30) * 3;
                                if (target.nSP > nPSP) self.say("Hummm... você tem #bSP#k demais... você não pode subir para o 2º nível de classe com tanto SP guardado. Use mais SP nas habilidades do 1º nível e volte mais tarde...");
                                else {
                                    ret = inventory.exchange(0, 4031012, -1);
                                    if (ret == 0) self.say("Hummm... tem certeza de que possui #b#t4031012##k do #p1072001#? É melhor ter certeza... pois você não pode subir para o 2º nível de classe sem isso...");
                                    else {
                                        target.nJob = 210;
                                        target.incSP(1, 0);
                                        incval = random(450, 500);
                                        target.incMMP(incval, 0);
                                        inventory.incSlotCount(4, 4);

                                        self.say("A partir de agora, você se tornou um #bFeiticeiro do Fogo e Veneno#k... Os Feiticeiros usam sua elevada inteligência e a força da natureza ao nosso redor para derrubar os inimigos... continue com os seus estudos, pois um dia eu o tornarei muito mais poderoso com o meu próprio poder...");
                                        self.say("Entreguei-lhe um livro que contém a lista de habilidades que você pode adquirir como Feiticeiro do Fogo e Veneno...Também expandi seu inventário de Etc., acrescentando uma fileira inteira, e seu MP máximo... pode conferir.");
                                        self.say("Também te dei um pouco de #bSP#k. Abra o #bMenu de habilidades#k localizado no canto inferior esquerdo. Você poderá aprimorar as habilidades de 2º nível recém-adquiridas. Um aviso: Não é possível aprimorá-las de uma vez. Algumas delas ficarão disponíveis somente após você aprender outras habilidades. Não se esqueça disso.");
                                        self.say("Os Feiticeiros têm de ser fortes. Mas lembre que você não pode abusar desse poder e usá-lo contra um ser mais fraco. Use seu grande poder da maneira certa, pois... usá-lo da maneira certa é muito mais difícil que só ficar mais forte. Procure-me depois que já tiver avançado bastante...");
                                    }
                                }
                            }
                        } else if (v2 == 1) {
                            mJob = self.askYesNo("Então você quer fazer o 2º avanço de carreira como #bFeiticeiro do Gelo e Luz#k? Depois de tomar sua decisão, você não poderá voltar atrás e mudar sua carreira... está certo sobre sua decisão?");
                            if (mJob == 0) self.say("Mesmo? Precisa pensar melhor, né? Não se apresse, não se apresse. Não é algo que se deva fazer de qualquer jeito... venha falar comigo quando tomar sua decisão, certo?");
                            else if (mJob == 1) {
                                nPSP = (target.nLevel - 30) * 3;
                                if (target.nSP > nPSP) self.say("Hummm, você tem #bSP#k demais. Você não pode subir para o 2º nível de classe com tanto SP guardado. Use mais SP nas habilidades do 1º nível e volte mais tarde...");
                                else {
                                    ret = inventory.exchange(0, 4031012, -1);
                                    if (ret == 0) self.say("Tem certeza de que possui a #b#t4031012##k do #p1072001#? É melhor ter certeza, pois não posso permitir que você suba de nível de classe sem isso...");
                                    else {
                                        target.nJob = 220;
                                        target.incSP(1, 0);
                                        incval = random(450, 500);
                                        target.incMMP(incval, 0);
                                        inventory.incSlotCount(4, 4);

                                        self.say("Certo, agora você se tornou um #bFeiticeiro do Gelo e Luz#k... Os Feiticeiros usam sua elevada inteligência e a força da natureza ao nosso redor para derrubar os inimigos... continue com os seus estudos, pois um dia eu vou te tornar muito mais #Gpoderoso:poderosa# com o meu próprio poder...");
                                        self.say("Entreguei-lhe um livro que contém a lista de habilidades que você pode adquirir como Feiticeiro do Gelo e Luz...Também expandi seu inventário de Etc. acrescentando uma nova fileira. Seu MP máximo também aumentou. Pode conferir.");
                                        self.say("Também te dei um pouco de #bSP#k. Abra o #bMenu de habilidades#k localizado no canto inferior esquerdo. Você poderá aprimorar as habilidades de 2º nível recém-adquiridas. Um aviso: Não é possível aprimorá-las todas de uma vez. Algumas delas ficarão disponíveis somente após você aprender outras habilidades. Não se esqueça disso.");
                                        self.say("Os Feiticeiros têm de ser fortes. Mas lembre que você não pode abusar desse poder e usá-lo contra um ser mais fraco. Use seu grande poder da maneira certa, pois... usá-lo da maneira certa é muito mais difícil que só ficar mais forte. Procure-me depois que já tiver avançado bastante. Estarei esperando você...");
                                    }
                                }
                            }
                        } else if (v2 == 2) {
                            mJob = self.askYesNo("Então você quer subir para o 2º nível de classe como #bClérigo#k? Depois que tomar sua decisão, você não poderá voltar atrás e escolher outra classe... tem certeza disso?");
                            if (mJob == 0) self.say("Mesmo? Precisa pensar melhor, né? Não se apresse, não se apresse. Não é algo que se deva fazer de qualquer jeito... venha falar comigo quando tomar sua decisão...");
                            else if (mJob == 1) {
                                nPSP = (target.nLevel - 30) * 3;
                                if (target.nSP > nPSP) self.say("Hummm... você tem #bSP#k demais. Você não poderá subir para o 2º nível de classe com tanto SP guardado. Use mais SP nas habilidades do 1º nível e volte mais tarde...");
                                else {
                                    ret = inventory.exchange(0, 4031012, -1);
                                    if (ret == 0) self.say("Tem certeza de que possui a #b#t4031012##k do #p1072001#? É melhor ter certeza... pois você não pode subir para o nível de classe sem isso...");
                                    else {
                                        target.nJob = 230;
                                        target.incSP(1, 0);
                                        incval = random(450, 500);
                                        target.incMMP(incval, 0);
                                        inventory.incSlotCount(4, 4);
                                        self.say("Certo, você será um #bClérigo#k a partir de agora. Os Clérigos sopram vida em cada organismo com uma fé inabalável em Deus. Nunca deixe de aprimorar sua fé... um dia, eu vou te ajudar a se tornar muito mais #Gpoderoso:poderosa#...");
                                        self.say("Eu te entreguei um livro que contém a lista de habilidades que você pode adquirir como Clérigo...Também expandi uma fileira do seu inventário de Etc. e seu MP máximo... pode conferir...");
                                        self.say("Também te dei um pouco de #bSP#k. Abra o #bMenu de habilidades#k localizado no canto inferior esquerdo. Você poderá aprimorar as habilidades de 2º nível recém-adquiridas. Um aviso: Não é possível aprimorá-las todas de uma vez. Algumas delas ficarão disponíveis somente após você aprender outras habilidades. Não se esqueça disso.");
                                        self.say("O Clérigo precisa de fé mais do que qualquer outra coisa. Mantenha sua fé em Deus e trate todos os indivíduos com o respeito e a dignidade que eles merecem. Continue se esforçando e um dia você terá ainda mais poder mágico e religioso... certo... me procure depois de ter feito mais peregrinações. Estarei esperando você...");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    nSec = self.askYesNo("Hummm... você cresceu bastante desde a última vez. Você está bastante diferente, mais #Galto:alta# e forte... agora consigo notar seu porte de Bruxo... então... o que você acha? Deseja se tornar mais forte do que já é? É só fazer um simples teste... quer tentar?");
                    if (nSec == 0) self.say("Mesmo? Tornar-se mais forte rapidamente te ajudará muito durante sua jornada... se mudar de idéia no futuro, poderá voltar aqui quando quiser. Lembre que eu tornarei você muito mais #Gpoderoso:poderosa# do que já é...");
                    else if (nSec == 1) {
                        self.say("Bom... você parece ser forte, é verdade, mas preciso ver se seu poder é real. O teste não será difícil e acho que você é capaz de passar por ele. Aqui, pegue esta carta. Não a perca.");
                        ret = inventory.exchange(0, 4031009, 1);
                        if (ret == 0) self.say("Creio que você não tenha espaço no seu inventário para receber minha carta. Libere espaço no seu inventário de Etc. e volte a falar comigo. Afinal, você poderá fazer o teste somente com a carta.");
                        else self.say("Leve esta carta ao #b#p1072001##k que está perto de #b#m101020000##k próximo a #m101000000#. Ele está me substituindo como instrutor... Entregue-lhe a carta e ele aplicará o teste em meu lugar. Ele lhe dará todas as informações necessárias. Boa sorte para você...");
                    }
                }
            } else {
                v = self.askMenu("Alguma pergunta sobre como ser um Bruxo?\r\n#b#L0#Quais são as características básicas de um Bruxo?#l\r\n#L1#Quais são as armas de um Bruxo?#l\r\n#L2#Quais são as armaduras de um Bruxo?#l\r\n#L3#Quais são as habilidades disponíveis para um Bruxo?#l");
                if (v == 0) {
                    self.say("Vou te falar mais sobre ser um Bruxo. Os Bruxos usam bem altos níveis de magia e inteligência. Eles podem usar o poder da natureza ao nosso redor para matar inimigos, mas são muito fracos em combates corpo a corpo. Seu vigor também não é elevado, portanto, tome cuidado e evite ser #Gmorto:morta# de qualquer maneira.");
                    self.say("O fato de você ser capaz de atacar monstros a distância o ajudará bastante. Tente aprimorar o nível de INT se quiser atacar seus inimigos com magias precisas. Quanto maior for sua inteligência, melhor você será em lidar com a sua magia...");
                } else if (v == 1) {
                    self.say("Vou te falar mais sobre as armas de um Bruxo. Na verdade, não significa muito para um Bruxo atacar seus oponentes com armas. Os Bruxos não têm força e destreza, então você poderá ter dificuldade para derrotar até mesmo uma lesma.");
                    self.say("Já os poderes mágicos são uma OUTRA história. Os Bruxos usam maças, cajados e varinhas. As maças são boas para, bem, ataques de força, mas... eu não recomendaria isso para um Bruxo, ponto final.");
                    self.say("Na verdade, cajados e varinhas são as armas preferenciais. Elas possuem poderes mágicos especiais e, por isso, melhoram o desempenho do Bruxo. É uma boa idéia carregar uma arma com muito poder mágico...");
                } else if (v == 2) {
                    self.say("Vou te falar mais sobre as armaduras de um Bruxo. Honestamente, os Bruxos não possuem muitas armaduras, já que têm pouca força física e pouco vigor. Suas habilidades de defesa também não são boas, então não sei se vai ajudar em alguma coisa...");
                    self.say("Algumas armaduras, entretanto, têm a habilidade de eliminar poder mágico, então podem proteger você contra ataques mágicos. Elas não vão ajudar muito, mas ainda é melhor do que não usar nada... então, não deixe de comprá-las se der tempo...");
                } else if (v == 3) {
                    self.say("As habilidades disponíveis para os Bruxos usam os altos níveis de inteligência e magia que os Bruxos possuem. Também estão disponíveis Proteção Arcana e Armadura Arcana, que evitam a morte de Bruxos com pouco vigor.");
                    self.say("As habilidades de ataque são #b#q2001004##k e #b#q2001005##k. Primeiramente, #q2001004# é uma habilidade que causa muito dano no oponente com um uso mínimo de MP.");
                    self.say("#q2001005#, por sua vez, usa muito MP para atacar o oponente DUAS VEZES. Mas você pode usar #q2001004# somente quando essa habilidade tiver mais que uma melhoria. Não se esqueça disso. Você decide o que fazer...");
                }
            }
        } else if (target.nJob == 0) {
            self.say("Você deseja ser um Bruxo? Você precisa cumprir alguns requisitos para isso. Você deve estar pelo menos no #bNível 8, com INT 20#k. Vamos ver se você tem o necessário para se tornar um Bruxo...");
            if (target.nLevel > 7 &&
                target.nINT > 19
            ) {
                nRet = self.askYesNo("Você definitivamente tem o aspecto de um Bruxo. Pode não ter chegado lá ainda, mas já consigo ver um Bruxo em você... o que você acha? Você deseja se tornar um Bruxo?");
                if (nRet == 0) self.say("Mesmo? Precisa pensar melhor, né? Não se apresse, não se apresse. Não é algo que se deva fazer de qualquer jeito... venha falar comigo quando tomar sua decisão...");
                else if (nRet == 1) {
                    inven = target.inventory;
                    if (inven.slotCount(1) > inven.holdCount(1)) {
                        self.say("Certo, agora você é um Bruxo, já que eu, #p1032001#, o líder dos Bruxos, estou te autorizando. Não é muito, mas vou te dar um pouco do que tenho...");
                        ret = inven.exchange(0, 1372043, 1);
                        if (ret == 0) self.say("Humm... Verifique se existe um slot vazio na sua janela de Equip. Gostaria de te dar uma arma para você treinar como recompensa por seu primeiro nível de classe.");
                        else {
                            target.nJob = 200;
                            incval = random(100, 150);
                            target.incMMP(incval, 0);
                            target.incSP(1, 0);
                            self.say("Você acabou de se equipar com muito mais poder mágico. Continue treinando e se torne cada dia melhor... vou observar você de vez em quando...");
                            self.say("Eu apenas te dei um pouco de #bSP#k. Quando você abrir o #bmenu de habilidades#k no canto inferior esquerdo da tela, você verá as habilidades que poderá aprender usando SP. Um aviso: Você não poderá aumentá-las todas de uma vez. Existem também aquelas que ficarão disponíveis somente após você aprender algumas habilidades primeiro.");
                            self.say("Mais um aviso. Depois que escolher sua carreira, tente continuar vivo pelo maior tempo que conseguir. Se você morrer, perderá todo o seu nível de experiência. Você não quer perder seus pontos de experiência ganhos com tanto sacrifício, não é?");
                            self.say("OK! Isso é tudo que posso te ensinar. Vá aos lugares, treine e se torne ainda melhor. Procure-me quando achar que já fez tudo o que podia e precisar de algo interessante. Estarei esperando você aqui...");
                            self.say("Ah, e... se tiver alguma dúvida sobre ser um Bruxo, é só perguntar. Eu não sei TUDO, para falar a verdade, mas vou ajudar você com tudo o que sei. Até lá...");
                        }
                    } else self.say("Humm... Verifique se existe um slot vazio no seu inventário de EQUIP, pois gostaria de te dar uma arma para você treinar como recompensa por seu primeiro nível de classe.");
                }
            } else
                self.say("Você precisa treinar mais para ser um Bruxo. Por isso, você tem de se esforçar para se tornar mais #Gpoderoso:poderosa# do que já é. Volte quando estiver mais forte.");
        } else if (target.nJob == 210) self.say("Ahhh... é você... o que você acha da vida como Feiticeiro? Você... parece bem à vontade com essas flechas em chamas agora... por favor, dedique-se e treine ainda mais.");
        else if (target.nJob == 220) self.say("Ahhh... é você... o que você acha da vida como Feiticeiro? Você... parece ser capaz de lidar com o gelo e o luz com facilidade... por favor, dedique-se e treine ainda mais.");
        else if (target.nJob == 230) self.say("Ahhh... é você... o que você acha da vida como Clérigo? Você... parece ser capaz de lidar com magia sagrada com facilidade... por favor, dedique-se e treine ainda mais.");
        else if (target.nJob == 211 &&
            target.nJob == 221
        )
            self.say("Ahhh... Você finalmente se tornou um #bMago#k... Eu sabia que você não iria me decepcionar. Então, o que você acha da vida como Mago? Por favor, dedique-se e treine ainda mais.");
        else if (target.nJob == 231) self.say("Ahhh... Você finalmente se tornou um #bSacerdote#k... Eu sabia que você não iria me decepcionar. Então, o que você acha da vida como Sacerdote? Por favor, dedique-se e treine ainda mais.");
        else self.say("Você gostaria de ter em mãos o poder da própria natureza? Poderá ser uma longa e difícil estrada, mas você certamente receberá uma recompensa no final, alcançando o ápice da arte dos feiticeiros...");
    }
}

function magician() {
    magicianAction();
}