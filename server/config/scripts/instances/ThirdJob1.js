/**
 * Assumptions:
 *  - All events have a timer.
 *  - All events requires a party.
 * */

let monsterId = -1;


function onEventInit() {
    event.log("onEventInit")
}

function onEventStart() {
    event.log("onEventStart")
    let info = event.get3rdJobMobMapInfo();
    monsterId = info[2];
    let field = event.getField(108010301);
    field.spawnMonster(monsterId, 200, 20)
}

function onEventEnd() {
    event.log("onEventEnd")
}