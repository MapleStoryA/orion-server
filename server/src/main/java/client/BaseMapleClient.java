package client;

import constants.ServerConstants;
import handling.session.NetworkSession;
import tools.MapleAESOFB;
import tools.MaplePacketCreator;

public class BaseMapleClient {
    public static final String CLIENT_KEY = "CLIENT";

    public static final byte LOGIN_NOTLOGGEDIN = 0,
            LOGIN_SERVER_TRANSITION = 1,
            LOGIN_LOGGEDIN = 2,
            LOGIN_WAITING = 3,
            CASH_SHOP_TRANSITION = 4,
            LOGIN_CS_LOGGEDIN = 5,
            CHANGE_CHANNEL = 6;

    protected final transient MapleAESOFB send;
    protected final transient MapleAESOFB receive;
    protected NetworkSession session;

    public BaseMapleClient(MapleAESOFB send, MapleAESOFB receive, NetworkSession session) {
        this.send = send;
        this.receive = receive;
        this.session = session;
    }


    public BaseMapleClient(byte[] ivSend, byte[] ivRecv, NetworkSession session) {
        this(new MapleAESOFB(ivSend, (short) (0xFFFF - ServerConstants.MAPLE_VERSION)),
                new MapleAESOFB(ivRecv, ServerConstants.MAPLE_VERSION), session);
    }

    public final MapleAESOFB getReceiveCrypto() {
        return receive;
    }

    public final MapleAESOFB getSendCrypto() {
        return send;
    }

    public final NetworkSession getSession() {
        return session;
    }

    public void sendPacket(byte[] packet) {
        getSession().write(packet);
    }

    public void enableActions() {
        getSession().write(MaplePacketCreator.enableActions());
    }
}
