/**
 * Assumptions:
 *  - All events have a timer.
 *  - All events requires a party.
 * */

startMap = 100000000;
endMap = 100000000

function onEventInit() {
    event.log("onEventInit")
}

function onEventStart() {
    event.log("onEventStart")
}

function onEventEnd() {
    event.log("onEventEnd")
}