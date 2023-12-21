package client;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import tools.collection.Pair;

@Slf4j
public record AttackPair(int objectId, List<Pair<Integer, Boolean>> attack) {}
