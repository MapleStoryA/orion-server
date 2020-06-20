/* Author: Xterminator
	NPC Name: 		Shanks
	Map(s): 		Maple Road : Southperry (60000)
	Description: 		Brings you to Victoria Island
*/
var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	cm.sendOk("Hmm ... Eu acho que voce ainda tem coisas a fazer aqui?");
	cm.dispose();
	return;
}