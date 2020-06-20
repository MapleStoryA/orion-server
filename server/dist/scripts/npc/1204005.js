 /* 
	NPC Name: 		Tru
	Map(s): 		Info Shop quest
	Description: 		Buff
*/

function start() {
	
	action(1, 0, 0);
}
var quest = 21733;
var status = 1;
function action(mode, type, selection) {
	if(cm.getMap().getAllMonster().size() === 1){
		cm.sendOk("Defeat him!");
		cm.dispose();
	}
	if(status == 1){
		cm.sendOk("Don't stop training. Every ounce of your energy is required to protect the world of Maple....");
		status = 2;
		return;
	}
	cm.warp(104000004, 0);
	cm.teachSkill(21100000, 0);//Pole arm mastery
	cm.forceCompleteQuest(quest);
	cm.getPlayer().dropMessage(5, "You've learn a new skill Polearm Mastery");
	cm.dispose();
}