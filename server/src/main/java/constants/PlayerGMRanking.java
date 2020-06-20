package constants;


public enum PlayerGMRanking {
  NORMAL(0, 0),
  ELITE(1, 0),
  INTERN(2, 0),
  GM(3, 0x40),
  SUPERGM(4, 0x40),
  ADMIN(5, 0x80),
  CONTROLLER(6, 0x80);

  /*
   SubGradeCodes:
      PrimaryTrace(0x1),
      SecondaryTrace(0x2),
      AdminClient(0x4),
      MobMoveObserve(0x8),
      ManagerAccount(0x10),
      OutSourceSuperGM(0x20),
      OutSourceGM(0x40),
      UserGM(0x80),
      TesterAccount(0x100),
  */
  // isAdminAccount: m_nGradeCode & 1 || m_bManagerAccount
  // isSubGMAccount: m_nSubGradeCode & 0x20 || m_nSubGradeCode & 0x40 || m_nSubGradeCode < 0
  // isUserGM: m_nSubGradeCode >> 7
  // isTradeBlockedUser: m_nGradeCode & 1 || m_bManagerAccount || m_bTesterAccount || m_nGradeCode & 0x10
  // isTesterAccount: m_bTesterAccount || m_bManagerAccount
	/* AdminLevel:
	 	0: m_nGradeCode & 0x20
		1: m_nGradeCode & 1 || m_bManagerAccount
		2: m_nSubGradeCode & 0x20
		3: m_nSubGradeCode & 0x40
		else return: (m_nSubGradeCode ^ 0x80) >> 7 | 4
	*/
  private int level;
  private byte subGrade;

  private PlayerGMRanking(int level, int subGrade) {
    this.level = level;
    this.subGrade = (byte) subGrade;
  }

  public int getLevel() {
    return level;
  }

  public byte getSubGrade() {
    return subGrade;
  }

  public static PlayerGMRanking getByLevel(int level) {
    for (PlayerGMRanking pgr : values()) {
      if (pgr.getLevel() == level) {
        return pgr;
      }
    }
    return NORMAL;
  }
}
