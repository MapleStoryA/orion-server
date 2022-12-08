import ch.qos.logback.classic.ClassicConstants;
import client.SkillFactory;
import constants.JobConstants;
import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.WorldServer;
import handling.world.guild.MapleGuild;
import handling.world.helper.WorldInitHelper;
import handling.world.respawn.RespawnWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.AutobanManager;
import server.ItemMakerFactory;
import server.MapleCarnivalFactory;
import server.MapleItemInformationProvider;
import server.RandomRewards;
import server.RankingWorker;
import server.ServerProperties;
import server.ShutdownServer;
import server.SpeedQuizFactory;
import server.SpeedRunner;
import server.Timer;
import server.Timer.CheatTimer;
import server.Timer.EtcTimer;
import server.TimerManager;
import server.cashshop.CashItemFactory;
import server.config.ServerConfig;
import server.config.ServerEnvironment;
import server.events.MapleOxQuizFactory;
import server.gachapon.GachaponFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.PlayerNPC;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.Executors;

import static handling.world.respawn.RespawnWorker.CHANNELS_PER_THREAD;


public class GameApp {

    static {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, "config/logback.xml");
    }

    private static final Logger log = LoggerFactory.getLogger(GameApp.class);

    private static long startTime = System.currentTimeMillis();

    public static void main(String[] args) throws InterruptedException {
        ServerEnvironment.getConfig();
        GameApp server = new GameApp();
        server.start();
        log.info("[" + ServerProperties.getProperty("login.serverName") + "]");
    }

    private static void initDatabase() {
        try {
            ServerConfig config = ServerEnvironment.getConfig();
            DatabaseConnection.initConfig(config);
        } catch (SQLException ex) {
            throw new RuntimeException("[SQL EXCEPTION] Error connecting to the database.", ex);
        }
    }

    private static void setAccountsAsLoggedOff() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = 0");
            ps.executeUpdate();
            ps.close();

        } catch (SQLException ex) {
            throw new RuntimeException("[EXCEPTION] Please check if the SQL server is active.", ex);
        }
    }

    private static void printLoad(String thread) {
        log.info("[Loading Completed] " + thread + " | Completed in " + (System.currentTimeMillis() - startTime) + " Milliseconds.");
    }

    public static void listenCommand() {
        try (Scanner sc = new Scanner(System.in)) {
            String input;
            input = sc.nextLine();
            String command = input;
            if (command.contains("shutdown")) {
                Thread t = null;
                if (t == null || !t.isAlive()) {
                    t = new Thread(ShutdownServer.getInstance());
                    ShutdownServer.getInstance().shutdown();
                    t.start();
                }
            } else if (command.contains("restart")) {
                Thread t = new Thread(ShutdownServer.getInstance());
                ShutdownServer.getInstance().shutdown();
                t.start();
                EtcTimer.getInstance().schedule(new Runnable() {
                    public void run() {
                        String[] args = {"restart"};
                        try {
                            main(args);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 3 * 1000);
            }
        }

    }

    public void start() throws InterruptedException {
        initDatabase();
        setAccountsAsLoggedOff();


        WorldInitHelper.initCommunity();
        WorldInitHelper.initTimers();
        TimerManager.getInstance().start();

        var executorService = Executors.newFixedThreadPool(10);
        executorService.submit(() -> {
            printLoad("WorldLoader");
            MapleGuildRanking.getInstance().getRank();
            MapleGuild.loadAll();
        });

        executorService.submit(() -> {
            printLoad("QuestLoader");
            MapleQuest.initQuests();
            MapleLifeFactory.loadQuestCounts();
        });

        executorService.submit(() -> {
            printLoad("ProviderLoader");
            MapleItemInformationProvider.getInstance().load();
        });

        executorService.submit(() -> {
            printLoad("MonsterLoader");
            MapleMonsterInformationProvider.getInstance().load();
        });

        executorService.submit(() -> {
            printLoad("SkillFactoryLoader");
            SkillFactory.getSkill(99999999);
            JobConstants.loadAllSkills();
        });

        executorService.submit(() -> {
            printLoad("BasicLoader");
            LoginInformationProvider.getInstance();
            RandomRewards.getInstance();
            MapleOxQuizFactory.getInstance().initialize();
            MapleCarnivalFactory.getInstance().initialize();
            SpeedRunner.getInstance().loadSpeedRuns();
            SpeedQuizFactory.getInstance().initialize();
            ItemMakerFactory.getInstance();
            MapleMapFactory.loadCustomLife();
            GachaponFactory.getInstance();
        });

        executorService.submit(() -> {
            printLoad("CashItemLoader");
            if (!ServerEnvironment.isDebugEnabled()) {
                CashItemFactory.getInstance().loadCashShopData();
            }
        });

        executorService.shutdown();


        log.info("[Loading Login]");
        LoginServer.getInstance();
        log.info("[Login Initialized]");

        log.info("[Loading Channel]");

        WorldServer worldServer = WorldServer.getInstance();

        for (int i = 0; i < Integer.parseInt(ServerProperties.getProperty("channel.count", "0")); i++) {
            int channel = i + 1;
            int port = Short.parseShort(ServerProperties.getProperty("channel.net.port" + channel, String.valueOf(ChannelServer.DEFAULT_PORT + channel)));
            ChannelServer ch = new ChannelServer(channel, port);
            worldServer.registerChannel(channel, ch);
            ch.onStart();
        }


        log.info("[Channel Initialized]");

        log.info("[Loading CS]");
        CashShopServer.getInstance();
        log.info("[CS Initialized]");

        CheatTimer.getInstance().register(AutobanManager.getInstance(), 60000);
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));

        Integer[] chs = WorldServer.getInstance().getAllChannelIds().toArray(new Integer[0]);
        for (int i = 0; i < chs.length; i += CHANNELS_PER_THREAD) {
            Timer.WorldTimer.getInstance().register(new RespawnWorker(chs, i), 1125); //divisible by 9000 if possible.
        }

        if (ShutdownServer.getInstance() == null) {
            ShutdownServer.registerMBean();
        } else {
            log.info("--MBean server was already active--");
        }
        PlayerNPC.loadAll();
        log.info("[Fully Initialized in " + (System.currentTimeMillis() - startTime) / 1000L + " seconds]");
        RankingWorker.getInstance().run();


        log.info("[/////////////////////////////////////////////////]");
        log.info("Console Commands: ");
        log.info("say | prefixsay | shutdown | restart");
        listenCommand();
    }

    public static class Shutdown implements Runnable {

        @Override
        public void run() {
            ShutdownServer.getInstance().run();
        }
    }

}
