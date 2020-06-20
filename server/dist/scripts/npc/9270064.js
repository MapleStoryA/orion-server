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
    cm.sendSimple("#e#bHello "+cm.getName()+". Wished you were as sexy as me? There is only one way for that, donation that is. Would you like to know more about something related to donation? \r\n\r\n#L0#Tell me about the donation rewards.#l\r\n#L1#Warp me to the donator island!#l");
    } else if (status == 1) {
        if (selection == 0) {
            cm.sendOk("#eFor every #r$#k that you donate, you will get a donor point. You can spend them on the donator island. ");/*\r\n\r\n #r5$#b - #k5 GM scroll of choice\r\n#r10$#b - #k7 GM scroll of choice + Free Name Change\r\n#e#r15$#b - #kSame as above + 1 Fixed MSI\r\n#e#r20$#b - #kSame thing as above + 2 Free Fixed MSI'S\r\n#e#r25$#b - #kSame thing as above + a full set of fixed MSI\r\n\r\n#rCumulative#k\r\n\r\n #e#r75$#b - #kDonator Status with donor commands\r\n#e#r150$#b - #kIntern GM");*/
            cm.dispose();
        } else if (selection == 1) {
            cm.warp(1000000);
            cm.dispose();
			}
		}
	}
}