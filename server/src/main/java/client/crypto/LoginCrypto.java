/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package client.crypto;

import tools.HexTool;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@lombok.extern.slf4j.Slf4j
public class LoginCrypto {

    protected final static int extralength = 6;
    private final static String[] Alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private final static String[] Number = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private final static Random rand = new Random();

    public static final String Generate_13DigitAsiasoftPassport() {
        StringBuilder sb = new StringBuilder();
        sb.append(Alphabet[rand.nextInt(Alphabet.length)]); // First Letter

        for (int i = 0; i < 11; i++) {
            sb.append(Number[rand.nextInt(Number.length)]); // 11 Numbers
        }
        sb.append(Alphabet[rand.nextInt(Alphabet.length)]); // Last Letter

        return sb.toString();
    }

    private static final String toSimpleHexString(final byte[] bytes) {
        return HexTool.toString(bytes).replace(" ", "").toLowerCase();
    }

    private static final String hashWithDigest(final String in, final String digest) {
        try {
            MessageDigest Digester = MessageDigest.getInstance(digest);
            Digester.update(in.getBytes(StandardCharsets.UTF_8), 0, in.length());
            byte[] sha1Hash = Digester.digest();
            return toSimpleHexString(sha1Hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Hashing the password failed", ex);
        }

    }

    private static final String hexSha1(final String in) {
        return hashWithDigest(in, "SHA-1");
    }

    private static final String hexSha512(final String in) {
        return hashWithDigest(in, "SHA-512");
    }

    public static final boolean checkSha1Hash(final String hash, final String password) {
        String hexSha1 = hexSha1(password);
        return hash.equals(hexSha1);
    }

    public static final boolean checkSaltedSha512Hash(final String hash, final String password, final String salt) {
        return hash.equals(makeSaltedSha512Hash(password, salt));
    }

    public static final String makeSaltedSha512Hash(final String password, final String salt) {
        return hexSha512(password + salt);
    }

    public static final String makeSalt() {
        byte[] salt = new byte[16];
        rand.nextBytes(salt);
        return toSimpleHexString(salt);
    }

    public final static String rand_s(final String in) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < extralength; i++) {
            sb.append(rand.nextBoolean() ? Alphabet[rand.nextInt(Alphabet.length)] : Number[rand.nextInt(Number.length)]);
        }
        return sb + in;
    }

    public final static String rand_r(final String in) {
        return in.substring(extralength, extralength + 128);
    }
}
