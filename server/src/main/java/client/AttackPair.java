package client;

import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.collection.Pair;

@Slf4j
@Getter
public class AttackPair {

    private final int objectId;
    private final List<Pair<Integer, Boolean>> attack;

    public AttackPair(int objectId, List<Pair<Integer, Boolean>> attack) {
        this.objectId = objectId;
        this.attack = attack;
    }
}
