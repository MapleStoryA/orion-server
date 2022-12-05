/* Author ~ johnlth93, xb0ib0ix3 and vincent - Zairean Dev 
*  
* X'mas Event 
* 
* 9201030.js: Happy Village - X'mas Present Box Item Exchanger.... Fixed a serious error in selection 4,5 (can get item without X'mas Present Box) 
*/ 
function start() {    
        cm.sendSimple("Hello,Would you like to exchange #v4000423# for my Santa Equipments? #k\r\n#L0# #v4000423# for a #v1002225# #k\r\n#L1##v4000423# for a #v1012007##k\r\n#L2##v4000423# for a #v1082101##k\r\n#L3##v4000423# for a #v1051049# #k\r\n#L4##v4000423# for a #v1070005##k\r\n#L5##v4000423# for a #v1071016##k\r\n#L6##v4000423# for a #v1050119# #k\r\n#L7##v4000423# for a #v1050019# #k\r\n#L8##v4000423# for a #v1051131# #k\r\n#L9##v4000423# for a #v1702166# #k\r\n#L10##v4000423# for a #v1702100# #k\r\n#L11##v4000423# for a #v1702008# #k\r\n#L12##v4000423# for a #v1002479# #k\r\n#L13##v4000423# for a #v1052046#"); 
    } 
function action(mode, type, selection) { 
    cm.dispose(); 
        if (selection == 0) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1002225,1); 
            cm.sendOk("Happy Merry Christmas with the #v1002225#"); 
        } else if (selection == 1) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1012007,1); 
            cm.sendOk("Happy Merry Christmas with the #v1012007#"); 
        } else if (selection == 2) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1082101,1); 
            cm.sendOk("Happy Merry Christmas with the #v1082101#"); 
        } else if (selection == 3) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1051049,1); 
            cm.sendOk("Happy Merry Christmas with the #v1051049#"); 
        } else if (selection == 4) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1070005,1); 
            cm.sendOk("Happy Merry Christmas with the #v1070005#"); 
        } else if (selection == 5) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1071016,1); 
            cm.sendOk("Happy Merry Christmas with the #v1071016#"); 
        } else if (selection == 6) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1050119,1); 
            cm.sendOk("Happy Merry Christmas with the #v1050119#"); 
        } else if (selection == 7) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1050019,1); 
            cm.sendOk("Happy Merry Christmas with the #v1050019#"); 
        } else if (selection == 8) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1051131,1); 
            cm.sendOk("Happy Merry Christmas with the #v1051131#"); 
        } else if (selection == 9) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1702166,1); 
            cm.sendOk("Happy Merry Christmas with the #v1702166#"); 
            cm.dispose(); 
        } else if (selection == 10) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1702100,1); 
            cm.sendOk("Happy Merry Christmas with the #v1702100#"); 
            cm.dispose(); 
        } else if (selection == 11) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1702008,1); 
            cm.sendOk("Happy Merry Christmas with the #v1702008#"); 
            cm.dispose(); 
        } else if (selection == 12) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1002479,1); 
            cm.sendOk("Happy Merry Christmas with the #v1002479#"); 
            cm.dispose(); 
        } else if (selection == 13) { 
            cm.haveItem(4000423,1); 
            cm.gainItem(4000423,-1); 
            cm.gainItem(1052046,1); 
            cm.sendOk("Happy Merry Christmas with the #v1052046#"); 
            cm.dispose(); 
        } 
    }