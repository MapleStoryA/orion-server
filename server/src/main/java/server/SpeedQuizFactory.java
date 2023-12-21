package server;

import database.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** @author AuroX */
@lombok.extern.slf4j.Slf4j
public class SpeedQuizFactory {

    private static final SpeedQuizFactory instance = new SpeedQuizFactory();
    private final Map<QuizEntry, Integer> quiz = new HashMap<>();

    public SpeedQuizFactory() {}

    public static SpeedQuizFactory getInstance() {
        return instance;
    }

    public void initialize() {
        if (!quiz.isEmpty()) {
            return;
        }
        log.info("Loading Speed Quiz Data...");
        try (var con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `wz_speedquiz`");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quiz.put(
                            new QuizEntry(rs.getByte("type"), rs.getInt("objectid"), rs.getString("answer")),
                            rs.getInt("questionNo"));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load Speed Quiz data. Reason: " + e);
        }
    }

    public List<QuizEntry> getQuizDataType(final int questionId, final byte type) {
        List<QuizEntry> entries = new LinkedList<>();
        for (final Entry<QuizEntry, Integer> q : quiz.entrySet()) {
            if (q.getValue() == questionId && q.getKey().getType() == type) {
                entries.add(q.getKey());
            }
        }
        return entries;
    }

    public static class QuizEntry {

        public byte type;
        public int oid;
        public String answer;

        public QuizEntry(byte type, int oid, String answer) {
            this.type = type;
            this.oid = oid;
            this.answer = answer;
        }

        public byte getType() {
            return type;
        }

        public int getObjectId() {
            return oid;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
