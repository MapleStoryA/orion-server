package server.gachapon;

import server.config.ServerEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

public class SimpleGachaRandomizer implements RewardRandomizer {

    private final List<? extends AbstractRandomEntity> rewards;

    public SimpleGachaRandomizer(List<? extends AbstractRandomEntity> rewards) {
        this.rewards = rewards;
    }

    @Override
    public AbstractRandomEntity next() {
        Map<Double, List<AbstractRandomEntity>> r = new TreeMap<>();
        for (AbstractRandomEntity d : rewards) {

            Double chance = d.getChance() / 1000;
            if (!r.containsKey(chance)) {
                r.put(chance, new ArrayList<>());
            }
            if (ServerEnvironment.isDebugEnabled()) {
                System.out.println("Item: " + d.getId() + " c: " + chance);
            }
            r.get(chance).add(d);
        }
        double random = 0.01 + (100 - 0.01) * new Random().nextDouble();
        if (ServerEnvironment.isDebugEnabled()) {
            System.out.println("Random chance: " + random);
        }
        int count = 0;
        for (Entry<Double, List<AbstractRandomEntity>> option : r.entrySet()) {
            if (ServerEnvironment.isDebugEnabled()) {
                System.out.println("Item Chance: " + option.getKey() + "");
            }
            count++;
            if (option.getKey() >= random || count == r.size()) {
                List<AbstractRandomEntity> gacha = option.getValue();
                Collections.shuffle(gacha);
                if (ServerEnvironment.isDebugEnabled()) {
                    System.out.println("Choosen one: " + option.getKey() + "");
                }
                AbstractRandomEntity item = gacha.get(0);

                return item;
            }
        }

        return null;
    }

}
