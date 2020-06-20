var status = -1;

function start(mode, type, selection) {
	status++;
	if(mode === 0){
		qm.sendNext("Come back when you are ready..");
		qm.dispose();
		return;
	}
	if(status === 0){
		qm.sendNext("How is training going? Hm, Lv 70? You still have a long way to go, " +
				"but it's definately priseworthy compared to the first time I met you. " +
				"Continue to focused, and I'm sure you'll regain your strength soon!");
		return;
	}
	if(status === 1){
		qm.sendAcceptDecline("But first, you must head to #bRien#k. Your #bGiant Polearm#k is acting weird again. " +
				"I think it has something to tell you. It might be able to restore your abilities, so please hurry")
		qm.forceCompleteQuest(21300);
		return;
	}
	if(status === 2){
		qm.sendOk("Anyway, I thought it was really something that a weapon had its own identity," +
				" but this weapon gets extremely annoying. " +
				"It cries, saying that I'm not paying attention to its needs, and now... Oh, please keep this a secret from the Polearm. I don't think it's a good idea to upset the weapon any more than I already have");
		return;
	}
	qm.dispose();
}

function end(mode, type, selection) {
	qm.sendNext("Please talk to Tylus of El Nath instead.");
	qm.dispose();
}