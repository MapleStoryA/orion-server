package server;

import constants.GameConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomRewards {

  private final static RandomRewards instance = new RandomRewards();
  private List<Integer> compiledGold = null;
  private List<Integer> compiledSilver = null;
  private List<Integer> compiledFishing = null;
  private List<Integer> compiledEvent = null;
  private List<Integer> compiledEventC = null;
  private List<Integer> compiledEventB = null;
  private List<Integer> compiledEventA = null;
  private List<Integer> compiledCashSurprise = null;


  public static RandomRewards getInstance() {
    return instance;
  }

  protected RandomRewards() {
    // Gold Box
    List<Integer> returnArray = new ArrayList<Integer>();

    processRewards(returnArray, GameConstants.goldrewards);

    compiledGold = returnArray;

    // Silver Box
    returnArray = new ArrayList<Integer>();

    processRewards(returnArray, GameConstants.silverrewards);

    compiledSilver = returnArray;

    // Fishing Rewards
    returnArray = new ArrayList<Integer>();

    processRewards(returnArray, GameConstants.fishingReward);

    compiledFishing = returnArray;

    // Cash Shop Surprise Rewards
    returnArray = new ArrayList<Integer>();

    processRewards(returnArray, GameConstants.cashSurpriseRewards);

    compiledCashSurprise = returnArray;

    // Event Rewards
    returnArray = new ArrayList<Integer>();

    processRewards(returnArray, GameConstants.eventCommonReward);

    compiledEventC = returnArray;

    returnArray = new ArrayList<Integer>();

    processRewards(returnArray, GameConstants.eventUncommonReward);

    compiledEventB = returnArray;

    returnArray = new ArrayList<Integer>();

    processRewards(returnArray, GameConstants.eventRareReward);

    compiledEventA = returnArray;

    returnArray = new ArrayList<Integer>();

    processRewards(returnArray, GameConstants.eventSuperReward);

    compiledEvent = returnArray;
  }

  private final void processRewards(final List<Integer> returnArray, final int[] list) {
    int lastitem = 0;
    for (int i = 0; i < list.length; i++) {
      if (i % 2 == 0) { // Even
        lastitem = list[i];
      } else { // Odd
        for (int j = 0; j < list[i]; j++) {
          returnArray.add(lastitem);
        }
      }
    }
    Collections.shuffle(returnArray);
  }

  public final int getGoldBoxReward() {
    return compiledGold.get(Randomizer.nextInt(compiledGold.size()));
  }

  public final int getSilverBoxReward() {
    return compiledSilver.get(Randomizer.nextInt(compiledSilver.size()));
  }

  public final int getFishingReward() {
    return compiledFishing.get(Randomizer.nextInt(compiledFishing.size()));
  }

  public final int getCSSReward() {
    int ret = compiledCashSurprise.get(Randomizer.nextInt(compiledCashSurprise.size()));

    return ret;
  }

  public final int getEventReward() {
    final int chance = Randomizer.nextInt(100);
    if (chance < 50) {
      return compiledEventC.get(Randomizer.nextInt(compiledEventC.size()));
    } else if (chance < 80) {
      return compiledEventB.get(Randomizer.nextInt(compiledEventB.size()));
    } else if (chance < 95) {
      return compiledEventA.get(Randomizer.nextInt(compiledEventA.size()));
    } else {
      return compiledEvent.get(Randomizer.nextInt(compiledEvent.size()));
    }
  }
}
