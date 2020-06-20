package server.movement;

import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 22, 2017
 */
public class Elem{

	byte type;
	public byte bMoveAction, bStat;
	public short x, y, vx, vy, fh, fhFallStart, xOffset, yOffset;
	public short tElapse;

	public void decode(SeekableLittleEndianAccessor lea){
		type = lea.readByte();// nAttr
		// label_12, encode2, goto label_13
		// label_13, write, encode 2
		switch (type){
			case 0:
			case 5:
			case 12:
			case 14:
			case 35:
			case 36:
				x = lea.readShort();
				y = lea.readShort();
				vx = lea.readShort();
				vy = lea.readShort();
				fh = lea.readShort();
				if(type == 12){
					fhFallStart = lea.readShort();
				}
				xOffset = lea.readShort();
				yOffset = lea.readShort();
				bMoveAction = lea.readByte();
				tElapse = lea.readShort();
				break;
			case 3:
			case 4:
			case 6:
			case 7:
			case 8:
			case 10:
				x = lea.readShort();
				y = lea.readShort();
				fh = lea.readShort();
				bMoveAction = lea.readByte();
				tElapse = lea.readShort();
				break;
			case 11:
				vx = lea.readShort();
				vy = lea.readShort();
				fhFallStart = lea.readShort();
				bMoveAction = lea.readByte();
				tElapse = lea.readShort();
				break;
			case 17:
				x = lea.readShort();
				y = lea.readShort();
				vx = lea.readShort();
				vy = lea.readShort();
				bMoveAction = lea.readByte();
				tElapse = lea.readShort();
				break;
			case 1:
			case 2:
			case 13:
			case 16:
			case 18:
			case 31:
			case 32:
			case 33:
			case 34:
				vx = lea.readShort();
				// LABEL_23
				vy = lea.readShort();
				// LABEL_18
				bMoveAction = lea.readByte();
				tElapse = lea.readShort();
				break;
			case 9:
				bStat = lea.readByte();
				break;
			default:
				bMoveAction = lea.readByte();
				tElapse = lea.readShort();
				break;
		}
	}

	public void encode(LittleEndianWriter lew){
		lew.write(type);
		switch (type){
			case 0:
			case 5:
			case 12:
			case 14:
			case 35:
			case 36:
				lew.writeShort(x);
				lew.writeShort(y);
				lew.writeShort(vx);
				lew.writeShort(vy);
				lew.writeShort(fh);
				if(type == 12){
					lew.writeShort(fhFallStart);
				}
				lew.writeShort(xOffset);
				lew.writeShort(yOffset);
				lew.write(bMoveAction);
				lew.writeShort(tElapse);
				break;
			case 3:
			case 4:
			case 6:
			case 7:
			case 8:
			case 10:
				lew.writeShort(x);
				lew.writeShort(y);
				lew.writeShort(fh);
				lew.write(bMoveAction);
				lew.writeShort(tElapse);
				break;
			case 11:
				lew.writeShort(vx);
				lew.writeShort(vy);
				lew.writeShort(fhFallStart);
				lew.write(bMoveAction);
				lew.writeShort(tElapse);
				break;
			case 17:
				lew.writeShort(x);
				lew.writeShort(y);
				lew.writeShort(vx);
				lew.writeShort(vy);
				lew.write(bMoveAction);
				lew.writeShort(tElapse);
				break;
			case 1:
			case 2:
			case 13:
			case 16:
			case 18:
			case 31:
			case 32:
			case 33:
			case 34:
				lew.writeShort(vx);
				lew.writeShort(vy);
				lew.write(bMoveAction);
				lew.writeShort(tElapse);
				break;
			case 9:
				lew.write(bStat);
				break;
			default:
				lew.write(bMoveAction);
				lew.writeShort(tElapse);
				break;
		}
	}
}
