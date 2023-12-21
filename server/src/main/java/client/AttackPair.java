package client;

import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.collection.Pair;

@Slf4j
@Getter
public record AttackPair(int objectId, List<Pair<Integer, Boolean>> attack) {}
