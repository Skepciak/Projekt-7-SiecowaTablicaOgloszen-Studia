package pl.tablicaogloszen.serwer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Klasa do hashowania haseł.
 * Używamy SHA-256 żeby hasła nie były przechowywane jako plaintext w bazie.
 */
public class Bezpieczenstwo {

    /**
     * Hashuje hasło algorytmem SHA-256.
     * Dzięki temu nawet jak ktoś wejdzie do bazy, nie zobaczy prawdziwych haseł.
     * 
     * @param haslo hasło użytkownika (plaintext)
     * @return zahashowane hasło jako string hex (64 znaki)
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
