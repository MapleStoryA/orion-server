package tools.packet;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;

@Slf4j
public class ScriptMan {
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
    private static final short ScriptMessage = 0x163;

    public static byte[] OnSay(
            int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText, boolean bPrev, boolean bNext) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.Say.getMsgType());
        packet.write(bParam);
        if ((bParam & 0x4) > 0) // idek xd, its a 2nd template for something
        {
            packet.writeInt(nSpeakerTemplateID);
        }
        packet.writeMapleAsciiString(sText);
        packet.writeBool(bPrev);
        packet.writeBool(bNext);
        return packet.getPacket();
    }

    public static byte[] OnSayImage(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, List<String> asPath) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.SayImage.getMsgType());
        packet.write(bParam);
        packet.write(asPath.size());
        for (String sPath : asPath) {
            packet.writeMapleAsciiString(sPath); // CUtilDlgEx::AddImageList(v8, sPath);
        }
        return packet.getPacket();
    }

    public static byte[] OnAskYesNo(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskYesNo.getMsgType());
        packet.write(bParam); // (bParam & 0x6)
        packet.writeMapleAsciiString(sText);
        return packet.getPacket();
    }

    public static byte[] OnAskAccept(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskAccept.getMsgType());
        packet.write(bParam);
        packet.writeMapleAsciiString(sText);
        return packet.getPacket();
    }

    public static byte[] OnAskText(
            int nSpeakerTypeID,
            int nSpeakerTemplateID,
            byte bParam,
            String sMsg,
            String sMsgDefault,
            int nLenMin,
            int nLenMax) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskText.getMsgType());
        packet.write(bParam); // (bParam & 0x6)
        packet.writeMapleAsciiString(sMsg);
        packet.writeMapleAsciiString(sMsgDefault);
        packet.writeShort(nLenMin);
        packet.writeShort(nLenMax);
        return packet.getPacket();
    }

    public static byte[] OnAskBoxText(
            int nSpeakerTypeID,
            int nSpeakerTemplateID,
            byte bParam,
            String sMsg,
            String sMsgDefault,
            int nCol,
            int nLine) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskBoxText.getMsgType());
        packet.write(bParam); // (bParam & 0x6)
        packet.writeMapleAsciiString(sMsg);
        packet.writeMapleAsciiString(sMsgDefault);
        packet.writeShort(nCol);
        packet.writeShort(nLine);
        return packet.getPacket();
    }

    public static byte[] OnAskNumber(
            int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg, int nDef, int nMin, int nMax) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskNumber.getMsgType());
        packet.write(bParam); // (bParam & 0x6)
        packet.writeMapleAsciiString(sMsg);
        packet.writeInt(nDef);
        packet.writeInt(nMin);
        packet.writeInt(nMax);
        return packet.getPacket();
    }

    public static byte[] OnAskMenu(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskMenu.getMsgType());
        packet.write(bParam); // (bParam & 0x6)
        packet.writeMapleAsciiString(sMsg);
        return packet.getPacket();
    }

    public static byte[] OnAskAvatar(int nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, int[] anCanadite) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskAvatar.getMsgType());
        packet.write(0);
        packet.writeMapleAsciiString(sMsg);
        packet.write(anCanadite.length);
        for (int nCanadite : anCanadite) {
            packet.writeInt(nCanadite); // hair id's and stuff lol
        }
        return packet.getPacket();
    }

    public static byte[] OnAskMembershopAvatar(
            int nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, int[] aCanadite) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskMemberShopAvatar.getMsgType());
        packet.write(0);
        packet.writeMapleAsciiString(sMsg);
        packet.write(aCanadite.length);
        for (int nCanadite : aCanadite) {
            packet.writeInt(nCanadite); // hair id's and stuff lol
        }
        return packet.getPacket();
    }

    public static byte[] OnAskQuiz(
            int nSpeakerTypeID,
            int nSpeakerTemplateID,
            int nResCode,
            String sTitle,
            String sProblemText,
            String sHintText,
            int nMinInput,
            int nMaxInput,
            int tRemainInitialQuiz) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskQuiz.getMsgType());
        packet.write(0);
        packet.write(nResCode);
        if (nResCode == InitialQuizRes_Request) { // fail has no bytes <3
            packet.writeMapleAsciiString(sTitle);
            packet.writeMapleAsciiString(sProblemText);
            packet.writeMapleAsciiString(sHintText);
            packet.writeShort(nMinInput);
            packet.writeShort(nMaxInput);
            packet.writeInt(tRemainInitialQuiz);
        }
        return packet.getPacket();
    }

    public static byte[] OnAskSpeedQuiz(
            int nSpeakerTypeID,
            int nSpeakerTemplateID,
            int nResCode,
            int nType,
            int dwAnswer,
            int nCorrect,
            int nRemain,
            int tRemainInitialQuiz) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskSpeedQuiz.getMsgType());
        packet.write(0);
        packet.write(nResCode);
        if (nResCode == InitialQuizRes_Request) { // fail has no bytes <3
            packet.writeInt(nType);
            packet.writeInt(dwAnswer);
            packet.writeInt(nCorrect);
            packet.writeInt(nRemain);
            packet.writeInt(tRemainInitialQuiz);
        }
        return packet.getPacket();
    }

    public static byte[] OnAskSlideMenu(
            int nSpeakerTypeID, int nSpeakerTemplateID, boolean bSlideDlgEX, int nIndex, String sMsg) {
        OutPacket packet = new OutPacket();
        packet.writeShort(ScriptMessage);
        packet.write(nSpeakerTypeID);
        packet.writeInt(nSpeakerTemplateID);
        packet.write(ScriptMessageType.AskSlideMenu.getMsgType());
        packet.write(0);
        packet.writeInt(bSlideDlgEX ? 1 : 0); // Neo City
        packet.writeInt(nIndex); // Dimensional Mirror.. There's also supportF for potions and such in
        // higher versions.
        packet.writeMapleAsciiString(sMsg);
        return packet.getPacket();
    }

    private enum ScriptMessageType {
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

        ScriptMessageType(int nMsgType) {
            this.nMsgType = nMsgType;
        }

        public int getMsgType() {
            return nMsgType;
        }
    }
}
