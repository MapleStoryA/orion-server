import './global';

function event() {
    let gm = target.getGameEventManager();
    let result = gm.create(target.getPlayer(), "test");
    if (result === -1) {
        let event = target.getPlayer().getEvent();
        event.startEvent(target.getPlayer(), 100000000, 100000000, 10)
        self.say("You are already in the event")
        return;
    }
    if (result === -2) {
        self.say("Please create a party first")
        return;
    }
    if (result === -3) {
        self.say("Only the party leader can start this event")
        return;
    }
    self.say("Ok, be prepared for the event to start")

}

event();