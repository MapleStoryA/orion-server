package database;

import at.favre.lib.crypto.bcrypt.BCrypt;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Simple class to hash the password with Bcrypt.
 * <p>
 * salt = SHA-256(uuid + seed)
 * hashed password = bcrypt(salt + password)
 * decrypted password = bcrypt_verify((salt + raw password), hashed password)
 **/
@Slf4j
public class PasswordEncryptor {


    public String encrypt(String password, String salt) {
        return BCrypt.withDefaults().hashToString(10, salt.concat(password).toCharArray());
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword, String salt) {
        return BCrypt.verifyer().verify(salt.concat(rawPassword).toCharArray(), hashedPassword).verified;
    }

    private String createSha256(String seed) throws NoSuchAlgorithmException {
        var md = MessageDigest.getInstance("SHA-256");
        var uuid = UUID.randomUUID().toString();
        md.update(uuid.getBytes());
        byte[] bytes = md.digest(seed.getBytes(StandardCharsets.UTF_8));
        var sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private String generateSalt(String seed) {
        try {
            return createSha256(seed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        var encryptor = new PasswordEncryptor();
        var salt = encryptor.generateSalt("admin");
        var encryptedPassword = encryptor.encrypt("admin", salt);

        log.info("Salt: {}", salt);
        log.info("Salted password: {}", encryptedPassword);
        log.info("Result: {}", encryptor.verifyPassword("admin", encryptedPassword, salt));
    }
}
