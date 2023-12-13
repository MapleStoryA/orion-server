field = self.field;
qr = target.questRecord();
val = qr.get(7500);
cJob = target.nJob();

// moving the warrior
if (field.id == 105070001) {
    if (val == "p1" && (cJob == 110
        ||
        cJob == 120
        ||
        cJob == 130
    )) {
        setParty = FieldSet("ThirdJob1");
        res = setParty.enter(target.nCharacterID, 0);
        if (res != 0) self.say("Já existe alguém lutando com o clone do #b#p1022000##k. Volte mais tarde.");
    } else
        self.say("Parece que existe uma porta que me levará a outra dimensão, mas não consigo entrar por algum motivo.");
}
// moving the magician
else if (field.id == 100040106) {
    if (val == "p1" && (cJob == 210
        ||
        cJob == 220
        ||
        cJob == 230
    )) {
        setParty = FieldSet("ThirdJob2");
        res = setParty.enter(target.nCharacterID(), 0);
        if (res != 0) self.say("Já existe alguém lutando com o clone de #b#p1032001##k. Volte mais tarde.");
    } else
        self.say("Parece que existe uma porta que me levará a outra dimensão, mas não consigo entrar por algum motivo.");
}
// moving the bowman
else if (field.id == 105040305) {
    if (val == "p1" && (cJob == 310
        ||
        cJob == 320
    )) {
        setParty = FieldSet("ThirdJob3");
        res = setParty.enter(target.nCharacterID, 0);
        if (res != 0) self.say("Já existe alguém lutando com o clone de #b#p1012100##k. Volte mais tarde.");
    } else
        self.say("Parece que existe uma porta que me levará a outra dimensão, mas não consigo entrar por algum motivo.");
} else if (field.id == 107000402) {
    if (val == "p1" && (cJob == 410
        ||
        cJob == 420
    )) {
        setParty = FieldSet("ThirdJob4");
        res = setParty.enter(target.nCharacterID(), 0);
        if (res != 0) self.say("Já existe alguém lutando com o clone do #b#p1052001##k. Volte mais tarde.");
    } else
        self.say("Parece que existe uma porta que me levará a outra dimensão, mas não consigo entrar por algum motivo.");
} else self.say("Parece que existe uma porta que me levará a outra dimensão, mas não consigo entrar por algum motivo.");
