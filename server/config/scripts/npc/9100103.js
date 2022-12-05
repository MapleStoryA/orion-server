var isLoaded = false;
function setupGachapon(){
	if(isLoaded === false){
		load(GACHA_CONFIG.getGachaponPath());
		isLoaded = true;
	}
}
function loadGachaponBase(){
	load(getScriptPath() + '/npc/' + 'GachaponBase.js');
}
function action(mode, type, selection) {
	loadGachaponBase();
	setupGachapon();
	return gachaEntryPoint(mode, type, selection);
}