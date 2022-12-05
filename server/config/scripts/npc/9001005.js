/**
 @author David [Jvlaple]
**/

importPackage(Packages.client);

var status = 0;
var inventory = -1;
var item = null;
var quantity = -1;
var s = -1;
var retrieve = -1;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 2 && mode == 0) {
			cm.sendOk("Alright, see you next time.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 1) s = selection;
		if (status == 0) {
			cm.sendSimple("Hi, I work for the #bmarket#k. What would you like to do today?\r\n#L0#Sell Items#l\r\n#L1#Buy Items#l\r\n#L2#Retrieve Items#l\r\n#L3#Nothing.#l\r\n");
		} else{
			if (s == 0) { //Sell items
				if (status == 1) {
					cm.sendSimple("You want to sell your items? Choose an inventory first:\r\n#L0#Equip#l\r\n#L1#Use#l\r\n#L2#Set up#l\r\n#L3#Etc#l\r\n#L4#Cash#l\r\n");
				} else if (status == 2) {
					var inv = selection + 1;
					inventory = inv;
					var send = cm.getInventory(inv);
					if (!send.equals(""))
						cm.sendSimple("Here is your inventory:\r\n" + send + "\r\nPlease pick an item to sell.");
					else {
						cm.sendOk("You have no items in that inventory!");
						cm.dispose();
					}
				} else if (status == 3) {
					item = cm.getItem(selection, inventory);
					cm.sendGetNumber("You are about to sell: \r\n#v" + item.getItemId() + "#\r\nPlease give the quantity you would like to sell:", 
						item.getQuantity(), 1, 5000);
				} else if (status == 4) {
					quantity = selection;
					if (quantity > item.getQuantity()) {
						cm.sendOk("You're trying to sell more than you have. Sorry, but no can do.");
						cm.dispose();
					} else {
						cm.sendGetNumber("You are about to sell: \r\n#v" + item.getItemId() + "#\r\nPlease give the price you would like to sell it for:", 
							100, 1, 50000000);
					}
				} else if (status == 5) {
					cm.addItemToMarket(item.getItemId(), quantity, selection);
					cm.sendOk("Your item, #v" + item.getItemId() + "# was #bsuccessfully#k added to the market with " + quantity + " quantity and " + selection + " mesos.");
					cm.gainItem(item.getItemId(), -quantity);
					cm.dispose();
				}
			} else if (s == 1) { //Buy items
				if (status == 1) {
					var m = cm.getMarket();
					if (!m.equals(""))
						cm.sendSimple("Heres the items:\r\n" + m);
					else {
						cm.sendOk("There are currently no items on the market.");
						cm.dispose();
					}
				} else if (status == 2) {
					item = cm.getMarketItems().get(selection);
					cm.sendGetNumber("How many of: \r\n#v" + item.getId() + "#\r\nWill you buy:", 
						item.getQuantity(), 1, 50000000);
				} else if (status == 3) {
					quantity = selection;
					cm.sendYesNo("Are you sure you wish to buy " + selection + " #v" + item.getId() + "# for " + item.getPrice() + " mesos?");
				} else if (status == 4) {
					cm.buyItem(item.getId(), quantity, item.getPrice(), item.getOwner());
					cm.dispose();
				}
			} else if (s == 2) { //Retrieve items
				if (status == 1) {
					var r = cm.getMarketRetrival();
					if (!r.equals(""))
						cm.sendSimple("Here are your items:\r\n" + r);
					else {
						cm.sendOk("You currently have no items in the market.");
						cm.dispose();
					}
				} else if (status == 2) {
					cm.sendYesNo("Are you sure you wish to retrieve #v" + cm.getMyMarketItems().get(selection).getId() + "#?");
					retrieve = selection;
				} else if (status == 3) {
					cm.retrieveMarketItem(retrieve);
					cm.sendOk("You have successfully retrieved the item.");
					cm.dispose();
				}
			} else if (selection == 3) { //Nothing
				cm.sendOk("Really? If you do want to access the market, don't hesitate to talk to me.");
				cm.dispose();
			}
			else {
				cm.dispose();
			}
		}
	}
}