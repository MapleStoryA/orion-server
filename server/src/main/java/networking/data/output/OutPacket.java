package networking.data.output;

import java.io.ByteArrayOutputStream;
import tools.helper.HexTool;

/**
 * Writes a maplestory-packet little-endian stream of bytes.
 */
@lombok.extern.slf4j.Slf4j
public class OutPacket extends GenericLittleEndianWriter {

    private final ByteArrayOutputStream baos;

    /** Constructor - initializes this stream with a default size. */
    public OutPacket() {
        this(32);
    }

    /**
     * Constructor - initializes this stream with size <code>size</code>.
     *
     * @param size The size of the underlying stream.
     */
    public OutPacket(final int size) {
        this.baos = new ByteArrayOutputStream(size);
        setByteOutputStream(new BAOSByteOutputStream(baos));
    }

    /**
     * Gets a <code>MaplePacket</code> instance representing this sequence of bytes.
     *
     * @return A <code>MaplePacket</code> with the bytes in this stream.
     */
    public final byte[] getPacket() {
        // MaplePacket packet = new ByteArrayMaplePacket(baos.toByteArray());
        // log.info("Packet to be sent:\n" +packet.toString());
        return baos.toByteArray();
    }

    /**
     * Changes this packet into a human-readable hexadecimal stream of bytes.
     *
     * @return This packet as hex digits.
     */
    @Override
    public final String toString() {
        return HexTool.toString(baos.toByteArray());
    }
}
