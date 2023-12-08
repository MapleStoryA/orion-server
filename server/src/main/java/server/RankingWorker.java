package server;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RankingWorker {

    private static RankingWorker instance;
    private final Map<Integer, List<RankingInformation>> rankings = new HashMap<>();
    private final Map<String, Integer> jobCommands = new HashMap<>();

    public static RankingWorker getInstance() {
        if (instance == null) {
            instance = new RankingWorker();
        }
        return instance;
    }

    public final Integer getJobCommand(final String job) {
        return jobCommands.get(job);
    }

    public final Map<String, Integer> getJobCommands() {
        return jobCommands;
    }

    public final List<RankingInformation> getRankingInfo(final int job) {
        return rankings.get(job);
    }

    public void run() {
        log.info("Loading Rankings::");
        loadJobCommands();
        try (var con = DatabaseConnection.getConnection()) {
            updateRanking(con);
        } catch (Exception ex) {
            log.error("Log_Script_Except.rtf", ex);
        }
        log.info("Done loading Rankings :::"); // keep
    }

    private void updateRanking(Connection con) throws Exception {
        String sb =
                "SELECT c.id, c.job, c.exp, c.level, c.name, c.jobRank, c.jobRankMove, c.rank,"
                    + " c.rankMove, a.lastlogin AS lastlogin, a.loggedin FROM characters AS c LEFT"
                    + " JOIN accounts AS a ON c.accountid = a.id WHERE c.gm = 0 AND a.banned = 0"
                    + " ORDER BY c.level DESC , c.exp DESC , c.fame DESC , c.meso DESC , c.rank"
                    + " ASC";

        PreparedStatement charSelect = con.prepareStatement(sb);
        ResultSet rs = charSelect.executeQuery();
        PreparedStatement ps =
                con.prepareStatement(
                        "UPDATE characters SET jobRank = ?, jobRankMove = ?, rank = ?, rankMove = ?"
                                + " WHERE id = ?");
        int rank = 0; // for "all"
        final Map<Integer, Integer> rankMap = new LinkedHashMap<>();
        for (int i : jobCommands.values()) {
            rankMap.put(i, 0); // job to rank
            rankings.put(i, new ArrayList<RankingInformation>());
        }
        while (rs.next()) {
            int job = rs.getInt("job");
            if (!rankMap.containsKey(job / 100)) { // not supported.
                continue;
            }
            int jobRank = rankMap.get(job / 100) + 1;
            rankMap.put(job / 100, jobRank);
            rank++;
            rankings.get(-1)
                    .add(
                            new RankingInformation(
                                    rs.getString("name"),
                                    job,
                                    rs.getInt("level"),
                                    rs.getInt("exp"),
                                    rank));
            rankings.get(job / 100)
                    .add(
                            new RankingInformation(
                                    rs.getString("name"),
                                    job,
                                    rs.getInt("level"),
                                    rs.getInt("exp"),
                                    jobRank));
            ps.setInt(1, jobRank);
            ps.setInt(2, rs.getInt("jobRank") - jobRank);
            ps.setInt(3, rank);
            ps.setInt(4, rs.getInt("rank") - rank);
            ps.setInt(5, rs.getInt("id"));
            ps.addBatch();
        }
        ps.executeBatch(); // Batch update should be faster.
        rs.close();
        charSelect.close();
        ps.close();
    }

    public final void loadJobCommands() {
        if (!jobCommands.isEmpty()) {
            return;
        }
        jobCommands.put("all", -1);
        jobCommands.put("beginner", 0);
        jobCommands.put("warrior", 1);
        jobCommands.put("magician", 2);
        jobCommands.put("bowman", 3);
        jobCommands.put("thief", 4);
        jobCommands.put("pirate", 5);
        jobCommands.put("nobless", 10);
        jobCommands.put("soulmaster", 11);
        jobCommands.put("flamewizard", 12);
        jobCommands.put("windbreaker", 13);
        jobCommands.put("nightwalker", 14);
        jobCommands.put("striker", 15);
        jobCommands.put("legend", 20);
        jobCommands.put("aran", 21);
        jobCommands.put("evan", 22);
    }

    public static class RankingInformation {

        public int job, reborns, level, exp, rank;
        public String name, toString;

        public RankingInformation(String name, int job, int level, int exp, int rank) {
            this.name = name;
            this.job = job;
            this.level = level;
            this.exp = exp;
            this.rank = rank;
            loadToString();
        }

        public final void loadToString() {
            String builder =
                    "Rank "
                            + rank
                            + " : "
                            + name
                            + " - Reborns "
                            + reborns
                            + " - Level "
                            + level
                            + " "
                            + MapleCarnivalChallenge.getJobNameById(job)
                            + " | "
                            + exp
                            + " EXP";
            this.toString = builder; // Rank 1 : AuroX - Level 200 SuperGM | 0 EXP
        }

        @Override
        public String toString() {
            return toString;
        }
    }
}
