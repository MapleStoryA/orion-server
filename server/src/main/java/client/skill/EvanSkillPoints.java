package client.skill;

import client.MapleJob;
import java.util.HashMap;
import server.config.ServerEnvironment;

@lombok.extern.slf4j.Slf4j
public class EvanSkillPoints {
    public static final String INSERT_POINTS_QUERY =
            "INSERT INTO evan_skillpoints (characterid, evan1, evan2, evan3, evan4, evan5, evan6,"
                    + " evan7, evan8, evan9, evan10) VALUES (";
    private final HashMap<Integer, Integer> skillPoints = new HashMap<>(10, 1.0F);

    public EvanSkillPoints() {
        this.skillPoints.put(Integer.valueOf(2200), Integer.valueOf(0));
        this.skillPoints.put(Integer.valueOf(2210), Integer.valueOf(0));
        this.skillPoints.put(Integer.valueOf(2211), Integer.valueOf(0));
        this.skillPoints.put(Integer.valueOf(2212), Integer.valueOf(0));
        this.skillPoints.put(Integer.valueOf(2213), Integer.valueOf(0));
        this.skillPoints.put(Integer.valueOf(2214), Integer.valueOf(0));
        this.skillPoints.put(Integer.valueOf(2215), Integer.valueOf(0));
        this.skillPoints.put(Integer.valueOf(2216), Integer.valueOf(0));
        this.skillPoints.put(Integer.valueOf(2217), Integer.valueOf(0));
        this.skillPoints.put(Integer.valueOf(2218), Integer.valueOf(0));
    }

    public HashMap<Integer, Integer> getSkillPoints() {
        return this.skillPoints;
    }

    public void addSkillPoints(int job, int points) {
        if (this.skillPoints.containsKey(Integer.valueOf(job))) {
            this.skillPoints.put(
                    Integer.valueOf(job),
                    Integer.valueOf(
                            points + this.skillPoints.get(Integer.valueOf(job)).intValue()));
        }
    }

    public int getSkillPoints(int jobid) {
        if (this.skillPoints.containsKey(Integer.valueOf(jobid))) {
            return this.skillPoints.get(Integer.valueOf(jobid)).intValue();
        }
        return 0;
    }

    public String prepareSkillQuery(int id) {
        StringBuilder query = new StringBuilder(INSERT_POINTS_QUERY);
        query.append(id).append(", ").append(this.skillPoints.get(MapleJob.EVAN2.getId()));
        for (int i = 2210; i < 2219; i++) {
            query.append(", ").append(this.skillPoints.get(i));
        }
        query.append(")");
        if (ServerEnvironment.isDebugEnabled()) {
            log.info("Saving evanskill points: " + query);
        }
        return query.toString();
    }

    public void setSkillPoints(int job, int points) {
        if (this.skillPoints.containsKey(job)) {
            this.skillPoints.put(job, points);
        }
    }

    @Override
    public String toString() {
        return "EvanSkillPoints [skillPoints=" + skillPoints + "]";
    }
}
