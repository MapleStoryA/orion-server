package server.movement;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 17, 2016
 */
public enum MovementState{
	DEFAULT("DEFAULT", 0),
	WALKING_RIGHT("WALKING_LEFT", 2),
	WALKING_LEFT("WALKING_RIGHT", 3),
	STAND_RIGHT("STAND_LEFT", 4),
	STAND_LEFT("STAND_RIGHT", 5),
	JUMP_RIGHT("JUMP_LEFT", 6),
	JUMP_LEFT("JUMP_RIGHT", 7),
	ALERT_RIGHT("ALERT_LEFT", 8),
	ALERT_LEFT("ALERT_RIGHT", 9),
	PRONE_RIGHT("PRONE_LEFT", 10),
	PRONE_LEFT("PRONE_RIGHT", 11),
	SWIM_RIGHT("SWIM_LEFT", 12),
	SWIM_LEFT("SWIM_RIGHT", 13),
	LADDER_LEFT("LADDER_RIGHT", 14),
	LADDER_RIGHT("LADDER_LEFT", 15),
	ROPE_RIGHT("ROPE_LEFT", 16),
	ROPE_LEFT("ROPE_RIGHT", 17),
	DEAD_RIGHT("DEAD_LEFT", 18),
	DEAD_LEFT("DEAD_RIGHT", 19),
	SIT_RIGHT("SIT_LEFT", 20),
	SIT_LEFT("SIT_RIGHT", 21);

	private String oppositeDirection;
	private int state;

	MovementState(String oppositeDirection, int state){
		this.oppositeDirection = oppositeDirection;
		this.state = state;
	}

	public MovementState getOppositeDirection(){
		return MovementState.valueOf(oppositeDirection);
	}

	public int getState(){
		return state;
	}

	public boolean isState(MovementState state){
		return isState(state.getState());
	}

	public boolean isState(int state){
		return this.state == state || getOppositeDirection().state == state;
	}

	public static MovementState getStateByID(int state){
		for(MovementState ms : values()){
			if(ms.state == state) return ms;
		}
		return DEFAULT;
	}
}
