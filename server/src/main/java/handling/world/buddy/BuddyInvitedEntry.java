package handling.world.buddy;

import java.util.Objects;

@lombok.extern.slf4j.Slf4j
public class BuddyInvitedEntry {

    public String name;
    public int inviter;
    public long expiration;

    public BuddyInvitedEntry(final String n, final int inviterid) {
        name = n.toLowerCase();
        inviter = inviterid;
        expiration = System.currentTimeMillis() + 10 * 60 * 1000; // 10 minutes expiration
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof BuddyInvitedEntry oth)) {
            return false;
        }
        return (inviter == oth.inviter && name.equals(oth.name));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + this.inviter;
        hash = 97 * hash + (int) (this.expiration ^ (this.expiration >>> 32));
        return hash;
    }
}
