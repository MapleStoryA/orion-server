import './global';
let field = target.field();
let qr = target.questRecord();
let val = qr.get(7500);
let cJob = target.nJob();
//
// moving the warrior
if (field.id() == 105070001) {
    if (val == "p1" && (cJob == 110
        ||
        cJob == 120
        ||
        cJob == 130
    )) {
        let setParty = target.FieldSet("ThirdJob1");
        let res = setParty.enter(target.getCharacterID(), 0);
        if (res != 0) self.say("Já existe alguém lutando com o clone do #b#p1022000##k. Volte mais tarde.");
    } else
        self.say("Parece que existe uma porta que me levará a outra dimensão, mas não consigo entrar por algum motivo.");
}
// // moving the magician
// else if (field.id() == 100040106) {
//     if (val == "p1" && (cJob == 210
//         ||
//         cJob == 220
//         ||
//         cJob == 230
//     )) {
//         let setParty = target.FieldSet("ThirdJob2");
//         let res = setParty.enter(target.getCharacterID(), 0);
//         if (res != 0) self.say("Já existe alguém lutando com o clone de #b#p1032001##k. Volte mais tarde.");
//     } else
//         self.say("Parece que existe uma porta que me levará a outra dimensão, mas não consigo entrar por algum motivo.");
// }
// // moving the bowman
// else if (field.id() == 105040305) {
//     if (val == "p1" && (cJob == 310
//         ||
//         cJob == 320
//     )) {
//         let setParty = target.FieldSet("ThirdJob3");
//         let res = setParty.enter(target.getCharacterID(), 0);
//         if (res != 0) self.say("Já existe alguém lutando com o clone de #b#p1012100##k. Volte mais tarde.");
//     } else
//         self.say("Parece que existe uma porta que me levará a outra dimensão, mas não consigo entrar por algum motivo.");
// } else if (field.id() == 107000402) {
//     if (val == "p1" && (cJob == 410
//         ||
//         cJob == 420
//     )) {
//         let setParty = target.FieldSet("ThirdJob4");
//         let res = setParty.enter(target.getCharacterID(), 0);
//         if (res != 0) self.say("Já existe alguém lutando com o clone do #b#p1052001##k. Volte mais tarde.");
//     } else
//         self.say("Parece que existe uma porta que me levará a outra dimensão, mas não consigo entrar por algum motivo.");
// } else self.say("Parece que existe uma porta que me levará a outra dimensão, mas não consigo entrar por algum motivo.");