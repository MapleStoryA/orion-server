package client.state;

import client.MapleStat;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class TempAvatarStats2 {

    private Map<MapleStat, Integer> stats = new HashMap<>();


    void set(MapleStat stat, Integer value) {
        this.stats.put(stat, value);
    }

    void get(MapleStat stat) {
        this.get(stat);
    }
}
