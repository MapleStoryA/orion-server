/*Stage 2 Box For LPQ
 *@Author Jvlaple
 */
 
function act(){
	return;
	var map = rm.getPlayer().getMap().getId();
	if(map == 220020300){
		return;
	}
	rm.playerMessage(5, "An unknown force has warped you into a trap.");
	rm.warp(922010201);
}