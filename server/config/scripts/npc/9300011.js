/*
	NPC Name: 		Cai Shen
	Map(s): 		Everywhere, towns
	Description: 		Introduction to Gachapon
*/

var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }
    if (status == 0) {
	cm.sendNext("The Gachapon machine is now ready. Each of the machine that are set up in different towns will give out different items.");
    } else if (status == 1) {
	cm.sendPrev("You may even acquire items that are hard to find. The Gachapon coupon is available in cash shop. Would you like to try your hands on it?");
    } else if (status == 2) {
	cm.dispose();
    }
}