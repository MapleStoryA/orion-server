package server.movement;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import networking.data.input.InPacket;
import networking.data.output.LittleEndianWriter;

/**
 * @Author Arnah @Website http://Vertisy.ca/
 *
 * @since Jun 22, 2017
 */
@lombok.extern.slf4j.Slf4j
public class MovePath {

    public List<Elem> lElem = new LinkedList<Elem>();
    public Point startPosition, velocity;

    public void decode(InPacket lea) {
        startPosition = lea.readPos();
        velocity = lea.readPos();
        byte size = lea.readByte();
        for (int i = 0; i < size; i++) {
            Elem elem = new Elem();
            elem.decode(lea);
            lElem.add(elem);
        }
    }

    public void encode(LittleEndianWriter lew) {
        lew.writePos(startPosition);
        lew.writePos(velocity);
        lew.write(lElem.size());
        for (Elem elem : lElem) {
            elem.encode(lew);
        }
    }
}
