var mapid = [105040100, 105040200, 800010100];

function start() {
cm.sendSimple("#eHello mortal. My name is Rene. I am in charge of poor little being like you seeking for power. I might just know what you're looking for. I have modified the biodiversity of certain map to spawn powerful monster to help you level and up train yourself. Where you would like to go? \r\n\r\n#r#L0#Skelegons Map 1 (Less than 20 rebirths only)#l\r\n#L1#Skelegons Map 2 (Less than 50 Rebirths only)#l\r\n#L2#Training map 3 (recommended Rebirth: 25+)#l");
}
function action(mode, type, selection) {
        if (mode == 1) {
            cm.warp(mapid[selection]);
            cm.dispose();
    }
  cm.dispose();
}