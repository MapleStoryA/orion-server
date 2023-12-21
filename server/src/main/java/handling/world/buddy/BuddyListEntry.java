package handling.world.buddy;

@lombok.extern.slf4j.Slf4j
public class BuddyListEntry {

    private final String name;
    private final int cid;
    private String group;
    private int channel;

    public BuddyListEntry(final String name, final int characterId, final String group, final int channel) {
        this.name = name;
        this.cid = characterId;
        this.group = group;
        this.channel = channel;
    }

    public final String getName() {
        return name;
    }

    public final String getGroup() {
        return group;
    }

    public final void setGroup(final String g) {
        this.group = g;
    }

    public final int getCharacterId() {
        return cid;
    }

    public final int getChannel() {
        return channel;
    }

    public final void setChannel(final int channel) {
        this.channel = channel;
    }
}
