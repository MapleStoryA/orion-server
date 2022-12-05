/**
 *  	
 * Cassandra: Event manager
 *
 **/

var rewards = {};

var status = -1;
var title = "Hello #h0#, what's going on? I'm Cassandra, the head of Maple Story game events.\r\r\n";
var menuOptions = "#b#L0#Check in for today and check my points.#l\r\n#L1#I want to know about the event.#l#k\r\n#b#L2#I want to receive my reward.#l#k";
var numberOfDays = 14;
var DAILY_ATTENDANCE = "att2017";
var DAILY_ATTENDANCE_LAST_LOGIN = "att2017_login";
var DAILY_ATT_LAST_PRIZE = "att2017_last";


function start() {
	status = -1;
	action(1, 0, 0);
	rewards["1"]  = { id: 2000004, amount: 15, type: "item" };
	rewards["2"]  = { id: 4031123, amount: 7, type: "item" };
	rewards["3"]  = { id: 5530038, amount: 1, type: "item" };
	rewards["4"]  = { id: 2049401, amount: 1, type: "item" };
	rewards["5"]  = { id: 2049300, amount: 1, type: "item" };
	rewards["6"]  = { id: 5220000, amount: 1, type: "item" };
	rewards["7"]  = { id: 2049100, amount: 1, type: "item" };
	rewards["8"]  = { id: 5220000, amount: 1, type: "item" };
	rewards["9"]  = { id: 5220000, amount: 1, type: "item" };
	rewards["10"] = { id: 2049400, amount: 1, type: "item" };
	rewards["11"] = { id: 2022179, amount: 1, type: "item" };
	rewards["12"] = { id: 2049300, amount: 1, type: "item" };
	rewards["13"] = { id: 3010073, amount: 1, type: "item" };
	rewards["14"] = { id: 5200010, amount: 1, type: "item" };
}

function formatToday(){
	var year = new Date().getFullYear();
	var month = new Date().getMonth() + 1;
	var day = new Date().getDate();
	return day + "/" + month + "/" + year;
}

function formatYesterday(){
	var date = new Date();
	date.setDate(date.getDate() - 1);
	var year = date.getFullYear();
	var month = date.getMonth() + 1;
	var day = date.getDate();
	return day + "/" + month + "/" + year;
}

function action(mode, type, selection) {
	cm.debug("Mode: " + mode + " Type: " + type + " Selection: " + selection);
	var formattedToday = formatToday();
	var formattedYesterday = formatYesterday();
	if (mode == 1 && selection == -1) {
		cm.sendOk("Come back at any time.");
		cm.dispose();
		return;
	}
	if(status == -1){
		cm.sendSimple(title + menuOptions);
		status = 0;
		return;
	}
	if(status == 0 && selection == 1){
		var menu = "If you log everyday during the #bMaple Attendance#k event with a character level 45 or higher, you can get daily surprises: ";
		for(var i = 1; i <= 14; i++){
	    	var reward = rewards[i];
	    	menu += "\r\r";
	    	menu += "#bDay " + i + "#k: " + reward.amount + " x ";
	    	menu += "a free #t" + reward.id + "# ";
	    	menu += "#i" + reward.id + "#";
	    }
		menu += "\r"
		cm.sendPrev(menu);
		status = -1;
		return;
	}
	if(status == 0 && selection == 0){
		if(cm.getPlayer().getLevel() < 45){
			cm.sendNext("A minimum level of 45 is required to participate of this event.");
			cm.dispose();
			return;
		}
		var dailyCount = cm.getPlayer().get(DAILY_ATTENDANCE);
		var lastLogin = cm.getPlayer().get(DAILY_ATTENDANCE_LAST_LOGIN);
		var menu = "Check-in for today was succesfully, ";
		if(dailyCount == null){
			cm.getPlayer().set(DAILY_ATTENDANCE, "1");
			cm.getPlayer().set(DAILY_ATTENDANCE_LAST_LOGIN, formattedToday);
			dailyCount = 1;
		}else{
			if(formattedToday.equals(lastLogin)){
				menu = "You already did a check-in today, ";
			}else if(formattedYesterday.equals(lastLogin)){
				dailyCount = Number(dailyCount) + 1;
				cm.getPlayer().set(DAILY_ATTENDANCE, dailyCount);
				cm.getPlayer().set(DAILY_ATTENDANCE_LAST_LOGIN, formattedToday);
			}else {
				cm.sendNext("You haven't logged in for a while, so the counter will be resetted.");
				cm.getPlayer().set(DAILY_ATTENDANCE, 1);
				cm.getPlayer().set(DAILY_ATTENDANCE_LAST_LOGIN, formattedToday);
			}
		}
		menu +=  " and here are your current status:";
		
		var calendar = Number(3800015) + Number(dailyCount);
		menu += "#r" + dailyCount + "#k" + " of " + numberOfDays + " days\r\r";
		menu += "#i" + calendar + "#";
		cm.sendPrev(menu);
		status = -1;
		return;
	}
	if(status == 0 && selection == 2){
		var menu = "Here's your daily prize for being a great player!\r\r#Wprob#\r\r";
		var lastCount = cm.getPlayer().get(DAILY_ATTENDANCE);
		var lastPrize = cm.getPlayer().get(DAILY_ATT_LAST_PRIZE);
		if(formattedToday.equals(lastPrize)){
			cm.sendOk("You already received your prize, come back tomorrow!");
			cm.dispose();
			return;
		}
		if(lastCount == null){
			cm.sendOk("Do a check-in to receive your daily gift!");
			cm.dispose();
			return;
		}
		if(lastCount > 0){
			var reward = rewards[lastCount];
			menu += "\r\r";
			switch(reward.type){
				case "item":
					menu += "#t" + reward.id + "# : ";
					menu += "#i" + reward.id + "#\r\r";
					cm.gainItem(reward.id, reward.amount);
			}
		}
		cm.getPlayer().set(DAILY_ATT_LAST_PRIZE, formattedToday);
		cm.sendOk(menu);
	}
	cm.dispose();

}