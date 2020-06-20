function start() {
status = -1;
action(1, 0, 0);
}
function action(mode, type, selection) {
if (mode == -1) {
cm.dispose();
}
else {
if (mode == 0) {
cm.sendOk("#e#bHave fun in MapleStory!");
cm.dispose();
return;
}
if (mode == 1) {
status++;
}
else {
status--;
}
if (status == 0) {
        cm.sendGetText("#eYou currently have #b"+cm.getDonatorPoints()+"#k Donator Points!\r\n\r\n Hello, i am the #bDonator Point To Coconut Trader#k! The current rate is #b1:45#k . (1 Point for 45 Coconut)\r\n\r\n#rHow many vote points would you like to trade for coconut#k?");
    } else if (status == 1) {
        if (cm.getText() < 0) {
            cm.sendOk("#eNO FUCKING CHEATING");
            cm.dispose();
        } else if (cm.getText() == 0) {
            cm.sendOk("#eYou cannot input 0!");
            cm.dispose();
        } else {
            cm.sendYesNo("#eAre you sure you want to use #b"+cm.getText()+"#k donator points? You would get #b"+45 * cm.getText()+"#k coconuts.");
            }
        } else if (status == 2) {
            if (cm.getDonatorPoints() >= cm.getText()) {
                cm.gainItem(4000465, 45 * cm.getText());
                cm.takeDonatorPoints(cm.getText());
                cm.sendOk("#eYou have gained "+45 * cm.getText()+" coconuts. Thanks for supporting us by donating!");
                cm.dispose();
            }else{
                cm.sendOk("#eYou don't have enough Donator Points to do this!");
                cm.dispose();
            }
        }
    }
}