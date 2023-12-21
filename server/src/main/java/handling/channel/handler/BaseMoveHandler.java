package handling.channel.handler;

import java.awt.*;
import networking.packet.AbstractMaplePacketHandler;
import server.maps.AnimatedMapleMapObject;
import server.movement.Elem;
import server.movement.MovePath;

public abstract class BaseMoveHandler extends AbstractMaplePacketHandler {

    protected void updatePosition(MovePath path, AnimatedMapleMapObject target, int yoffset) {
        for (Elem elem : path.lElem) {
            if (elem.x != 0 && elem.y != 0) {
                target.setPosition(new Point(elem.x, elem.y));
            }
            target.setStance(elem.bMoveAction);
        }
    }
}
