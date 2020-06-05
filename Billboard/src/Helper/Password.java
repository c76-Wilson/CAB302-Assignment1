package Helper;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class Password {

    public static String hash(String password){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes());

            String hashedPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest());

            return hashedPassword;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static String getSaltedHash(String password) throws Exception {
        byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(16);

        return Base64.getEncoder().encodeToString(salt) + "$" + saltAndHash(password, salt);
    }

    private static String saltAndHash(String password, byte[] salt) throws Exception{
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, 20*1000, 512));

        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static boolean authenticatePassword(String password, String storedPassword) throws Exception {
        String[] saltAndHash = storedPassword.split("\\$");

        if (saltAndHash.length < 2){
            throw new Exception("Stored password must be in format 'SALT$HASH'");
        }

        String hashOfInput = saltAndHash(password, Base64.getDecoder().decode(saltAndHash[0]));

        return hashOfInput.equals(saltAndHash[1]);
    }
}