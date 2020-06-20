var status = -1;
var gachapon_item = 5220000;
var remote_item = 5451000;

var limited = [
5200010,
5200009
];

function gachaEntryPoint(mode, type, selection) {
	cm.debug('Script: ' + scriptName() + ' mode:' + mode + " type: " + type + " selection: " +selection + " status: " + status);
	if(selection === -1 && status === 0 && mode === 0){
		cm.dispose();
		return;
	}
	if (mode == 1) {
		status++;
    } else {
    	status--;
    }
    if (status == 0) {
		if (cm.haveItem(gachapon_item) || cm.haveItem(remote_item)) {
			var menu = "#eLimited Time Sale!#n\r\r";
			for(var i = 0; i < limited.length;i++){
				menu += "#i" + limited[i]  + " ##t" + limited[i] + "# \r\r";
			}
			menu += "#rOnly 15 days(s) left Hurry!#k\r\r\r\r";
			menu += "You may use the " + cm.getMap().getMapName() + " Gachapon. Which service would you like to use?\r\r\n";
		    menu += "#b#L0#Use Gachapon Ticket#l\r\r";
		    menu += "#L1#Use a Gachapon Food Coupon#l#k\r";
		    cm.sendSimple(menu);
		} else {
		    cm.sendOk("You don't have a single ticket with you. Please buy the ticket at the department store before coming back to me. Thank you.");
		    cm.safeDispose();
		}
    } else if (status == 1 && selection == 0) {
		var item = cm.gainGachaponItem(scriptName(), cm.haveItem(remote_item));
		var name = cm.getMap().getMapName();
		if (item != -1) {
			cm.sendOk("You have obtained #b#t" + item + "##k from " + name + " Gachapon.\r\nThank you for using our Gachapon services. Please come again!");
		} else {
		    cm.sendOk("Please check your item inventory and see if you have the ticket, or if the inventory is full.");
		}
		cm.safeDispose();
		
    } else if(status == 1 && selection == 1){
    	cm.sendOk(cm.getMap().getMapName() + " Gachapon food is out of service at moment.");
    	cm.safeDispose();
    }
}