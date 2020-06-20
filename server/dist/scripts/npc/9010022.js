/**
 * Dimensional Mirror
 * Warps you to Party Quests/Special Maps
 */
var text = "";

function start() {
    //text += "#0# Ariant Coliseum";
	if (cm.getPlayerStat("LVL") >= 25 && cm.getPlayerStat("LVL") <= 30) {
    	text += "#0# Ariant Coliseum";
    }
    if (cm.getPlayerStat("LVL") >= 25) {
        text += "#1# Mu Lung Training Center";
    }
    if (cm.getPlayerStat("LVL") >= 31 && cm.getPlayerStat("LVL") <= 50) {
        text += "#2# Monster Carnival 1";
    }
    if (cm.getPlayerStat("LVL") >= 51 && cm.getPlayerStat("LVL") <= 70) {
        text += "#3# Monster Carnival 2";
    }
    if (cm.getPlayerStat("LVL") >= 60 && cm.getPlayerStat("LVL") <= 80) {
        text += "#4# Dual Raid";
    }
    if (cm.getPlayerStat("LVL") >= 40) {
        text += "#5# Nett's Pyramid";
    }
    if (cm.getPlayerStat("LVL") >= 25 && cm.getPlayerStat("LVL") <= 30) {
        text += "#6# Kerning Subway";
    }
    text += "#7# Happyville";
    
    cm.askMapSelection(text);
}

function action(mode, type, selection) {
    if (mode == 1) {
        switch (selection) {
        case 0:
        	if (cm.getPlayerStat("LVL") >= 25) {
                cm.saveLocation("MULUNG_TC");
                cm.warp(980010000, 0);
            }
            break;
        case 1:
            if (cm.getPlayerStat("LVL") >= 25) {
                cm.saveLocation("MULUNG_TC");
                cm.warp(925020000, 0);
            }
            break;
        case 2:
            if (cm.getPlayerStat("LVL") >= 31 && cm.getPlayerStat("LVL") <= 50) {
                cm.saveLocation("MULUNG_TC");
                cm.warp(980000000, 4);
            }
            break;
        case 3:
            if (cm.getPlayerStat("LVL") >= 51 && cm.getPlayerStat("LVL") <= 70) {
                cm.saveLocation("MULUNG_TC");
                cm.warp(980030000, 4);
            }
            break;
        case 4:
            if (cm.getPlayerStat("LVL") >= 60 && cm.getPlayerStat("LVL") <= 80) {
                cm.saveLocation("MULUNG_TC");
                cm.warp(923020000, 0);
            }
            break;
        case 5:
            if (cm.getPlayerStat("LVL") >= 40) {
                cm.saveLocation("MULUNG_TC");
                cm.warp(926010000, 4);
            }
            break;
        case 6:
            if (cm.getPlayerStat("LVL") >= 25 && cm.getPlayerStat("LVL") <= 30) {
                cm.saveLocation("MULUNG_TC");
                cm.warp(910320000, 2);
            }
            break;
        case 7:
            cm.saveLocation("MULUNG_TC");
            cm.warp(209000000, 2);
            break;
        }
    }
    cm.dispose();
}