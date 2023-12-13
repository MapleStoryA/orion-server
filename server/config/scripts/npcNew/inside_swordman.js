if (target.nJob() == 100 &&
    target.nLevel() >= 30
) {
    if (inventory.itemCount(4031013) >= 30) {
        self.say("Ahhhhh... voc� juntou 30 itens do tipo #t4031013#!! Deve ter sido dif�cil... que incr�vel! Muito bem. Voc� passou no teste. Vou recompens�-lo com a #b#t4031012##k. Leve isto com voc� e volte a #m102000000#.");
        nBlack = inventory.itemCount(4031013);
        ret = inventory.exchange(0, 4031013, -nBlack, 4031008, -1, 4031012, 1);
        if (ret == 0) self.say("Algo est� errado... verifique se voc� tem 30 itens do tipo #t4031013#, a carta do #b#p1022000##k e um slot vazio no seu invent�rio de Etc.");
        else target.registerTransferField(102020300, "");
    } else {
        nRet = self.askYesNo("O que est� acontecendo? Acho que voc� n�o juntou 30 itens do tipo #b#t4031013##k ainda... Se estiver tendo problemas com isso, voc� pode sair agora e tentar novamente mais tarde. Ent�o... quer desistir e dar o fora daqui?");
        if (nRet == 0) self.say("Isso a�! Pare de reclamar e comece a juntar as bolinhas. Venha falar comigo quando tiver juntado 30 itens do tipo #b#t4031013##k.");
        else if (nRet == 1) {
            self.say("Mesmo...? Certo, vou deixar voc� sair. Mas n�o desista. Voc� sempre poder� tentar novamente, ent�o n�o desista. At� l�, adeus...");
            target.registerTransferField(102020300, "");
        }
    }
} else {
    self.say("O qu�? Como voc� chegou aqui?... que estranho... bom, vou deixar voc� sair. Este � um lugar muito perigoso. V� embora ou correr� mais riscos.");
    target.registerTransferField(102020300, "");
}
