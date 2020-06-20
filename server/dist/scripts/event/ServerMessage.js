var Message = new Array(
    "Please do not use foul language, harass or scam other players. We would like to keep this community clean & friendly.",
    "Gather your friends and enjoy the fun of our Party Quests !",
    "Please report any bugs/glitches at our forum.",
    "Use @ea if you cant speak to a NPC.",
    "Make a party with your friends and conquer Mulung Dojo! Take down the bosses and receive points to exchange for belts",
    "Purchase cash items on cash shop to create your unique character look!",
    "There will be Channel limit for certain bosses. You can only fight the bosses in the stated channel.",
    "Friendship rings/friendship shirt are working! ",
    "Look for Mar the Fairy at Ellinia with the rock of evolution to evolve your pet dragon or pet robo.",
    "Look for Mar the Fairy to get a pet Snail Roon that auto loots meso/drops for you.",
    "Please report any bugs you are facing immediately in the forums!",
    "The server rates from level 1 to 10 are 1x",
    "To become a DualBlade complete the quest that appears at level 2 and finish the necessary quests ");

var setupTask;

function init() {
    scheduleNew();
}

function scheduleNew() {
    setupTask = em.schedule("start", 900000);
}

function cancelSchedule() {
	setupTask.cancel(false);
}

function start() {
    scheduleNew();
    em.broadcastYellowMsg("[MapleTip] " + Message[Math.floor(Math.random() * Message.length)]);
}