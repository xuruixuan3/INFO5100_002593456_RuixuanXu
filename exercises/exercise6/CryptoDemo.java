import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

/**
 * CryptoDemo.java
 *
 * Demonstrates:
 * 1) Symmetric encryption/decryption with AES-256 using AES/GCM/NoPadding
 * 2) Asymmetric encryption/decryption with RSA-2048 using RSA/ECB/PKCS1Padding
 * 3) Signing and verifying with RSA-2048 keys (SHA256withRSA)
 *
 * NOTE:
 * - For AES/GCM, use a fresh random IV per encryption.
 * - For RSA encryption, only encrypt small payloads (due to RSA size limits).
 */
public class CryptoDemo {

    // ---------------------- Utilities ----------------------

    private static String b64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private static byte[] fromB64(String s) {
        return Base64.getDecoder().decode(s);
    }

    private static byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        new SecureRandom().nextBytes(b);
        return b;
    }

    // ---------------------- AES/GCM (Symmetric) ----------------------

    /**
     * Encrypt plaintext with AES-256 in GCM mode.
     * Returns a compact package: IV || CIPHERTEXT+TAG
     */
    private static byte[] aesGcmEncrypt(byte[] aesKey32Bytes, byte[] plaintext) throws Exception {
        // GCM recommended IV length is 12 bytes
        byte[] iv = randomBytes(12);

        SecretKey key = new SecretKeySpec(aesKey32Bytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        // 128-bit authentication tag is standard
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] cipherTextWithTag = cipher.doFinal(plaintext);

        // Package = IV || cipherTextWithTag
        byte[] out = new byte[iv.length + cipherTextWithTag.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(cipherTextWithTag, 0, out, iv.length, cipherTextWithTag.length);
        return out;
    }

    private static byte[] aesGcmDecrypt(byte[] aesKey32Bytes, byte[] ivPlusCiphertext) throws Exception {
        byte[] iv = Arrays.copyOfRange(ivPlusCiphertext, 0, 12);
        byte[] cipherTextWithTag = Arrays.copyOfRange(ivPlusCiphertext, 12, ivPlusCiphertext.length);

        SecretKey key = new SecretKeySpec(aesKey32Bytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        // If tampered, this throws AEADBadTagException
        return cipher.doFinal(cipherTextWithTag);
    }

    // ---------------------- RSA (Asymmetric Encryption) ----------------------

    private static byte[] rsaEncrypt(PublicKey publicKey, byte[] plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plaintext);
    }

    private static byte[] rsaDecrypt(PrivateKey privateKey, byte[] ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(ciphertext);
    }

    // ---------------------- RSA Sign / Verify ----------------------

    private static byte[] rsaSign(PrivateKey privateKey, byte[] message) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(message);
        return sig.sign();
    }

    private static boolean rsaVerify(PublicKey publicKey, byte[] message, byte[] signature) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(message);
        return sig.verify(signature);
    }

    // ---------------------- Actor (Alice / Bob) ----------------------

    static class Person {
        final String name;

        // RSA identity keys
        final KeyPair rsaKeyPair;

        Person(String name) throws Exception {
            this.name = name;
            this.rsaKeyPair = generateRsa2048KeyPair();
        }

        PublicKey getPublicKey() {
            return rsaKeyPair.getPublic();
        }

        PrivateKey getPrivateKey() {
            return rsaKeyPair.getPrivate();
        }

        private static KeyPair generateRsa2048KeyPair() throws Exception {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return kpg.generateKeyPair();
        }
    }

    // ---------------------- Main Demo ----------------------

    public static void main(String[] args) throws Exception {
        Person alice = new Person("Alice");
        Person bob = new Person("Bob");

        System.out.println("=== Actors Created ===");
        System.out.println("Alice RSA Public Key (Base64, truncated): " + b64(alice.getPublicKey().getEncoded()).substring(0, 40) + "...");
        System.out.println("Bob   RSA Public Key (Base64, truncated): " + b64(bob.getPublicKey().getEncoded()).substring(0, 40) + "...");
        System.out.println();

        // ============================================================
        // 1) Symmetric: AES-256 GCM
        // ============================================================

        System.out.println("=== 1) Symmetric Encryption: AES-256 / GCM / NoPadding ===");

        // Create a 256-bit AES key (32 bytes). In real life, they must share it securely.
        // Here we just generate it once to simulate "shared secret".
        byte[] sharedAesKey = generateAes256KeyBytes();
        System.out.println("Shared AES-256 Key (Base64): " + b64(sharedAesKey));

        String msg1 = "Hi Bob — meet me at 3pm. (AES/GCM)";
        System.out.println("\nAlice -> Bob plaintext: " + msg1);

        byte[] aesPackage = aesGcmEncrypt(sharedAesKey, msg1.getBytes(StandardCharsets.UTF_8));
        System.out.println("Alice -> Bob AES package (IV||CT+TAG) Base64: " + b64(aesPackage));

        byte[] decrypted1 = aesGcmDecrypt(sharedAesKey, aesPackage);
        System.out.println("Bob decrypted plaintext: " + new String(decrypted1, StandardCharsets.UTF_8));

        // Optional: demonstrate tampering detection
        System.out.println("\nTamper test (flip 1 byte) -> should fail verification:");
        byte[] tampered = Arrays.copyOf(aesPackage, aesPackage.length);
        tampered[tampered.length - 1] ^= 0x01; // flip 1 bit
        try {
            aesGcmDecrypt(sharedAesKey, tampered);
            System.out.println("Unexpected: tampered message decrypted successfully (should not happen).");
        } catch (Exception ex) {
            System.out.println("Expected failure on tampered ciphertext: " + ex.getClass().getSimpleName());
        }

        System.out.println();

        // ============================================================
        // 2) Asymmetric: RSA encryption (RSA/ECB/PKCS1Padding)
        // ============================================================

        System.out.println("=== 2) Asymmetric Encryption: RSA-2048 / ECB / PKCS1Padding ===");
        // IMPORTANT: RSA can only encrypt small messages. We'll keep it short.
        String msg2 = "Secret token: 12345";
        System.out.println("\nAlice -> Bob plaintext: " + msg2);

        byte[] rsaCipher = rsaEncrypt(bob.getPublicKey(), msg2.getBytes(StandardCharsets.UTF_8));
        System.out.println("Alice -> Bob RSA ciphertext Base64: " + b64(rsaCipher));

        byte[] decrypted2 = rsaDecrypt(bob.getPrivateKey(), rsaCipher);
        System.out.println("Bob decrypted plaintext: " + new String(decrypted2, StandardCharsets.UTF_8));

        // And Bob -> Alice, similarly:
        String msg2b = "Got it. -Bob";
        System.out.println("\nBob -> Alice plaintext: " + msg2b);

        byte[] rsaCipher2 = rsaEncrypt(alice.getPublicKey(), msg2b.getBytes(StandardCharsets.UTF_8));
        System.out.println("Bob -> Alice RSA ciphertext Base64: " + b64(rsaCipher2));

        byte[] decrypted2b = rsaDecrypt(alice.getPrivateKey(), rsaCipher2);
        System.out.println("Alice decrypted plaintext: " + new String(decrypted2b, StandardCharsets.UTF_8));

        System.out.println();

        // ============================================================
        // 3) Digital Signature: RSA sign/verify (RSA-2048)
        // ============================================================

        System.out.println("=== 3) Signing & Verifying: RSA-2048 (SHA256withRSA) ===");

        String msg3 = "I, Alice, approve this transaction.";
        byte[] msg3Bytes = msg3.getBytes(StandardCharsets.UTF_8);

        System.out.println("\nMessage: " + msg3);

        // Alice signs with her private key
        byte[] signature = rsaSign(alice.getPrivateKey(), msg3Bytes);
        System.out.println("Alice signature Base64: " + b64(signature));

        // Bob verifies with Alice's public key
        boolean ok = rsaVerify(alice.getPublicKey(), msg3Bytes, signature);
        System.out.println("Bob verifies Alice signature: " + (ok ? "VALID" : "INVALID"));

        // Negative case: changed message should fail
        String altered = "I, Alice, approve this transaction (CHANGED).";
        boolean ok2 = rsaVerify(alice.getPublicKey(), altered.getBytes(StandardCharsets.UTF_8), signature);
        System.out.println("Bob verifies signature on altered message: " + (ok2 ? "VALID (unexpected)" : "INVALID (expected)"));

        System.out.println("\n=== Demo Complete ===");
    }

    private static byte[] generateAes256KeyBytes() throws Exception {
        // Preferred: use KeyGenerator for AES and then extract bytes
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        SecretKey key = kg.generateKey();
        byte[] raw = key.getEncoded();
        if (raw == null || raw.length != 32) {
            throw new IllegalStateException("Expected 32-byte AES-256 key, got: " + (raw == null ? "null" : raw.length));
        }
        return raw;
    }
}
