var itemid = [4000561, 4000022, 4000101, 4000112, 4000434];
var price = [120, 50, 60, 40, 25];
var coconut = 4000465;

function start() {
    var text = "#eHello, if you are talking to this npc, you are mostly new to the server. Well in the first place, welcome. Now, since you are new, i will help you getting some easy coconuts as a first.\r\n\r\n You can exchange some of the etc that the mobs in this map drops for coconuts. What would you like to trade?\r\n";
    for (i = 0;i<itemid.length;i++) {
        text += "\r\n#L"+i+"##v"+itemid[i]+"# #b- Price : "+price[i]+"#l";
    }
    cm.sendSimple(text)
}

function action(m,t,s) {
    if (m > 0) {
       if (cm.haveItem(itemid[s], price[s])) {
            cm.gainItem(itemid[s], -price[s]);
            cm.gainItem(coconut);
            cm.sendOk("#eHere, have your coconut!\r\n\r\n#rYou have lost #v"+itemid[s]+"# x50\r\n#gYou have gained #v"+coconut+"# - x1");
            cm.dispose();
        } else {
            cm.sendOk("#eYou are missing some of the requirement!");
            cm.dispose();
        }
    } else {
        cm.sendOk("#eAlright, hope you enjoy your stay!");
        cm.dispose();
    }
}