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
    cm.sendSimple("#e#dWelcome to the donator island. In each of those house, there are NPC's in each of those house for prize and donor point exchange. Which NPC would you like to know about? \r\n#k #L0#The house above.#l\r\n#L1#The house to the right#l");
    } else if (status == 1) {
        if (selection == 0) {
            cm.sendOk("#e#bNPC 1 - ");
            cm.dispose();
        } else if (selection == 1) {
            cm.sendOk("#e#bNPC 1 - ");
            cm.dispose();
            }
        }
    }
}