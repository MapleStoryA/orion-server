
var quest = 21733;
function enter(pi) {
    pi.playPortalSE();
    
    if(pi.getQuestStatus(21733) == 1){
    	pi.warpAndSpawnMonster(910400000, 9300344, new java.awt.Point(20, 120));
    }else{
    	pi.warp(104000004, 1);
    }
    
}