function enter(pi) {
	var mapsToWarp = [];
	mapsToWarp[680100000] = 100000000;
	mapsToWarp[680100001] = 260000000;
	mapsToWarp[680100003] = 200000000;
	var map = mapsToWarp[pi.getMap().getId()];
	if(!map){
		pi.warp(100000000,0);
	}else{
		pi.warp(map,0);
	}
	pi.playPortalSE();
}