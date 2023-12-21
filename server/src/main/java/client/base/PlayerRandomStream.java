package client.base;

import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;
import tools.helper.Randomizer;

@Slf4j
public class PlayerRandomStream {

    private transient long seed1, seed2, seed3;

    public PlayerRandomStream() {
        final int v4 = 5;
        this.CRand32__Seed(Randomizer.nextLong(), 1170746341L * v4 - 755606699, 1170746341L * v4 - 755606699);
    }

    public final void CRand32__Seed(final long s1, final long s2, final long s3) {
        seed1 = s1 | 0x100000;
        seed2 = s2 | 0x1000;
        seed3 = s3 | 0x10;
    }

    public final long CRand32__Random() {
        long v4 = this.seed1;
        long v5 = this.seed2;
        long v6 = this.seed3;
        long v7 = this.seed1;

        long v8 = ((v4 & 0xFFFFFFFEL) << 12) ^ ((v7 & 0x7FFC0 ^ (v4 >> 13)) >> 6);
        long v9 = 16 * (v5 & 0xFFFFFFF8L) ^ (((v5 >> 2) ^ v5 & 0x3F800000) >> 23);
        long v10 = ((v6 & 0xFFFFFFF0L) << 17) ^ (((v6 >> 3) ^ v6 & 0x1FFFFF00) >> 8);
        return (v8 ^ v9 ^ v10) & 0xffffffffL;
    }

    public final void connectData(final OutPacket packet) {
        long v5 = CRand32__Random();
        long s2 = CRand32__Random();
        long v6 = CRand32__Random();

        CRand32__Seed(v5, s2, v6);

        packet.writeInt((int) v5);
        packet.writeInt((int) s2);
        packet.writeInt((int) v6);
    }
}
