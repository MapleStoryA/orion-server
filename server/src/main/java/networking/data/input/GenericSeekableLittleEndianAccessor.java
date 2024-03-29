package networking.data.input;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericSeekableLittleEndianAccessor extends GenericLittleEndianAccessor implements InPacket {

    private final SeekableInputStreamBytestream bs;

    /**
     * Class constructor Provide a seekable input stream to wrap this object around.
     *
     * @param bs The byte stream to wrap this around.
     */
    public GenericSeekableLittleEndianAccessor(final SeekableInputStreamBytestream bs) {
        super(bs);
        this.bs = bs;
    }

    /**
     * Seek the pointer to <code>offset</code>
     *
     * @param offset The offset to seek to.
     * @see net.sf.odinms.tools.data.input.SeekableInputStreamBytestream#seek
     */
    @Override
    public final void seek(final long offset) {
        try {
            bs.seek(offset);
        } catch (IOException e) {
            System.err.println("Seek failed" + e);
        }
    }

    /**
     * Get the current position of the pointer.
     *
     * @return The current position of the pointer as a long integer.
     * @see net.sf.odinms.tools.data.input.SeekableInputStreamBytestream#getPosition
     */
    @Override
    public final long getPosition() {
        try {
            return bs.getPosition();
        } catch (IOException e) {
            System.err.println("getPosition failed" + e);
            return -1;
        }
    }

    /**
     * Skip <code>num</code> number of bytes in the stream.
     *
     * @param num The number of bytes to skip.
     */
    @Override
    public final void skip(final int num) {
        seek(getPosition() + num);
    }
}
