import client.MapleCharacter;
import client.SkillFactory;
import constants.JobConstants;
import server.config.ServerConfig;
import server.config.ServerEnvironment;
import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.World;
import handling.world.guild.MapleGuild;
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
import server.Timer.BuffTimer;
import server.Timer.CheatTimer;
import server.Timer.CloneTimer;
import server.Timer.EtcTimer;
import server.Timer.EventTimer;
import server.Timer.MapTimer;
import server.Timer.MobTimer;
import server.Timer.PingTimer;
import server.Timer.WorldTimer;
import server.TimerManager;
import server.cashshop.CashItemFactory;
import server.events.MapleOxQuizFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.PlayerNPC;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;
import tools.AutoJCE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Start {

    public static long startTime = System.currentTimeMillis();
    public static final Start instance = new Start();

    public static void main(String[] args) throws InterruptedException {
        AutoJCE.removeCryptographyRestrictions();
        ServerEnvironment.getConfig();
        instance.run();
    }

    public void run() throws InterruptedException {

        try {
            ServerConfig config = ServerEnvironment.getConfig();
            DatabaseConnection.initConfig(config);
        } catch (SQLException ex) {
            throw new RuntimeException("[SQL EXCEPTION] Error connecting to the database.", ex);
        }

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = 0");
            ps.executeUpdate();
            ps.close();

        } catch (SQLException ex) {
            throw new RuntimeException("[EXCEPTION] Please check if the SQL server is active.", ex);
        }

        System.out.println("[" + ServerProperties.getProperty("login.serverName") + "]");
        World.init();
        WorldTimer.getInstance().start();
        EtcTimer.getInstance().start();
        MapTimer.getInstance().start();
        MobTimer.getInstance().start();
        CloneTimer.getInstance().start();
        EventTimer.getInstance().start();
        BuffTimer.getInstance().start();
        PingTimer.getInstance().start();
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
        });

        executorService.submit(() -> {
            printLoad("CashItemLoader");
            if (!ServerEnvironment.isDebugEnabled()) {
                CashItemFactory.getInstance().loadCashShopData();
            }
        });


        System.out.println("[Loading Login]");
        LoginServer.run_startup_configurations();
        System.out.println("[Login Initialized]");

        System.out.println("[Loading Channel]");
        ChannelServer.startChannel_Main();
        System.out.println("[Channel Initialized]");

        System.out.println("[Loading CS]");
        CashShopServer.run_startup_configurations();
        System.out.println("[CS Initialized]");

        CheatTimer.getInstance().register(AutobanManager.getInstance(), 60000);
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
        World.registerRespawn();
        if (ShutdownServer.getInstance() == null) {
            ShutdownServer.registerMBean();
        } else {
            System.out.println("--MBean server was already active--");
        }
        PlayerNPC.loadAll();
        LoginServer.setOn();
        System.out.println("[Fully Initialized in " + (System.currentTimeMillis() - startTime) / 1000L + " seconds]");
        RankingWorker.getInstance().run();


        System.out.println("[/////////////////////////////////////////////////]");
        System.out.println("Console Commands: ");
        System.out.println("say | prefixsay | shutdown | restart");
        listenCommand();
    }

    private static void printLoad(String thread) {
        System.out.println("[Loading Completed] " + thread + " | Completed in " + (System.currentTimeMillis() - startTime) + " Milliseconds.");
    }

    public static class Shutdown implements Runnable {

        @Override
        public void run() {
            ShutdownServer.getInstance().run();
        }
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

}
