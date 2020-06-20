var mapid = new Array(200000110,200000120,200000130,200000140,200000150);
var platform = new Array("Ellinia","Ludibrium","Leafre","Mu Lung","Ariant");
var flight = new Array("ship","ship","ship","Hak","Geenie");
var menu;
var select;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if(mode == 0 && status == 0) {
	cm.dispose();
	return;
    }
    if(mode == 0) {
	cm.sendOk("Please make sure you know where you are going and then go to the platform through me. The ride is on schedule so you better not miss it!");
	cm.dispose();
	return;
    }
    if(mode == 1)
	status++;
    else
	status--;
    if(status == 0) {
	menu = "Orbis Station has a lots of platforms available to choose from. You need to choose the one that'll take you to the destination of your choice. Which platform will you take?";
	for(var i=0; i < platform.length; i++) {
	    menu += "\r\n#L"+i+"##bThe platform to the ship that heads to "+platform[i]+"#k#l";
	}
	cm.sendSimple(menu);
    } else if(status == 1) {
	select = selection;
	cm.sendYesNo("Even if you took the wrong passage you can get back here using the portal, so no worries. Will you move to the #bplatform to the "+flight[select]+" that heads to "+platform[select]+"#k?");
    } else if(status == 2) {
	cm.warp(mapid[select], 0);
	cm.dispose();
    }
}