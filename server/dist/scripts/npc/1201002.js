var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

var npcSkills = [ {
	"name" : "Combo Ability",
	"value" : 21000000,
	"job" : 2100,
	"requirements" : [ {
		"item" : 4000004,
		"qty" : 100,
		"desc" : "Squish Liquid "
	}, {
		"item" : 4000000,
		"qty" : 50,
		"desc" : "Snail Shell"
	} ]
}, {
	"name" : "Polearm Booster",
	"value" : 21001003,
	"job" : 2100,
	"requirements" : [ {
		"item" : 4000004,
		"qty" : 150,
		"desc" : "Squish Liquid "
	}, {
		"item" : 4000000,
		"qty" : 25,
		"desc" : "Snail Shell"
	} ]
},
{
	"name" : "Polearm Mastery",
	"value" : 21100000,
	"job" : 2110,
	"requirements" : [ {
		"item" : 4000106,
		"qty" : 100,
		"desc" : "Squish Liquid "
	}, {
		"item" : 4000037,
		"qty" : 45,
		"desc" : "Bublings Huge "
	} ]
},
{
	"name" : "Final Charge",
	"value" : 21100002,
	"job" : 2110,
	"requirements" : [ {
		"item" : 4000106,
		"qty" : 100,
		"desc" : "Squish Liquid "
	}, {
		"item" : 4000037,
		"qty" : 45,
		"desc" : "Bublings Huge "
	} ]
},
{
	"name" : "Combo Drain",
	"value" : 21100005,
	"job" : 2110,
	"requirements" : [ {
		"item" : 4000106,
		"qty" : 100,
		"desc" : "Squish Liquid "
	}, {
		"item" : 4000037,
		"qty" : 45,
		"desc" : "Bublings Huge "
	} ]
},
{
	"name" : "Combo Smash",
	"value" : 21100004,
	"job" : 2110,
	"requirements" : [ {
		"item" : 4000106,
		"qty" : 100,
		"desc" : "Squish Liquid "
	}, {
		"item" : 4000037,
		"qty" : 45,
		"desc" : "Bublings Huge "
	} ]
},
{
	"name" : "Full Swing",
	"value" : 21110002,
	"job" : 2111,
	"requirements" : [ {
		"item" : 4000106,
		"qty" : 100,
		"desc" : "Squish Liquid "
	}, {
		"item" : 4000037,
		"qty" : 45,
		"desc" : "Bublings Huge "
	} ]
}];

var selectedSkill = 0;
var availableSkills;

function getElementsForJob(job) {
	var skills = [];
	for (var i = 0; i < npcSkills.length; i++) {
		if(npcSkills[i].job === job){
			skills.push(npcSkills[i]);
		}
	}
	return skills;
}

function action(mode, type, selection) {
	availableSkills = getElementsForJob(cm.getPlayer().getJob());
	if (status === -1) {
		cm.sendYesNo("#bSo you are looking for new skills?#k");
		status = 1;
		return;
	}
	// cm.debug(status + " " + mode + " " + type + " " + selection);
	if (status >= 1 && mode === 0 || (availableSkills.lentgth === 0)) {
		cm.sendOk("#bCome back later.#k");
		cm.dispose();
		return;
	}
	
	if (status === 1) {
		var message = "";
		for (var i = 0; i < availableSkills.length; i++) {
			message += "\r\n#b#L" + i + "#" + availableSkills[i].name + "#l"
		}
		cm.sendSimple("Choose your skill: " + message)
		status = 2;
		return;
	}
	if (status === 2) {
		cm.sendYesNo("You have choosen " + availableSkills[selection].name + ". Are you sure you want to learn it? ");
		status = 3;
		selectedSkill = selection;
		return;
	}
	if (status == 3) {
		var message = "Necessary items: \r\n"
		for (var i = 0; i < availableSkills[selectedSkill].requirements.length; i++) {
			var requirement = availableSkills[selectedSkill].requirements[i];
			message += requirement.qty + " x #b#t" + requirement.item + "##k\r\n" + "#b#i" + requirement.item + "##k\t\r\n"; 
		}
		cm.sendNext(message);
		status = 4;
		return;
	}
	if (status === 4) {
		for (var i = 0; i < availableSkills[selectedSkill].requirements.length; i++) {
			var requirement = availableSkills[selectedSkill].requirements[i];
			if (!cm.getPlayer().haveItem(requirement.item, requirement.qty)) {
				cm.sendOk("You don't have all the necessary items.");
				cm.dispose();
				return;
			}
		}
		for (var i = 0; i < availableSkills[selectedSkill].requirements.length; i++) {
			var requirement = availableSkills[selectedSkill].requirements[i];
			cm.getPlayer().removeItem(requirement.item, -requirement.qty);
		}
		cm.sendOk("You've learn the skill " + availableSkills[selectedSkill].name)
		cm.teachSkill(availableSkills[selectedSkill].value, 0);
		status = 5;
	}
	cm.dispose();
}