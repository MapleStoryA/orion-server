var status = 0;
var mapsToWarp = [
                  
];

function start() {
	mapsToWarp[100000000] = 680100000;
	mapsToWarp[260000000] = 680100001;
	mapsToWarp[200000000] = 680100003;
	status = -1;
	action(1, 0, 0);
}

function isSunday() {
	return new Date().getDay() == 0;
}
function isSaturday() {
	return new Date().getDay() == 6;
}


function action(mode, type, selection) {

	if (mode == -1) {
		return;
	}
	if(isSaturday()){
		cm.sendNext("The 7th Day Market opens tomorrow.");
		cm.dispose();
	}
	if(!isSunday()){
		cm.sendNext("The 7th Day Market is not open yet.");
		cm.dispose();
		return;
	}
	
	if(status === -1){
		var text = "";
		text += "Hello, the Maple 7th Day Market opens today.\r\n"
		text += "#L0##bMove to Maple 7th Day Market map.\r\n"
		text += "#L1##bListen to an explanation about the Maple 7th Day Market.\r\n"
		cm.sendSimple(text);
		status = 1;
		return;
	}
	if(status === 1 && selection === 0){
		cm.sendNext("Okay, we will send you to the 7th Day Market map.")
		status = 2;
		return;
	}
	if(status === 2){
		var map = mapsToWarp[cm.getMap().getId()];
		if(!map){
			cm.warp(680100002);
		}else{
			cm.warp(map);
		}
	}
	cm.dispose();
}