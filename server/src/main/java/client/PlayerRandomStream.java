package client;

import networking.data.output.OutPacket;
import tools.helper.Randomizer;

@lombok.extern.slf4j.Slf4j
public class PlayerRandomStream {

    private transient long seed1, seed2, seed3;
    private transient long seed1_, seed2_, seed3_;
    private transient long seed1__, seed2__, seed3__;
    private transient long seed1___, seed2___, seed3___;
    //    private transient int past_s1 = -1, past_s2 = -1, past_s3 = -1;

    public PlayerRandomStream() {
        final int v4 = 5;
        this.CRand32__Seed(Randomizer.nextLong(), 1170746341L * v4 - 755606699, 1170746341L * v4 - 755606699);
    }

    public final void CRand32__Seed(final long s1, final long s2, final long s3) {
        seed1 = s1 | 0x100000;
        seed2 = s2 | 0x1000;
        seed3 = s3 | 0x10;

        seed1_ = s1 | 0x100000;
        seed2_ = s2 | 0x1000;
        seed3_ = s3 | 0x10;

        seed1__ = s1 | 0x100000;
        seed2__ = s2 | 0x1000;
        seed3__ = s3 | 0x10;
    }

    public final long CRand32__Random() {
        long v4 = this.seed1;
        long v5 = this.seed2;
        long v6 = this.seed3;
        long v7 = this.seed1;

        long v8 = ((v4 & 0xFFFFFFFE) << 12) ^ ((v7 & 0x7FFC0 ^ (v4 >> 13)) >> 6);
        long v9 = 16 * (v5 & 0xFFFFFFF8) ^ (((v5 >> 2) ^ v5 & 0x3F800000) >> 23);
        long v10 = ((v6 & 0xFFFFFFF0) << 17) ^ (((v6 >> 3) ^ v6 & 0x1FFFFF00) >> 8);
        this.seed3_ = v10 & 0xffffffffL;
        this.seed1_ = v8 & 0xffffffffL;
        this.seed2_ = v9 & 0xffffffffL;
        return (v8 ^ v9 ^ v10) & 0xffffffffL; // to be confirmed, I am not experienced in converting signed >
        // unsigned
    }

    public final long CRand32__Random_Character() {
        long v4 = this.seed1_;
        long v5 = this.seed2_;
        long v6 = this.seed3_;
        long v7 = this.seed1_;

        long v8 = ((v4 & 0xFFFFFFFE) << 12) ^ ((v7 & 0x7FFC0 ^ (v4 >> 13)) >> 6);
        long v9 = 16 * (v5 & 0xFFFFFFF8) ^ (((v5 >> 2) ^ v5 & 0x3F800000) >> 23);
        long v10 = ((v6 & 0xFFFFFFF0) << 17) ^ (((v6 >> 3) ^ v6 & 0x1FFFFF00) >> 8);
        this.seed3_ = v10 & 0xffffffffL;
        this.seed1_ = v8 & 0xffffffffL;
        this.seed2_ = v9 & 0xffffffffL;
        return (v8 ^ v9 ^ v10) & 0xffffffffL;
    }

    public final long CRand32__Random_CheckDamageMiss() {
        long v4 = this.seed1__;
        long v5 = this.seed2__;
        long v6 = this.seed3__;
        long v7 = this.seed1__;

        long v8 = ((v4 & 0xFFFFFFFE) << 12) ^ ((v7 & 0x7FFC0 ^ (v4 >> 13)) >> 6);
        long v9 = 16 * (v5 & 0xFFFFFFF8) ^ (((v5 >> 2) ^ v5 & 0x3F800000) >> 23);
        long v10 = ((v6 & 0xFFFFFFF0) << 17) ^ (((v6 >> 3) ^ v6 & 0x1FFFFF00) >> 8);
        this.seed3_ = v10 & 0xffffffffL;
        this.seed1_ = v8 & 0xffffffffL;
        this.seed2_ = v9 & 0xffffffffL;
        return (v8 ^ v9 ^ v10) & 0xffffffffL;
    }

    public final long CRand32__Random_ForMonster() {
        long v4 = this.seed1___;
        long v5 = this.seed2___;
        long v6 = this.seed3___;
        long v7 = this.seed1___;

        long v8 = ((v4 & 0xFFFFFFFE) << 12) ^ ((v7 & 0x7FFC0 ^ (v4 >> 13)) >> 6);
        long v9 = 16 * (v5 & 0xFFFFFFF8) ^ (((v5 >> 2) ^ v5 & 0x3F800000) >> 23);
        long v10 = ((v6 & 0xFFFFFFF0) << 17) ^ (((v6 >> 3) ^ v6 & 0x1FFFFF00) >> 8);
        this.seed3_ = v10 & 0xffffffffL;
        this.seed1_ = v8 & 0xffffffffL;
        this.seed2_ = v9 & 0xffffffffL;
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
