var GACHA_CONFIG = {
	basePath: getScriptPath(),
	npcBasePath: 'npc',
	gachaponScript: 'Gachapon.js',
	getBasePath: function() {
		return basePath;
	},
	getNpcPath : function(){
		return this.basePath + '/' + this.npcBasePath;
	},
	getGachaponPath : function(){
		return this.getNpcPath() + '/' + this.gachaponScript;
	}
}
