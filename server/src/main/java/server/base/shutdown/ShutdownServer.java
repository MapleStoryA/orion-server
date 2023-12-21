package server.base.shutdown;

import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.WorldServer;
import handling.world.alliance.AllianceManager;
import handling.world.guild.GuildManager;
import handling.world.helper.BroadcastHelper;
import handling.world.helper.WorldInitHelper;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import lombok.extern.slf4j.Slf4j;
import tools.MaplePacketCreator;

@Slf4j
public class ShutdownServer implements ShutdownServerMBean {

    public static ShutdownServer instance;
    public int mode = 0;

    public static void registerMBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            instance = new ShutdownServer();
            mBeanServer.registerMBean(instance, new ObjectName("server:type=ShutdownServer"));
        } catch (Exception e) {
            log.info("Error registering Shutdown MBean");
            e.printStackTrace();
        }
    }

    public static ShutdownServer getInstance() {
        return instance;
    }

    public void run() {
        if (this.mode == 0) {
            BroadcastHelper.broadcastMessage(
                    MaplePacketCreator.serverNotice(0, "The world is going to shutdown soon. Please log off safely."));
            for (ChannelServer cs : WorldServer.getInstance().getAllChannels()) {
                cs.setShutdown();
                cs.setServerMessage("The world is going to shutdown soon. Please log off safely.");
                cs.closeAllMerchant();
            }
            GuildManager.save();
            AllianceManager.save();
            this.mode += 1;
        } else if (this.mode == 1) {
            this.mode += 1;
            log.info("Shutdown 2 commencing...");

            try {
                BroadcastHelper.broadcastMessage(MaplePacketCreator.serverNotice(
                        0, "The world is going to shutdown now. Please log off safely."));
                for (ChannelServer channelServer : WorldServer.getInstance().getAllChannels()) {
                    synchronized (this) {
                        channelServer.shutdown();
                    }
                }
                LoginServer.getInstance().shutdown();
                CashShopServer.getInstance().shutdown();
                WorldInitHelper.initTimers();
            } catch (Exception e) {
                log.info("Failed to shutdown..." + e);
            }

            log.info("Shutdown 2 has finished.");
            this.mode = 0;
            log.info("Done.");
        }
    }

    public void shutdown() {
        run();
    }
}
