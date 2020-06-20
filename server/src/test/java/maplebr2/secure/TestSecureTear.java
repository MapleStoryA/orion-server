package maplebr2.secure;

import org.junit.Test;

import java.math.BigInteger;

public class TestSecureTear {


  @Test
  public void testTear() {
    String val = Integer.toHexString(_ZtlSecureTear.value(0x1E)).toUpperCase();
    System.out.println(val);
  }

  @Test
  public void test() {
    BigInteger temp = BigInteger.ZERO;
    temp.setBit(128);
    System.out.println(temp);

  }


}
