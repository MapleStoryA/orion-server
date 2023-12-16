import './global';
function fighterAction() {
    let qr = target.questRecord();
    let val = qr.get(7500);
    let cJob = target.nJob();

    val = qr.get(7500);



    if (val == "s" && (cJob == 110
        ||
        cJob == 120
        ||
        cJob == 130
    )) {
        qr.set(7500, "p1");
        self.say("Estava esperando você. Alguns dias atrás, #bTylus#k de Ossyria me falou de você. Bem... Eu gostaria de testar sua força. Existe uma passagem secreta perto do túnel de formigas. Só você poderá atravessá-la. Quando estiver lá dentro, você encontrará meu outro eu. Derrote-o e traga o #b#t4031059##k para mim.");
        self.say("Meu outro eu é bastante forte. Ele usa muitas habilidades especiais e você deverá travar uma luta corpo a corpo com ele. Entretanto, não é possível permanecer muito tempo na passagem secreta. É essencial que você o derrote o mais rápido possível. Bem... Boa sorte! Fico aguardando você trazer o #b#t4031059##k para mim.");
    } else if (val == "p1") {
        if (inventory.itemCount(4031059) >= 1) {
            self.say("Uau... Você derrotou meu outro eu e trouxe o #b#t4031059##k para mim. Muito bom! Isso certamente prova sua força. Em termos de força, você já pode ir para o 3º nível nde classe. Conforme prometido, darei  #b#t4031057##k a você. Entregue este colar a #bTylus#k de Ossyria e poderá fazer um segundo teste para o 3º nível de classe. Boa sorte~.");
            let ret = inventory.exchange(0, 4031059, -1, 4031057, 1);
            if (ret == 0) self.say("Hum... que estranho. Tem certeza de que está com o #b#t4031059##k? Se estiver, certifique-se de que possui um slot vazio na guia de itens.");
            else qr.set(7500, "p2");
        } else self.say("Existe uma passagem secreta perto do túnel de formigas. Só você poderá atravessá-la. Quando estiver lá dentro, você encontrará meu outro eu. Derrote-o e traga o #b#t4031059##k para mim.");
    } else if (val == "p2") {
        if (inventory.itemCount(4031057) <= 0) {
            self.say("Ahh! Você perdeu #b#t4031057##k, hein? Eu disse que deveria tomar cuidado... Pelo amor de Deus, vou te dar outro... DE NOVO. Por favor, tenha cuidado desta vez. Sem isto, você não poderá fazer o teste para o 3º nível de classe.");
            let ret = inventory.exchange(0, 4031057, 1);
            if (ret == 0) self.say("Hum... que estranho. Certifique-se de que possui um slot vazio na guia de itens.");
        } else self.say("Entregue este colar a #bTylus#k de Ossyria e poderá fazer um segundo teste para o 3º nível de classe. Boa sorte~!");
    } else {
        if (target.nJob() == 100) {
            if (target.nLevel() >= 30) {
                if (inventory.itemCount(4031008) >= 1) self.say("Ainda não encontrou a pessoa? Encontre o #b#p1072000##k que está perto de #b#m102020300##k próximo a #m102000000#. Entregue a carta para ele e talvez ele te diga o que você precisa fazer.");
                else if (inventory.itemCount(4031012) >= 1) {
                    self.say("Ahh! Você voltou sem nenhum arranhão! Eu sabia que você iria tirar de letra... admito que você é um Guerreiro forte e formidável... certo, agora te tornarei um Guerreiro ainda mais forte do que já é... MAS, antes disso, você terá de escolher um dos três caminhos que te serão oferecidos... não vai ser fácil, por isso, se tiver alguma pergunta, manda ver!");
                    let v1 = self.askMenu("Certo, quando tomar sua decisão, clique em [Quero escolher minha classe!] na parte inferior.\r\n#b#L0#Explique-me o papel do Soldado.#k#l\r\n#b#L1#Explique-me o papel do Escudeiro.#k#l\r\n#b#L2#Explique-me o papel do Lanceiro.#k#l\r\n#b#L3#Quero escolher minha classe!#k#l");
                    if (v1 == 0) {
                        self.say("Deixe-me explicar o papel do Soldado. Ele é o tipo mais comum de Guerreiro. As armas utilizadas são a #b espada #k e o #b machado #k, pois existem habilidades avançadas que poderão ser adquiridas mais tarde. Não utilize as duas armas ao mesmo tempo. Fique apenas com aquela que te agradar mais...");
                        self.say("Além disso, também existem habilidades como #b#q1101006##k e #b#q1101007##k disponíveis para os soldados. #b#q1101006##k é o tipo de habilidade que permite que você e seu grupo aprimorem suas armas. Com ela, você poderá derrubar seus inimigos com uma carga repentina de poder, o que a torna muito útil. A desvantagem é que sua habilidade de proteção (defesa) ficará um pouco reduzida.");
                        self.say("#b#q1101007##k é a habilidade que permite que você devolva uma porção do dano causado pela arma do inimigo. Quanto maior for o ataque, pior será o dano que sofrerão de volta. Isso ajudará aqueles que preferem combates de perto. O que você acha? Não é legal ser um Soldado?");
                    } else if (v1 == 1) {
                        self.say("Deixe-me explicar o papel do Escudeiro. O Escudeiro é um aprendiz de cavaleiro dando seus primeiros passos. Ele costuma usar #bespadas#k e/ou #bmaças#k. Não é uma boa idéia usar as duas armas, então é melhor você escolher uma e permanecer com ela.");
                        self.say("Além disso, também existem habilidades como #b#q1201006##k e #b#q1101007##k para você aprender. #b#q1201006##k faz qualquer oponente ao seu redor perder algumas habilidades de ataque e defesa por um certo tempo. Ela é muito útil contra monstros poderosos com boas habilidades de ataque. Também funciona bem em jogos cooperativos.");
                        self.say("#b#q1101007##k é uma habilidade que te permite devolver por um certo tempo uma determinada quantidade do dano causado pelos monstros. Quanto maior for o dano que você receber, mais dano você também causa ao inimigo. É a habilidade perfeita para os Guerreiros que estão se especializando em combates corpo a corpo. O que você acha? Não é legal ser um Escudeiro?");
                    } else if (v1 == 2) {
                        self.say("Deixe-me explicar o papel do Lanceiro. É uma classeque se especializa no uso de armas longas, como #blanças#k e #blanças de batalha#k. Existem muitas habilidades úteis para serem adquiridas com ambas as armas, mas recomendo que você escolha uma e permaneça com ela.");
                        self.say("Além disso, também existem habilidades como #b#q1301006##k e #b#q1301007##k para você aprender. #b#q1301006##k permite que você e os membros do seu grupo aprimorem a defesa de ataque e magia por um certo tempo. É uma habilidade bastante útil para os Lanceiros com armas que exigem as duas mãos e não conseguem se defender ao mesmo tempo.");
                        self.say("#b#q1301007##k é uma habilidade que permite que você e seu grupo aprimorem temporariamente o HP e MP máximos. Você poderá realizar um aumento de até 160%, logo a habilidade ajudará você e seu grupo principalmente quando estiverem partindo para cima de oponentes realmente poderosos. O que você acha? Você não acha legal ser um Lanceiro?");
                    } else if (v1 == 3) {
                        let v2 = self.askMenu("Hummm, já se decidiu? Escolha o 2º nível na classe de sua preferência.\r\n#b#L0#Soldado#k#l\r\n#b#L1#Escudeiro#k#l\r\n#b#L2#Lanceiro#k#l");
                        if (v2 == 0) {
                            let mJob = self.askYesNo("Então você quer subir para o 2º nível de classe como #bSoldado#k? Depois que tomar sua decisão, você não poderá voltar atrás e escolher outra carreira... ainda quer isso?");
                            if (mJob == 0) self.say("Mesmo? Então você precisa pensar um pouco mais. Não há pressa... não é algo que se deva fazer de qualquer jeito... me informe quando tomar sua decisão, certo?");
                            else if (mJob == 1) {
                                let nPSP = (target.nLevel() - 30) * 3;
                                if (target.nSP() > nPSP) self.say("Hummm... você tem #bSP#k demais... você não pode subir para o 2º nívelde classe com tanto SP guardado. Use mais SP nas habilidades do 1º nível e volte mais tarde.");
                                else {
                                    let ret = inventory.exchange(0, 4031012, -1);
                                    if (ret == 0) self.say("Humm... Tem certeza de que possui #b#t4031012##k do #p1072000#? Não posso permitir subir um nível de classe sem isso.");
                                    //job adv. - warrior
                                    else {
                                        target.changeJob(110);
                                        target.incSP(1, 0);
                                        let incval = self.random(300, 350);
                                        target.incMHP(incval, 0);
                                        inventory.incSlotCount(2, 4);
                                        inventory.incSlotCount(4, 4);

                                        self.say("Certo! Você agora se tornou um #bSoldado#k! Um soldado luta para ser o mais forte entre os fortes e nunca pára de lutar. Nunca perca sua vontade de lutar e sempre se esforce. Vou ajudar você a se tornar mais forte do que já é.");
                                        self.say("Eu te entreguei um livro que contém a lista de habilidades que você pode adquirir como Soldado. Nesse livro, você vai encontrar várias habilidades que o Soldado pode aprender. Seus inventários de Uso e Etc. também foram expandidos com uma nova fileira de slots disponíveis. Seu MP máximo também cresceu... pode conferir.");
                                        self.say("Também te dei um pouco de #bSP#k. Abra o #bMenu de habilidades#k localizado no canto inferior esquerdo. Você poderá aprimorar as habilidades de 2º nível recém-adquiridas. Um aviso: Não é possível aprimorá-las de uma vez. Algumas delas ficarão disponíveis somente após você aprender outras habilidades. Não se esqueça disso.");
                                        self.say("Os soldados têm de ser fortes. Mas lembre que você não pode abusar desse poder e usá-lo contra um ser mais fraco. Use seu grande poder da maneira certa, pois... usá-lo da maneira certa é muito mais difícil que só ficar mais forte. Procure-me depois que já tiver avançado bastante.");
                                    }
                                }
                            }
                        } else if (v2 == 1) {
                            let mJob = self.askYesNo("Então você quer fazer o 2º nível de classe como #bEscudeiro#k? Lembre que, quando tomar sua decisão, você não poderá mais mudar de classe. Tem certeza de que quer fazer isso?");
                            if (mJob == 0) self.say("Mesmo? Precisa pensar melhor, né? Não se apresse, não se apresse. Não é algo que se deva fazer de qualquer jeito... venha falar comigo quando tomar sua decisão, certo?");
                            else if (mJob == 1) {
                                let nPSP = (target.nLevel() - 30) * 3;
                                if (target.nSP() > nPSP) self.say("Hummm, acho que você tem #bSP#k demais. Você não pode subir para o 2º nível de classe com tanto SP guardado. Use mais SP nas habilidades do 1º nível e volte mais tarde.");
                                else {
                                    let ret = inventory.exchange(0, 4031012, -1);
                                    if (ret == 0) self.say("Hummm, tem certeza de que possui #b#t4031012##k do #p1072000#? É melhor ter certeza, pois não posso permitir subir um nível de classe sem isso.");
                                    // Job Adv = Page
                                    else {
                                        target.changeJob(120);
                                        target.incSP(1, 0);
                                        let incval = self.random(100, 150);
                                        target.incMMP(incval, 0);
                                        inventory.incSlotCount(2, 4);
                                        inventory.incSlotCount(4, 4);

                                        self.say("Certo! Você agora se tornou um #bEscudeiro#k! Os Escudeiros têm a inteligência e a bravura de um Guerreiro... espero que você siga o caminho certo com a mentalidade certa... vou ajudar você a se tornar mais forte do que já é.");
                                        self.say("Entreguei-lhe um livro que contém a lista de habilidades que você pode adquirir como Escudeiro. Nesse livro, você encontrará várias habilidades que o Escudeiro poderá aprender. Seus inventários de Uso e Etc. também foram expandidos com uma nova fileira de slots disponíveis. Seu MP máximo também aumentou... pode conferir.");
                                        self.say("Também te dei um pouco de #bSP#k. Abra o #bMenu de habilidades#k localizado no canto inferior esquerdo. Você poderá aprimorar as habilidades de 2º nível recém-adquiridas. Um aviso: Não é possível aprimorá-las de uma vez. Algumas delas ficarão disponíveis somente após você aprender outras habilidades. Não se esqueça disso.");
                                        self.say("Os Escudeiros têm de ser fortes. Mas lembre que você não pode abusar desse poder e usá-lo contra um ser mais fraco. Use seu grande poder da maneira certa, pois... usá-lo da maneira certa é muito mais difícil que só ficar mais forte. Procure-me depois que já tiver avançado bastante.");
                                    }
                                }
                            }
                        } else if (v2 == 2) {
                            let mJob = self.askYesNo("Então você quer subir para o 2º nível de classe como #bLanceiro#k? Quando tomar sua decisão, você não poderá fazer avanços em nenhuma outra classe. Tem certeza disso?");
                            if (mJob == 0) self.say("Mesmo? Precisa pensar melhor sobre isso? Não se apresse, não se apresse. Não é algo que se deva fazer de qualquer jeito... venha falar comigo quando tomar sua decisão, certo?");
                            else if (mJob == 1) {
                                let nPSP = (target.nLevel() - 30) * 3;
                                if (target.nSP() > nPSP) self.say("Hummm... você tem #bSP#k demais... você não pode subir para o 2º nível de classe com tanto SP guardado. Use mais SP nas habilidades do 1º nível e volte mais tarde.");
                                else {
                                    let ret = inventory.exchange(0, 4031012, -1);
                                    if (ret == 0) self.say("Hummm... tem certeza de que possui #b#t4031012##k do #p1072000#? É melhor ter certeza. Você não poderá subir para o próximo nível de classe sem isso.");
                                    // Job Adv = spearman
                                    else {
                                        target.changeJob(130);
                                        target.incSP(1, 0);
                                        let incval = self.random(100, 150);
                                        target.incMMP(incval, 0);
                                        inventory.incSlotCount(2, 4);
                                        inventory.incSlotCount(4, 4);

                                        self.say("Certo! Você agora se tornou um #bLanceiro#k! O Lanceiro usa o poder da escuridão para derrubar os inimigos, sempre nas sombras... acredite em si mesmo e em seu incrível poder durante a sua jornada... vou ajudar você a se tornar mais forte do que já é.");
                                        self.say("Entreguei-lhe um livro que contém a lista de habilidades que você pode adquirir como Lanceiro. Nesse livro, você encontrará várias habilidades que o Lanceiro poderá aprender. Seus inventários de Uso e Etc. também foram expandidos com uma nova fileira de slots disponíveis. Seu MP máximo também cresceu... pode conferir.");
                                        self.say("Também te dei um pouco de #bSP#k. Abra o #bMenu de habilidades#k localizado no canto inferior esquerdo. Você poderá aprimorar as habilidades de 2º nível recém-adquiridas. Um aviso: Não é possível aprimorá-las todas de uma vez. Algumas delas ficarão disponíveis somente após você aprender outras habilidades. Não se esqueça disso.");
                                        self.say("Um Lanceiro precisa ser forte. Mas lembre que você não pode abusar desse poder e usá-lo contra um ser mais fraco. Use seu grande poder da maneira certa, pois... usá-lo da maneira certa é muito mais difícil que só ficar mais forte. Procure-me depois que já tiver avançado bastante. Estarei esperando você.");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    let nSec = self.askYesNo("Nossa, você realmente cresceu! Não é mais #Gpequeno:pequena# e #Gfraco:fraca#... agora consigo notar seu porte de Guerreiro! Impressionante... então, o que acha? Deseja se tornar mais forte do que já é? É só fazer um simples teste! Aceita?");
                    if (nSec == 0) self.say("Mesmo? Tornar-se mais forte rapidamente te ajudará muito durante sua jornada... se mudar de idéia no futuro, poderá voltar aqui quando quiser. Lembre que eu tornarei você muito mais #Gpoderoso:poderosa# do que já é.");
                    else if (nSec == 1) {
                        self.say("Bem pensado. Você é forte, não me entenda mal, mas ainda existe a necessidade de testar sua força e ver se o seu poder é real. O teste não é muito difícil, você se sairá bem... Aqui, pegue esta carta. Não a perca.");
                        let ret = inventory.exchange(0, 4031008, 1);
                        if (ret == 0) self.say("Hummm... não posso te dar a carta porque você não tem espaço suficiente no seu inventário de Etc. Volte depois de ter liberado um ou dois espaços no seu inventário, pois a carta é o único jeito de você fazer o teste.");
                        else self.say("Entregue esta carta ao #b#p1072000##k que pode estar perto de #b#m102020300##k próximo a #m102000000#. Ele está me substituindo como instrutor, pois ando ocupado por aqui. Entregue-lhe a carta e ele aplicará o teste em meu lugar. Outras informações serão passadas diretamente por ele a você. Boa sorte para você.");
                    }
                }
            } else {
                let v = self.askMenu("Ah, você tem uma pergunta? \r\n#b#L0#Quais são as características gerais de um Guerreiro?#l\r\n#L1#Quais são as armas de um Guerreiro?#l\r\n#L2#Quais são as armaduras de um Guerreiro?#l\r\n#L3#Quais são as habilidades disponíveis para um Guerreiro?#l");
                if (v == 0) {
                    self.say("Deixe-me explicar o papel do Guerreiro. Os Guerreiros possuem força física e poder incríveis. Eles também sabem se defender de ataques de monstros, por isso são os melhores para lutar em combates corpo a corpo com os monstros. Com um alto nível de vigor, você não vai morrer fácil.");
                    self.say("Entretanto, para atacar o monstro com precisão, você precisará de uma boa dose de DES, então não se concentre apenas em aprimorar sua FOR. Se quiser aprimorar-se rapidamente, recomendo que enfrente monstros mais fortes.");
                } else if (v == 1) {
                    self.say("Deixe-me explicar as armas que um Guerreiro utiliza. Ele usa armas que lhe permitem cortar, esfaquear e atacar. Você não poderá usar armas como arcos e armas de projéteis. Muito menos cajados pequenos.");
                    self.say("As armas mais comuns são espadas, maças, lanças de batalha, lanças, machados, etc... Toda arma tem suas vantagens e desvantagens, por isso examine-as bem antes de escolher uma delas. Por enquanto, tente usar aquelas com um nível elevado de ataque.");
                } else if (v == 2) {
                    self.say("Deixe-me explicar as armaduras que um Guerreiro utiliza. Os Guerreiros são fortes e têm muito vigor, por isso conseguem usar armaduras pesadas e resistentes. Elas não são muito bonitas... mas servem bem a seu propósito: serem as melhores armaduras.");
                    self.say("Os escudos, principalmente, são perfeitos para os Guerreiros.  Lembre-se, entretanto, de que você não poderá usar um escudo se estiver empunhando uma arma de duas mãos. Eu sei que será uma decisão difícil...");
                } else if (v == 3) {
                    self.say("As habilidades disponíveis para os Guerreiros são destinadas a sua força física e poder incríveis. Aquelas que aprimoram o combate corpo a corpo são as que ajudarão mais você. Também existe uma habilidade que permite recuperar seu HP. É melhor você se tornar um especialista nela.");
                    self.say("As duas habilidades de ataque disponíveis são #b#q1001004##k e #b#q1001005##k. #q1001004# é aquela que causa um grande dano em um único inimigo. Você poderá aprimorar essa habilidade desde o começo.");
                    self.say("Por sua vez, #q1001005# não causa muito dano, mas ataca vários inimigos em uma área de uma só vez. Você poderá usá-la somente quando já tiver aprimorado #q1001004# uma vez. Você decide.");
                }
            }
        } else if (target.nJob() == 0) {
            self.say("Você deseja se tornar um Guerreiro? Você precisa cumprir alguns critérios para isso. #bVocê precisa ter no mínimo nível 10, com pelo menos 35 de FOR#k. Vamos ver...");
            if (target.nLevel() > 9 &&
                target.nSTR() > 34
            ) {
                let nRet = self.askYesNo("Você definitivamente tem o aspecto de um Guerreiro. Pode não ter chegado lá ainda, mas já consigo ver um Guerreiro em você. O que você acha? Você deseja se tornar um Guerreiro?");
                if (nRet == 0) self.say("Mesmo? Precisa de mais tempo para pensar melhor sobre isso? Fique à vontade... não é algo que se deva fazer de qualquer jeito. Venha falar comigo quando tomar sua decisão.");
                else if (nRet == 1) {
                    if (inventory.slotCount(1) > inventory.holdCount(1)) {
                        self.say("A partir de agora, você será um Guerreiro! Continue se esforçando... Vou melhorar um pouco suas habilidades, na esperança de que você continue treinando para ser mais forte do que já é. Zaaaaaaz!!");
                        let ret = inventory.exchange(0, 1302077, 1);
                        if (ret == 0) self.say("Humm. Verifique se você tem pelo menos um slot vazio na sua janela de Equip. Eu gostaria de te dar uma arma como recompensa pelo seu primeiro nível de classe.");
                        else {
                            target.changeJob(100);
                            let incval = self.random(200, 250);
                            target.incMHP(incval, 0);
                            target.incSP(1, 0);
                            inventory.incSlotCount(1, 4); // equip
                            //inventory.incSlotCount(2, 4); // use
                            //inventory.incSlotCount(3, 4); // setup
                            //inventory.incSlotCount(4, 4); // etc
                            self.say("Você está muito mais forte agora. Além disso, todos os seus inventários têm slots a mais. Uma fileira inteira, para ser mais exato. Pode conferir. Eu apenas te dei um pouco de #bSP#k. Quando você abrir o #bmenu de habilidades#k no canto inferior esquerdo da tela, você verá as habilidades que poderá aprender usando SP. Um aviso: Você não poderá aumentá-las todas de uma vez. Existem também aquelas que ficarão disponíveis somente após você aprender algumas habilidades primeiro.");
                            self.say("Mais um aviso. Depois que escolher sua classe, tente continuar vivo pelo maior tempo que conseguir. Se você morrer, perderá o seu nível de experiência. Você não quer perder seus pontos de experiência que ganhou com tanto sacrifício, não é? Isso é tudo que posso te ensinar... daqui para frente, você terá de se esforçar cada vez mais para se tornar cada vez melhor. Venha me ver quando perceber que está se sentindo com mais poder do que agora.");
                            self.say("Ah, e... e se tiver alguma dúvida sobre ser um Guerreiro, é só vir me perguntar. Eu não sei TUDO, mas vou ajudar você com tudo o que sei. Até lá...");
                        }
                    } else self.say("Humm... Verifique se existe um slot vazio na sua janela de Equip. Estou tentando te dar uma arma como recompensa pelo seu desempenho.");
                }
            } else
                self.say("Não acredito que você já tenha as qualidades para ser um Guerreiro ainda. Você precisa treinar muito para se tornar um ou não conseguirá lidar com a situação. Torne-se bem mais forte e então venha me procurar.");
        } else if (target.nJob() == 110) self.say("Ahhh! É você! O que você acha? Como é a vida de um Soldado? Você... parece bem mais forte do que antes! Espero que continue se aprimorando.");
        else if (target.nJob() == 120) self.say("Ahhh... é você! O que você acha? Como é a vida de um Escudeiro? Sei que você ainda é um aprendiz, mas em breve o treinamento se encerrará e você será chamado de cavaleiro!");
        else if (target.nJob() == 130) self.say("Ahhh... é você! O que você acha? Como é a vida de um Lanceiro? Continue treinando com dedicação, pois um dia você se tornará um cavaleiro inigualável...");
        else if (target.nJob() == 111) self.say("Ahhh... Você finalmente se tornou um #bTemplário#k... Eu sabia que você não iria me decepcionar. Então, o que você acha da vida como Templário? Por favor, dedique-se e treine ainda mais.");
        else if (target.nJob() == 121) self.say("Ahhh... Você finalmente se tornou um #bCavaleiro#k... Eu sabia que você não iria me decepcionar. Então, o que você acha da vida como Cavaleiro? Por favor, dedique-se e treine ainda mais.");
        else if (target.nJob() == 131) self.say("Ahhh... Você finalmente se tornou um #bCavaleiro Draconiano#k... Eu sabia que você não iria me decepcionar. Então, o que você acha da vida como Cavaleiro Draconiano? Por favor, dedique-se e treine ainda mais.");
        else if (target.nJob() == 112) self.say("Ahhh... Você finalmente se tornou um #bHerói#k... Eu sabia que você não iria me decepcionar. Então, o que você acha da vida como Herói? Por favor, dedique-se e treine ainda mais.");
        else if (target.nJob() == 122) self.say("Ahhh... Você finalmente se tornou um #bPaladino#k... Eu sabia que você não iria me decepcionar. Então, o que você acha da vida como Paladino? Por favor, dedique-se e treine ainda mais.");
        else if (target.nJob() == 132) self.say("Ahhh... Você finalmente se tornou um #bCavaleiro Negro#k... Eu sabia que você não iria me decepcionar. Então, o que você acha da vida como Cavaleiro Negro? Por favor, dedique-se e treine ainda mais.");
        else self.say("Que físico magnífico! Que poder incrível! Guerreiros são os melhores!!!! O que você acha? Deseja subir de classe como guerreiro??");
    }
}
fighterAction();

