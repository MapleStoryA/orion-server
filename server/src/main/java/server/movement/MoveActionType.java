package server.movement;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 13, 2017
 *        Shit is from eric
 */
public enum MoveActionType{
	Walk(1),
	Move(1),
	Stand(2),
	Jump(3),
	Alert(4),
	Prone(5),
	Fly1(6),
	Ladder(7),
	Rope(8),
	Dead(9),
	Sit(0xA),
	Stand0(0xB),
	Hungry(0xC),
	Rest0(0xD),
	Rest1(0xE),
	Hang(0xF),
	Chase(0x10),
	Fly2(0x11),
	Fly2_Move(0x12),
	Dash2(0x13),
	Rocket_Booster(0x14),
	Tesla_Coil_Triangle(0x15),
	BackWalk(0x16),
	BladeStance(0x17),
	FeverMode(0x18),
	NO(0x19);

	private int id;

	MoveActionType(int id){
		this.id = id;
	}

	public int getID(){
		return id;
	}
}