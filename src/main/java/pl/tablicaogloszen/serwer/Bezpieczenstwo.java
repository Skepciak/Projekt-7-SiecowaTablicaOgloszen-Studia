package pl.tablicaogloszen.serwer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Klasa pomocnicza do obsługi bezpieczeństwa (hashowanie haseł).
 */
public class Bezpieczenstwo {

    /**
     * Tworzy hash SHA-256 z podanego hasła.
     * 
     * @param haslo Hasło w postaci tekstu jawnego
     * @return Hash w postaci ciągu szesnastkowego (hex)
     */
    public static String hashuj(String haslo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(haslo.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Brak algorytmu SHA-256", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
