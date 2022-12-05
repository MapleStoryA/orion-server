var status = -1;

function start(){
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if(status === -1){
		cm.getPlayer().dropMessage(5, "Hint: Actions speak louder than words");
		cm.sendGetText("#b(Only the correct password will let you in.)");
		status = 1;
	}
	if(status === 1){
		var input = cm.getText();
		if(input == null){
			return;
		}
		
		if(input === 'Actions speak louder than words'){
			cm.warpAndSpawnMonster(925040100, 9300351, new java.awt.Point(980,51));
			cm.dispose();
		}else{
			cm.dispose();
		}
	}
}