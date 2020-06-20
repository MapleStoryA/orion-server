/**
  Questions.
-- Original Author --------------------------------------------------------------------------------
	Jvlaple
-- Modified by -----------------------------------------------------------------------------------
	XoticMS.
---------------------------------------------------------------------------------------------------
**/
importPackage(Packages.client);
importPackage(Packages.server);
var status;
var otherChar;
 
function start() {
    otherChar = cm.getSender();
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else {
        if (type == 1 && mode == 0)
            otherChar.dropMessage(1, "O seu parceiro/a rejeitou seu pedido.");
        else
            otherChar.dropMessage(1, "O seu parceiro/a fechou a janela de chat.");
        cm.dispose();
        return;
    }
	
    if (status == 0) {
        cm.sendNext("Alguem do mundo MapleStory te enviar uma mensagem.");
    } else if (status == 1) {
        cm.sendYesNo("Voce gostaria de se casar com " + otherChar.getName() + "?") ;
    } else if (status == 2) {
        if (cm.createEngagement(otherChar.getName())) {
            otherChar.dropMessage(1, "Seu parceiro/a aceitou seu pedido, parabens!");
            otherChar.setMarriageQuestLevel(50);
            cm.getPlayer().setMarriageQuestLevel(50);
            if (otherChar.getItemQuantity(2240000, false) > 0) {
                MapleInventoryManipulator.removeById(otherChar.getClient(), MapleInventoryType.USE, 2240000, 1, false, false);
                MapleInventoryManipulator.addById(otherChar.getClient(), 4031358, 1, "slut!");
                MapleInventoryManipulator.addById(otherChar.getClient(), 4031357, 1, "cunt!");
                cm.gainItem(4031358, 1);
            } else if (otherChar.getItemQuantity(2240001, false) > 0) {
                MapleInventoryManipulator.removeById(otherChar.getClient(), MapleInventoryType.USE, 2240001, 1, false, false);
                MapleInventoryManipulator.addById(otherChar.getClient(), 4031360, 1, "shit!");
                MapleInventoryManipulator.addById(otherChar.getClient(), 4031359, 1, "shit!");
                cm.gainItem(4031360, 1);
            } else if (otherChar.getItemQuantity(2240002, false) > 0) {
                MapleInventoryManipulator.removeById(otherChar.getClient(), MapleInventoryType.USE, 2240002, 1, false, false);
                MapleInventoryManipulator.addById(otherChar.getClient(), 4031362, 1, "shit!");
                MapleInventoryManipulator.addById(otherChar.getClient(), 4031361, 1, "shit!");
                cm.gainItem(4031362, 1);
            } else if (otherChar.getItemQuantity(2240003, false) > 0) {
                MapleInventoryManipulator.removeById(otherChar.getClient(), MapleInventoryType.USE, 2240003, 1, false, false);
                MapleInventoryManipulator.addById(otherChar.getClient(), 4031364, 1, "shit!");
                MapleInventoryManipulator.addById(otherChar.getClient(), 4031363, 1, "shit!");
                cm.gainItem(4031364, 1);
            }
        } else {
            cm.sendOk("Aconteceu um erro, tente novamente mais tarde.");
            otherChar.dropMessage(1, "Aconteceu um erro, tente novamente mais tarde");
        }
        cm.dispose();
    }
}