package handling.world.helper;

import handling.world.alliance.AllianceManager;
import handling.world.messenger.MessengerManager;
import handling.world.party.PartyManager;
import lombok.extern.slf4j.Slf4j;
import server.timer.Timer;
import server.timer.Timer.WorldTimer;

@Slf4j
public class WorldInitHelper {

    public static void initCommunity() {
        FindCommand.findChannel(0);
        AllianceManager.lock.toString();
        MessengerManager.getMessenger(0);
        PartyManager.getParty(0);
    }

    public static void initTimers() {
        WorldTimer.getInstance().start();
        Timer.EtcTimer.getInstance().start();
        Timer.MapTimer.getInstance().start();
        Timer.MobTimer.getInstance().start();
        Timer.CloneTimer.getInstance().start();
        Timer.EventTimer.getInstance().start();
        Timer.BuffTimer.getInstance().start();
        Timer.PingTimer.getInstance().start();
    }
}
