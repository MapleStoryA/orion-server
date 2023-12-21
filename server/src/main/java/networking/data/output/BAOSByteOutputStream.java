package networking.data.output;

import java.io.ByteArrayOutputStream;

/**
 * Uses a byte array to output a stream of bytes.
 */
@lombok.extern.slf4j.Slf4j
public class BAOSByteOutputStream implements ByteOutputStream {

    private final ByteArrayOutputStream baos;

    /**
     * Class constructor - Wraps the stream around a Java BAOS.
     *
     * @param baos <code>The ByteArrayOutputStream</code> to wrap this around.
     */
    public BAOSByteOutputStream(final ByteArrayOutputStream baos) {
        super();
        this.baos = baos;
    }

    /**
     * Writes a byte to the stream.
     *
     * @param b The byte to write to the stream.
     * @see net.sf.odinms.tools.data.output.ByteOutputStream#writeByte(byte)
     */
    @Override
    public void writeByte(final byte b) {
        baos.write(b);
    }
}
