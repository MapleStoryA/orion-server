package maplebr2.secure;

public class _ZtlSecureTear {


  public static Integer value(Integer t) {
    Integer random = 0x6F388FCE;
    Integer v5 = Integer.rotateRight(t ^ random, 5);
    Integer v6 = Integer.rotateRight(random ^ 0xBAADF00D, 5);
    return (v5 + v6);
  }

}
