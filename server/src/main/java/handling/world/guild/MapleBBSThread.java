package handling.world.guild;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapleBBSThread implements java.io.Serializable {

    public static final long serialVersionUID = 3565477792085301248L;
    public String name, text;
    public long timestamp;
    public int localthreadID, guildID, ownerID, icon;
    public Map<Integer, MapleBBSReply> replies = new HashMap<>();

    public MapleBBSThread(
            final int localthreadID,
            final String name,
            final String text,
            final long timestamp,
            final int guildID,
            final int ownerID,
            final int icon) {
        this.localthreadID = localthreadID;
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.guildID = guildID;
        this.ownerID = ownerID;
        this.icon = icon;
    }

    public final int getReplyCount() {
        return replies.size();
    }

    public final boolean isNotice() {
        return localthreadID == 0;
    }

    public static class MapleBBSReply implements java.io.Serializable {

        /** */
        private static final long serialVersionUID = 1L;

        public int replyid, ownerID;
        public long timestamp;
        public String content;

        public MapleBBSReply(final int replyid, final int ownerID, final String content, final long timestamp) {
            this.ownerID = ownerID;
            this.replyid = replyid;
            this.content = content;
            this.timestamp = timestamp;
        }
    }

    public static class ThreadComparator implements Comparator<MapleBBSThread>, java.io.Serializable {

        /** */
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(MapleBBSThread o1, MapleBBSThread o2) {
            if (o1.localthreadID < o2.localthreadID) {
                return 1;
            } else if (o1.localthreadID == o2.localthreadID) {
                return 0;
            } else {
                return -1; // opposite here as oldest is last, newest is first
            }
        }
    }
}
