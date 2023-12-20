package handling.channel.handler.utils;

import client.MapleClient;
import handling.world.guild.GuildManager;
import handling.world.guild.MapleBBSThread;
import java.util.List;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class BBSHandlerUtils {

    public static final String correctLength(final String in, final int maxSize) {
        if (in.length() > maxSize) {
            return in.substring(0, maxSize);
        }
        return in;
    }

    public static void listBBSThreads(MapleClient c, int start) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }
        c.getSession()
                .write(MaplePacketCreator.BBSThreadList(
                        GuildManager.getBBS(c.getPlayer().getGuildId()), start));
    }

    public static final void newBBSReply(final MapleClient c, final int localthreadid, final String text) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }
        GuildManager.addBBSReply(
                c.getPlayer().getGuildId(), localthreadid, text, c.getPlayer().getId());
        displayThread(c, localthreadid);
    }

    public static final void editBBSThread(
            final MapleClient c, final String title, final String text, final int icon, final int localthreadid) {
        if (c.getPlayer().getGuildId() <= 0) {
            return; // expelled while viewing?
        }
        GuildManager.editBBSThread(
                c.getPlayer().getGuildId(),
                localthreadid,
                title,
                text,
                icon,
                c.getPlayer().getId(),
                c.getPlayer().getGuildRank());
        displayThread(c, localthreadid);
    }

    public static final void newBBSThread(
            final MapleClient c, final String title, final String text, final int icon, final boolean bNotice) {
        if (c.getPlayer().getGuildId() <= 0) {
            return; // expelled while viewing?
        }
        displayThread(
                c,
                GuildManager.addBBSThread(
                        c.getPlayer().getGuildId(),
                        title,
                        text,
                        icon,
                        bNotice,
                        c.getPlayer().getId()));
    }

    public static final void deleteBBSThread(final MapleClient c, final int localthreadid) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }
        GuildManager.deleteBBSThread(
                c.getPlayer().getGuildId(),
                localthreadid,
                c.getPlayer().getId(),
                c.getPlayer().getGuildRank());
    }

    public static final void deleteBBSReply(final MapleClient c, final int localthreadid, final int replyid) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }

        GuildManager.deleteBBSReply(
                c.getPlayer().getGuildId(),
                localthreadid,
                replyid,
                c.getPlayer().getId(),
                c.getPlayer().getGuildRank());
        displayThread(c, localthreadid);
    }

    public static final void displayThread(final MapleClient c, final int localthreadid) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }
        final List<MapleBBSThread> bbsList = GuildManager.getBBS(c.getPlayer().getGuildId());
        if (bbsList != null) {
            for (MapleBBSThread t : bbsList) {
                if (t != null && t.localthreadID == localthreadid) {
                    c.getSession().write(MaplePacketCreator.showThread(t));
                }
            }
        }
    }
}
