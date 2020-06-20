/* ===========================================================
			Resonance
	NPC Name: 		SELF
	Map(s): 		Mushroom Castle: Deep inside Mushroom Forest(106020300)
	Description: 	Upon reaching the magic barrier.
=============================================================
Version 1.0 - Script Done.(18/7/2010)
=============================================================
*/

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
	cm.PlayerToNpc("Save the princess and retrive back the #b#t4001318##b.");
	cm.dispose();
}
			