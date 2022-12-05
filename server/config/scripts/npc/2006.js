var itemid = [01702118, 01702119, 1702120, 01702008, 1702071, 01702149, 1000032, 1000030, 01002999, 01002998, 1003001, 1002225, 1000026, 1001036, 1002368, 1001028, 1002524, 1002824, 01041138, 1040137, 1040138, 01041139, 01048000, 01048000, 1060120, 1061141, 1061142, 1060121, 1062076, 1062044, 1060067, 1061068, 1061000, 01060119, 01051158, 1050138, 1052213, 1052210, 1052211, 1050019, 01050119, 1051131, 01052195, 1052031, 1052170, 1082224, 1082225, 1082101, 1072282, 1072283, 1072281, 1072406, 1072404, 1072253, 1071010, 1072277, 1072278, 1070001, 1071003, 1071016, 1070005, 1072328, 1072327, 1072333, 1072349, 1072341, 1072330, 1102097, 1102095, 1102096, 1102148, 1102149, 1102184, 1102152, 1102212, 1032034, 1010006, 1012044, 1012081, 1022079];
var fullLine = "______________________________________________";
var ct = "\t\t\t\t\t\t\t\t";
var randomColor;
var randomColorGenerator = Math.floor(Math.random()*4);
var coconut = 4000465;

function start() {
    var text = "#eHello, i am #rTienk#k, the rare nx seller! Every items cost 25 coconuts. Would you like to buy anything?\r\n" + "\r\n\r\n"+fullLine+"\r\n\r\n"+ct+"\t\t\t#rWeapons#k\r\n"+fullLine+"";
        for (i = 0; i < itemid.length; i++) {
            var itempicture = "#v"+itemid[i]+"#";
            text += "\r\n"+i+" #L"+i+"#"+itempicture+"\t#t"+itemid[i]+"##l";
                if (i == 5) {
                    text += "\r\n\r\n"+fullLine+"\r\n\r\n"+ct+"\t\t\t#rHats#k\r\n"+fullLine+"";
            } else if (i == 17) {
                text += "\r\n\r\n"+fullLine+"\r\n\r\n"+ct+"\t\t\t#rTops#k\r\n"+fullLine+"";
            } else if (i == 23) {            
                text += "\r\n\r\n"+fullLine+"\r\n\r\n"+ct+"\t\t\t#rBottoms#k\r\n"+fullLine+"";
            } else if (i == 33) {            
                text += "\r\n\r\n"+fullLine+"\r\n\r\n"+ct+"\t\t\t#rOveralls#k\r\n"+fullLine+"";
            } else if (i == 44) {            
                text += "\r\n\r\n"+fullLine+"\r\n\r\n"+ct+"\t\t\t#rGloves#k\r\n"+fullLine+"";
            } else if (i == 47) {            
                text += "\r\n\r\n"+fullLine+"\r\n\r\n"+ct+"\t\t\t#rShoes#k\r\n"+fullLine+"";
            } else if (i == 66) {            
                text += "\r\n\r\n"+fullLine+"\r\n\r\n"+ct+"\t\t\t#rCapes#k\r\n"+fullLine+"";
            } else if (i == 74) {            
                text += "\r\n\r\n"+fullLine+"\r\n\r\n"+ct+"\t\t\t#rEarrings#k\r\n"+fullLine+"";
            } else if (i == 75) {            
                text += "\r\n\r\n"+fullLine+"\r\n\r\n"+ct+"\t\t\t#rFace#k\r\n"+fullLine+"";
            } 
        }
    cm.sendSimple(text);
}

function action(mode, type, selection) {
    if(mode == 1) {
        if  (cm.haveItem(coconut, 25)) {
                cm.gainItem(coconut, -25);
                cm.gainItem(itemid[selection]);
                cm.sendOk("#eYou have gained your selected equipment\r\n\r\n#v"+itemid[selection]+"# - #t"+itemid[selection]+"#");
                cm.dispose();
            } else {
                cm.sendOk("#eYou do not have enough coconuts to aford this piece of equipment!");
                cm.dispose();
            }
        } else {
            cm.sendOk("#eCome back later if you'd like to purchase any item!");
            cm.dispose();
    }
}