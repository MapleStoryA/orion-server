var servername = "MapleStory";
var answer = ["MapleStory Rev 3.0 \r\n\r\n2012/01/04 - This npc is born", "Server Name - #rMapleStory#k\r\nRates:\r\n#rXP#k : #b500\r\n#rMeso#k : #b2500\r\n#rDrop#k : #b3#k\r\n\r\n_____FaQ_____\r\n#rQ:#k What was this server originally named ?\r\n#bA: OxyMS.\r\n#rQ:#kSince when do you make maplestory private servers?\r\n#bA: Since 2007, with GMS v53. I couldn't code really though.", "#b@commands / @help - Show you the list of commands\r\n@online - Show you which players are online\r\n@info - Open up the npc you're reading ATM\r\n@genocide <name> - kill the player for the cost of 50 charms of the undead\r\n@anonymousrape - Same as @genocide but it will not show your name and it cost 200 Charms instead.\r\n@dropnx - Open up Tau, the NX item dropper\r\n@home / @henesys - Warp you to henesys o.0\r\n@hhg1 - warp you to henesys hunting ground 1\r\n@fm - warp you to FM\r\n@str / @int / @dex / @luk - Distribute AP faster\r\n@clearslot <eq/use/setup/etc/cash/all> - clear your an inventory\r\n@ea - unstuck yourself if you cannot talk to any npc/change map/loot item\r\n@ranking <rank> <start num> <end num> - check out the ranking duh\r\n"];

function start() {
    cm.sendSimple("#eHello and welcome to #r"+servername+"#k info npc. You may get various information about the server here.\r\n\r\n#L0#Recent #rupdate#k#l\r\n#L1#Server #rnews#k#l\r\n#L2#Tell me the #rbasic information#k#l\r\n#L3#Tell me more about the #rcommands#k in MapleStory\r\n#L4#What are the #rnpc's#k in MapleStory?");
}

function action(m,t,s) {
    if (m > 0) {
            cm.sendOk("#e" + answer[s]);
            cm.dispose();
    } else {
        cm.sendOk("#eEnjoy your stay on MapleStory v90!");
        cm.dispose();
    }
}