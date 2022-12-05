
var status = -1;
var mobid = 0;
function action(mode, type, selection) {
	//cm.debug('mode: ' + mode + ' type: ' + type + ' selection: ' + selection + ' status:' + status);
	mobid = 9400641 + parseInt(cm.getPlayer().getEventInstance().getProperty("mode"));
	var stage = cm.getPlayer().getEventInstance().getProperty("stage");
	if(cm.haveMonster(mobid) || stage == null || stage.equals("1")){
		cm.dispose();
		return;
	}
	if(status == -1){
		cm.sendGetText("#b#kIt's the mirror that buber talked about. You can almost feel someone staring back at you.");
		status = 1;
		return;
	}
	if(cm.getText()!= null && cm.getText().equals('Olivia')){
		if (cm.getPlayer().getEventInstance() != null && stage.equals("0")) {
			cm.spawnMonster(mobid);
			cm.getPlayer().getEventInstance().setProperty("stage", "1");
		}
		cm.dispose();
	}
	cm.dispose()
   
}