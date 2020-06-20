function start() {
    if (cm.getPlayer().getMapId() == 970010000) {
        cm.sendYesNo("Would you like to go back to #bHenesys#k now?");
    } else {
        cm.sendYesNo("Hey, would you like to be warped to the maple hill and donate some suns to #bAramia#k?"); 
    }
}

function action(m,t,s) {
    if (m > 0) {
        if (cm.getPlayer().getMapId() == 970010000) {
            cm.warp(100000000)
            cm.sendOk("Thanks for helping the #rmaple tree#k growing!");
            cm.dispose();
        } else {
            cm.warp(970010000);
            cm.sendOk("Walk to your right to see #bAramia#k and the tree on top of the hill. Use the portal to the left to go back to henesys.");
            cm.dispose();
        }
    } else {
        if (cm.getPlayer().getMapId() == 970010000) {
            cm.sendOk("Alright, re-use the portal at anytime if you'd like to exit.");
            cm.dispose();
        } else {
            cm.sendOk("Alright come back at any time!");
            cm.dispose();
        }
    }
}