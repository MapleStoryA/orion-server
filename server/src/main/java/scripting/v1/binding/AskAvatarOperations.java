package scripting.v1.binding;

import client.MapleCharacter;
import client.MapleStat;
import constants.GameConstants;
import server.MapleInventoryManipulator;

import java.util.Random;

public class AskAvatarOperations {

  public static boolean IsValidHairID(int nID, int nGender) {
    int v3; // esi@1
    v3 = nID / 1000 % 10;
    return nID / 10000 == 3 && (v3 == 2 || nGender == 2 || nGender == v3);
  }

  public static boolean IsValidFaceID(int nID, int nGender) {
    int v3; // esi@1
    v3 = nID / 1000 % 10;
    return nID / 10000 == 2 && (v3 == 2 || nGender == 2 || nGender == v3);
  }

  public static int processAskAvatar(MapleCharacter player, int option, boolean isRandom) {
    int[] faces = (int[]) player.getTemporaryData("askAvatar");
    int nCouponItemID = (int) player.getTemporaryData("askAvatarItem");
    player.removeTemporaryData("askAvatar");
    player.removeTemporaryData("askAvatarItem");

    if (isRandom) {
      option = new Random().nextInt(faces.length - 1);
    }

    if (faces == null || option > faces.length) {
      return -3;
    }
    if (!player.haveItem(nCouponItemID)) {
      return -1;
    }
    if (nCouponItemID / 1000000 != 5) {
      return -1;
    }

    if (IsValidFaceID(faces[option], player.getGender())) {
      player.setFace(faces[option]);
      player.updateSingleStat(MapleStat.FACE, faces[option]);
    } else if (IsValidHairID(faces[option], player.getGender())) {
      player.setHair(faces[option]);
      player.updateSingleStat(MapleStat.HAIR, faces[option]);
    }
    MapleInventoryManipulator.removeById(player.getClient(), GameConstants.getInventoryType(nCouponItemID), nCouponItemID, 1, true, false);

    player.equipChanged();
    return 1;
  }
}
