package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import server.timer.TimerManager;
import tools.MaplePacketCreator;

@Slf4j
public class LuckyLogoutHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        // this is only called the when player log outs, i've no idea how to
        // make it disappear, but oh well..just dc the client, since
        // they attempted to log out
        final int selection = packet.readInt();
        // if (selected) { // return / dc.
        final MapleCharacter player = c.getPlayer();
        if (selection >= 0 && selection <= 2) { // 0, 1 or 2
            // gifting here..etc, i don't have the official one, so i'll use my
            // own.
            // add a check to the disconnect function to see if the char have
            // any logout gift, and keep shows till the user selected
            // reload every 1 day, or 3 days?
            // I have to use a custom one.
            // god..how to make the box disappear? ...
            c.getPlayer().dropMessage(1, "You will receive your gift in 3 days!");
            // I'm sure there's a packet to make the box disappear ==
            c.getSession().write(MaplePacketCreator.luckyLogoutGift((byte) 1, "70000393"));
            TimerManager.getInstance()
                    .schedule(
                            new Runnable() {

                                @Override
                                public void run() {
                                    c.getSession().close();
                                }
                            },
                            2000);
        }
    }
}
