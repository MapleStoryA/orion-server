var fullLine = "______________________________________________";
var ct = "\t\t\t\t\t\t\t\t";
var randomColor;
var randomColorGenerator = Math.floor(Math.random()*4);
var itemid = [3010000/*0*/, 3010001/*1*/, 3010004/*2*/, 3010002/*3*/, 3010003/*4*/, 3010005/*5*/, 3010006/*6*/, 3010007/*7*/, 3010008/*8*/, 3010010/*9*/, 3010011/*10*/, 3010012, 3010013, 3010014, 3010015, 3010016, 3010017, 3010018, 3010019, 3010025, 3010040, 3010041, 3010043, 3010045, 3010046, 3010047, 3010057, 3010058, 3010060, 3010061, 3010062, 3010064, 3010069, 3010071, 3010072, 3010073, 3010080, 3010081/*red bunny chair*/, 3010082/*blue bunny chair*/, 3010083/*peasan bunny chair*/, 3010084/*sax bunny chair*/, 3010085];
var price;
var chairPrice = 5000000;

function start() {
    var text = "#eHello, i am the chair seller for #rMapleStory#k.\r\nhich chair would you like to buy?" + "\r\n\r\n#k"+fullLine+"\r\n\r\n"+ct+"\t\t    Tier free. \r\n"+ct+"0 #Mesos#k per chair.\r\n"+fullLine+"\r\n";
    for (i = 0; i < itemid.length; i++) {
        randomColorGenerator++;
                if (randomColorGenerator == 1) {
                    randomColor = "#r";
                } else if (randomColorGenerator == 2) {
                    randomColor = "#b";
                } else if (randomColorGenerator == 3) {
                    randomColor = "#d";
                } else if (randomColorGenerator == 4) {
                    randomColor = "#g";
                    randomColorGenerator = 0;
                }
                var itemname = "#t"+itemid[i]+"#";
                if (i == 37) {
                    itemname = "Red Bunny Chair";
                } else if (i == 38) {
                    itemname = "Blue Bunny Chair";
                } else if (i == 39) {
                    itemname = "Peasant/Slave Bunny Chair";
                } else if (i == 40) {
                    itemname = "Trumpet Bunny Chair";
                }
                text += "\r\n#v"+itemid[i]+"# #L"+i+"#"+randomColor+""+itemname+"#l\r\n";
            if (i == 2) {
                text += "\r\n\r\n#k"+fullLine+"\r\n\r\n"+ct+"\t\t    Tier 1. \r\n"+ (ct + 5 *  chairPrice) +"#rmesos# per chair.\r\n"+fullLine+"\r\n";
            } else if (i == 9) {
                text += "\r\n\r\n#k"+fullLine+"\r\n\r\n"+ct+"\t\t    Tier 2. \r\n"+ (ct + 10 *  chairPrice) +" #rmesos#k per chair.\r\n"+fullLine+"\r\n";
            } else if (i == 18) {
                text += "\r\n\r\n#k"+fullLine+"\r\n\r\n"+ct+"\t\t    Tier 3. \r\n"+ (ct + 20 *  chairPrice) +" #rmesos#k per chair.\r\n"+fullLine+"\r\n";        
            } else if (i == 23) {
                text += "\r\n\r\n#k"+fullLine+"\r\n\r\n"+ct+"\t\t    Tier 4. \r\n" + (ct+ 40 *  chairPrice) +" #rmesos#k per chair.\r\n"+fullLine+"\r\n";
        }
    }
    cm.sendSimple(text)
}

function action(mode, type, selection) {
            if (selection <= 2) {
                price = 0;
            } else if (selection >= 3 && selection <= 9) {
                price = 5 * chairPrice;
            } else if (selection >= 10 && selection <= 18) {
                price = 10 * chairPrice;
            } else if (selection >= 19 && selection <= 23) {
                price = 20 * chairPrice;
            } else if (selection >= 24 && selection <= 41) {
                price = 40 * chairPrice;
               
            }
        if (mode == 1) {
        	
            if (cm.getPlayer().getMeso() > price) {
            	var selected = itemid[selection];
            	if(cm.getPlayer().haveItem(selected) && price == 0){
            		cm.sendOk("You already have this chair.");
            		cm.dispose();
            		return;
            	}
            	cm.getPlayer().gainMeso(-price, false);
                cm.gainItem(itemid[selection]);
                cm.sendOk("#eYou have gained your selected item. #k\r\n\r\n- #v"+selected+"#  #t"+selected+"#");
                cm.dispose();
                } else {
                cm.sendOk("#eYou do not have enough mesos to do this!~~~");
            }
        } else {
            cm.sendOk("#eCome back later if you'd like to purchase any chair!");
            cm.dispose();
    }
}