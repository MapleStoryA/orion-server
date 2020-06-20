songs = [];
if(songs.length == 0){
	initArr();
	
}
let keys = Object.keys(songs);

option = self.askMenu( getFirstMenu() );

let selectedCategory = keys[option];

let songsOfGivenMusicCategory = songs[selectedCategory];

let option = self.askMenu( getSecondMenu() );

let selectedSong = selectedCategory + "/" + songs[selectedCategory][option];

self.sayOk("#rNow playing:\r\n#Wicon8/0# #b" + selectedCategory + " - " + songs[selectedCategory][option] + "");

if(target.isMaster()){
	field.changeMusic(selectedSong);
}else{
	target.changeMusic(selectedSong);
}
 


function initArr() {
    songs["Bgm00"] = ["SleepyWood", "FloralLife", "GoPicnic", "Nightmare", "RestNPeace", "DragonDream"];
    songs["Bgm01"] = ["AncientMove", "MoonlightShadow", "WhereTheBarlogFrom", "CavaBien", "HighlandStar", "BadGuys"];
    songs["Bgm02"] = ["MissingYou", "WhenTheMorningComes", "EvilEyes", "JungleBook", "AboveTheTreetops"];
    songs["Bgm03"] = ["Subway", "Elfwood", "BlueSky", "Beachway", "SnowyVillage"];
    songs["Bgm04"] = ["PlayWithMe", "WhiteChristmas", "UponTheSky", "ArabPirate", "Shinin&apos;Harbor", "WarmRegard"];
    songs["Bgm05"] = ["WolfWood", "DownToTheCave", "AbandonedMine", "MineQuest", "HellGate"];
    songs["Bgm06"] = ["FinalFight", "WelcomeToTheHell", "ComeWithMe", "FlyingInABlueDream", "FantasticThinking"];
    songs["Bgm07"] = ["WaltzForWork", "WhereverYouAre", "FunnyTimeMaker", "HighEnough", "Fantasia"];
    songs["Bgm08"] = ["LetsMarch", "ForTheGlory", "FindingForest", "LetsHuntAliens", "PlotOfPixie"];
    songs["Bgm09"] = ["DarkShadow", "TheyMenacingYou", "FairyTale", "FairyTalediffvers", "TimeAttack"];
    songs["Bgm10"] = ["Timeless", "TimelessB", "BizarreTales", "TheWayGrotesque", "Eregos"];
    songs["Bgm11"] = ["BlueWorld", "Aquarium", "ShiningSea", "DownTown", "DarkMountain"];
    songs["Bgm12"] = ["AquaCave", "DeepSee", "WaterWay", "AcientRemain", "RuinCastle", "Dispute"];
    songs["Bgm13"] = ["CokeTown", "Leafre", "Minar&apos;sDream", "AcientForest", "TowerOfGoddess", "FightSand"];
    songs["Bgm14"] = ["DragonLoad", "HonTale", "CaveOfHontale", "DragonNest", "Ariant", "HotDesert", "DragonRider"];
    songs["Bgm15"] = ["MureungHill", "MureungForest", "WhiteHerb", "Pirate", "SunsetDesert", "Nautilus", "inNautilus", "ElinForest", "PoisonForest"];
    songs["Bgm16"] = ["TimeTemple", "Remembrance", "Repentance", "Forgetfulness", "Duskofgod", "FightingPinkBeen"];
    songs["Bgm17"] = ["MureungSchool1", "MureungSchool2", "MureungSchool3", "MureungSchool4", "secretFlower"];
    songs["Bgm18"] = ["QueensGarden", "DrillHall", "BlackWing", "RaindropFlower", "WolfAndSheep"];
    songs["Bgm19"] = ["RienVillage", "SnowDrop", "BambooGym", "CrystalCave", "MushCatle"];
    songs["Bgm20"] = ["NetsPiramid", "NetsPiramid2", "UnderSubway", "UnderSubway2", "GhostShip"];
    songs["Bgm21"] = ["KerningSquare", "KerningSquareField", "KerningSquareSubway", "TeraForest", "2021year", "2099year", "2215year", "2230year", "2503year"];
    songs["BgmEvent"] = ["FunnyRabbit", "FunnyRabbitFaster", "wedding", "weddingDance", "wichTower"];
    songs["BgmGL"] = ["amoria", "chapel", "cathedral", "Amorianchallenge", "NLCupbeat", "NLChunt", "NLCtown", "HauntedHouse", "CrimsonwoodKeep", "Bigfoot", "PhantomForest", "CrimsonwoodKeepInterior", "GrandmastersGauntlet", "PartyQuestGL", "Courtyard"];
    songs["BgmJp"] = ["Feeling", "BizarreForest", "Yume", "Bathroom", "BattleField", "Hana", "FirstStepMaster", "CastleOutSide", "CastleInside", "CastleBoss", "CastleTrap", "WhenTheMorningComes", "Elfwoods"];
    songs["BgmMY"] = ["KualaLumpur", "Highland"];
    songs["BgmSG"] = ["CBD_town", "CBD_field", "BoatQuay_field", "Ghostship", "BoatQuay_town", "Ulu_field"];
    songs["BgmTH"] = ["goldTempleTownTH", "goldTempleFieldTH", "goldTempleDungeonTH"];
    
    if(target.isMaster()){
    	 songs["MiscSongs"] = ["Harlem Shake", "Katy Perry - I Kissed A Girl", "Marshmello - Alone", "Never gonna give up", "Turn Down For What"];
    	 songs["TheOffSpring"] = ["The Kids Aren't Alright"];
    }
   
}

function getFirstMenu() {
	let text = "Choose your song category: \r\n";
	let keys = Object.keys(songs);
	for(var i = 0; i < keys.length; i++){
		text += "#L" + i + "##Wicon8/0# " + keys[i] + "\r\n";
	}
	return text;
}

function getSecondMenu(){
	var textMusics = "Now choose your song. \r\n";;
	for(var i = 0; i < songsOfGivenMusicCategory.length; i++){
		textMusics += "#L" + i + "##Wicon8/0# " + songsOfGivenMusicCategory[i] + "\r\n";
	}
	return textMusics;
}