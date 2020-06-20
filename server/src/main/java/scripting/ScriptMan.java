package scripting;

import tools.data.output.MaplePacketLittleEndianWriter;

import java.util.List;

public class ScriptMan {
  private static final short ScriptMessage = 0x163;

  private static enum ScriptMessageType {
    Say(0x0),
    SayImage(0x1),
    AskYesNo(0x2),
    AskText(0x3),
    AskNumber(0x4),
    AskMenu(0x5),
    AskQuiz(0x6),
    AskSpeedQuiz(0x7),
    AskAvatar(0x8),
    AskMemberShopAvatar(0x9),
    AskPet(0xA),
    AskPetAll(0xB),
    AskScript(0xC),
    AskAccept(0xD),
    AskBoxText(0xE),
    AskSlideMenu(0xF),
    AskCenter(0x10);
    private final int nMsgType;

    private ScriptMessageType(int nMsgType) {
      this.nMsgType = nMsgType;
    }

    public int getMsgType() {
      return nMsgType;
    }
  }

  // InitialQuiz
  public static final int InitialQuizRes_Request = 0x0;
  public static final int InitialQuizRes_Fail = 0x1;
  // InitialSpeedQuiz
  public static final int TypeSpeedQuizNpc = 0x0;
  public static final int TypeSpeedQuizMob = 0x1;
  public static final int TypeSpeedQuizItem = 0x2;
  // SpeakerTypeID
  public static final int NoESC = 0x1;
  public static final int NpcReplacedByUser = 0x2;
  public static final int NpcReplayedByNpc = 0x4;
  public static final int FlipImage = 0x8;

  public static byte[] OnSay(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText, boolean bPrev, boolean bNext) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.Say.getMsgType());
    mplew.write(bParam);
    if ((bParam & 0x4) > 0)//idek xd, its a 2nd template for something
    {
      mplew.writeInt(nSpeakerTemplateID);
    }
    mplew.writeMapleAsciiString(sText);
    mplew.writeBool(bPrev);
    mplew.writeBool(bNext);
    return mplew.getPacket();
  }

  public static byte[] OnSayImage(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, List<String> asPath) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.SayImage.getMsgType());
    mplew.write(bParam);
    mplew.write(asPath.size());
    for (String sPath : asPath) {
      mplew.writeMapleAsciiString(sPath);//CUtilDlgEx::AddImageList(v8, sPath);
    }
    return mplew.getPacket();
  }

  public static byte[] OnAskYesNo(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskYesNo.getMsgType());
    mplew.write(bParam);//(bParam & 0x6)
    mplew.writeMapleAsciiString(sText);
    return mplew.getPacket();
  }

  public static byte[] OnAskAccept(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskAccept.getMsgType());
    mplew.write(bParam);
    mplew.writeMapleAsciiString(sText);
    return mplew.getPacket();
  }

  public static byte[] OnAskText(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg, String sMsgDefault, int nLenMin, int nLenMax) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskText.getMsgType());
    mplew.write(bParam);//(bParam & 0x6)
    mplew.writeMapleAsciiString(sMsg);
    mplew.writeMapleAsciiString(sMsgDefault);
    mplew.writeShort(nLenMin);
    mplew.writeShort(nLenMax);
    return mplew.getPacket();
  }

  public static byte[] OnAskBoxText(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg, String sMsgDefault, int nCol, int nLine) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskBoxText.getMsgType());
    mplew.write(bParam);//(bParam & 0x6)
    mplew.writeMapleAsciiString(sMsg);
    mplew.writeMapleAsciiString(sMsgDefault);
    mplew.writeShort(nCol);
    mplew.writeShort(nLine);
    return mplew.getPacket();
  }

  public static byte[] OnAskNumber(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg, int nDef, int nMin, int nMax) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskNumber.getMsgType());
    mplew.write(bParam);//(bParam & 0x6)
    mplew.writeMapleAsciiString(sMsg);
    mplew.writeInt(nDef);
    mplew.writeInt(nMin);
    mplew.writeInt(nMax);
    return mplew.getPacket();
  }

  public static byte[] OnAskMenu(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskMenu.getMsgType());
    mplew.write(bParam);//(bParam & 0x6)
    mplew.writeMapleAsciiString(sMsg);
    return mplew.getPacket();
  }

  public static byte[] OnAskAvatar(int nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, int[] anCanadite) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskAvatar.getMsgType());
    mplew.write(0);
    mplew.writeMapleAsciiString(sMsg);
    mplew.write(anCanadite.length);
    for (int nCanadite : anCanadite) {
      mplew.writeInt(nCanadite);//hair id's and stuff lol
    }
    return mplew.getPacket();
  }

  public static byte[] OnAskMembershopAvatar(int nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, int[] aCanadite) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskMemberShopAvatar.getMsgType());
    mplew.write(0);
    mplew.writeMapleAsciiString(sMsg);
    mplew.write(aCanadite.length);
    for (int nCanadite : aCanadite) {
      mplew.writeInt(nCanadite);//hair id's and stuff lol
    }
    return mplew.getPacket();
  }


  public static byte[] OnAskQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, String sTitle, String sProblemText, String sHintText, int nMinInput, int nMaxInput, int tRemainInitialQuiz) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskQuiz.getMsgType());
    mplew.write(0);
    mplew.write(nResCode);
    if (nResCode == InitialQuizRes_Request) {//fail has no bytes <3
      mplew.writeMapleAsciiString(sTitle);
      mplew.writeMapleAsciiString(sProblemText);
      mplew.writeMapleAsciiString(sHintText);
      mplew.writeShort(nMinInput);
      mplew.writeShort(nMaxInput);
      mplew.writeInt(tRemainInitialQuiz);
    }
    return mplew.getPacket();
  }

  public static byte[] OnAskSpeedQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, int nType, int dwAnswer, int nCorrect, int nRemain, int tRemainInitialQuiz) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskSpeedQuiz.getMsgType());
    mplew.write(0);
    mplew.write(nResCode);
    if (nResCode == InitialQuizRes_Request) {//fail has no bytes <3
      mplew.writeInt(nType);
      mplew.writeInt(dwAnswer);
      mplew.writeInt(nCorrect);
      mplew.writeInt(nRemain);
      mplew.writeInt(tRemainInitialQuiz);
    }
    return mplew.getPacket();
  }

  public static byte[] OnAskSlideMenu(int nSpeakerTypeID, int nSpeakerTemplateID, boolean bSlideDlgEX, int nIndex, String sMsg) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(ScriptMessage);
    mplew.write(nSpeakerTypeID);
    mplew.writeInt(nSpeakerTemplateID);
    mplew.write(ScriptMessageType.AskSlideMenu.getMsgType());
    mplew.write(0);
    mplew.writeInt(bSlideDlgEX ? 1 : 0);//Neo City
    mplew.writeInt(nIndex);//Dimensional Mirror.. There's also supportF for potions and such in higher versions.
    mplew.writeMapleAsciiString(sMsg);
    return mplew.getPacket();
  }
}  
