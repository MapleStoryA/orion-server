/**
 *@author Jvlaple
 *party3_r4pt
 */
load("nashorn:mozilla_compat.js");
importPackage(java.lang);

function enter(pi) {
	var tehwat = Math.random() * 3;
	if (tehwat > 1) {
		pi.warp(920010600, 1);
	} else {
		pi.warp(920010600, 2);
	}
}