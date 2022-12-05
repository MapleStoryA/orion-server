importPackage(Packages.client);
importPackage(Packages.server);
importPackage(Packages.tools);

var status = 0;
var yes = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (cm.getPlayer().getMapId() == 0 || cm.getPlayer().getMapId() == 3) {
		if (mode == -1) {
			cm.dispose();
		} else {
			if (status == -1 && mode == 0) {
				cm.sendNext("Por favor, fale comigo de novo quando voce finalmente tomar sua decisao.");
				cm.dispose();
				return;
			} else if (status >= 0 && mode == 0) {
				yes = 1;
				cm.sendYesNo("Voce realmente quer comecar a sua jornada de imediato?");
			}
			if (mode == 1)
				status++;
			else
				status--;
			if (status == 0) {
				if (!cm.getJob().equals(MapleJob.BEGINNER) && !cm.getPlayer().isGM()) {
					status = 3;
					cm.sendNext("Voce nao pertence aqui.");
				} else {
					if (yes == 1) {
						status = 2;
						cm.sendNext("Parece que voce quer iniciar a sua viagem, sem ter o programa de treinamento. Entao, eu vou deixar voce passar para o campo de treinamento. Tenha cuidado!");
					} else {
						cm.sendYesNo("Bem-vindo ao mundo de MapleStory (#eMapleBR#n).\r\nO objetivo deste campo de treinamento e ajudar os iniciantes. Gostaria de entrar neste campo de treinamento? Algumas pessoas comecam sua jornada sem ter o programa de treinamento. Mas eu recomendo fortemente que voce faca o primeiro programa de treinamento.");
					}
				}
			} else if (status == 1) {
				cm.sendNext("Tudo bem, eu vou deixar voce entrar no campo de treinamento. Por favor, siga os exemplos de seu instrutor.");
			} else if (status == 2) {
				var statup = new java.util.ArrayList();
				var p = cm.getPlayer();
				/*
				var totAp = p.getRemainingAp() + p.getStr() + p.getDex() + p.getInt() + p.getLuk();		
				p.setStr(4);
				p.setDex(4);
				p.setInt(4);
				p.setLuk(4);
					p.setRemainingAp (totAp - 16);
					statup.add(new Pair(MapleStat.STR, java.lang.Integer.valueOf(4)));
					statup.add(new Pair(MapleStat.DEX, java.lang.Integer.valueOf(4)));
					statup.add(new Pair(MapleStat.LUK, java.lang.Integer.valueOf(4)));
					statup.add(new Pair(MapleStat.INT, java.lang.Integer.valueOf(4)));
				*/
				statup.add(new Pair(MapleStat.AVAILABLEAP, java.lang.Integer.valueOf(p.getRemainingAp())));
				cm.getC().getSession().write (MaplePacketCreator.updatePlayerStats(statup));
				cm.warp(1, 0);
				cm.dispose();
			} else if (status == 3) {
				var statup = new java.util.ArrayList();
				var p = cm.getPlayer();
				/*
				var totAp = p.getRemainingAp() + p.getStr() + p.getDex() + p.getInt() + p.getLuk();		
				p.setStr(4);
				p.setDex(4);
				p.setInt(4);
				p.setLuk(4);
				p.setRemainingAp (totAp - 16);
				statup.add(new Pair(MapleStat.STR, java.lang.Integer.valueOf(4)));
				statup.add(new Pair(MapleStat.DEX, java.lang.Integer.valueOf(4)));
				statup.add(new Pair(MapleStat.LUK, java.lang.Integer.valueOf(4)));
				statup.add(new Pair(MapleStat.INT, java.lang.Integer.valueOf(4)));
				statup.add(new Pair(MapleStat.AVAILABLEAP, java.lang.Integer.valueOf(p.getRemainingAp())));
				p.getClient().getSession().write (MaplePacketCreator.updatePlayerStats(statup));
				*/
				cm.warp(40000);
				cm.dispose();
			} else if (status == 4) {
				cm.warp(100000000);
				cm.dispose();
			}
		}
	} else {
		if (mode == -1) {
			cm.dispose();
		} else {
			if (mode == 1)
				status++;
			else
				status--;
			if (status == 0) {
				cm.sendNext("Este e o lugar aonde seu primeiro programa de treinamento comeca. Nesta sala, voce vai ter que olhar uma classe de sua escolha.");
			} else if (status == 1) {
				cm.sendPrev("Depois de treinar duro o suficiente, voce tera direito a ocupar uma classe. Voce pode se tornar um Arqueiro em Henesys, um Mago em Ellinia, um Guerreiro em Perion, e um Gatuno em Kerning City ...");
			} else if (status == 2) {
				cm.dispose();
			}
		}
	}
}

