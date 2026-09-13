package kr.zipimoney.global.crypto;

import kr.zipimoney.global.exception.DomainException;
import kr.zipimoney.global.exception.DomainExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * AES-256-GCM 암복호화 유틸.
 * Node.js 호환 바이트 레이아웃: [IV 12B][AuthTag 16B][Ciphertext]
 */
@Slf4j
@Component
public class AesGcmCryptoUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int TAG_LENGTH_BYTES = 16;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmCryptoUtil(@Value("${crypto.profile-key:}") String hexKey) {
        if (hexKey == null || hexKey.isBlank()) {
            log.warn("PROFILE_ENCRYPTION_KEY가 설정되지 않았습니다. 암호화 기능이 비활성화됩니다.");
            this.secretKey = null;
        } else {
            byte[] keyBytes = HexFormat.of().parseHex(hexKey);
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        }
    }

    /**
     * 암호화: 평문 → [IV 12B][AuthTag 16B][Ciphertext]
     */
    public byte[] encrypt(String plaintext) {
        if (secretKey == null) {
            throw new DomainException(DomainExceptionCode.ENCRYPTION_FAILED);
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Java GCM appends tag to ciphertext, need to split
            // encrypted = [ciphertext][tag 16B]
            int ciphertextLen = encrypted.length - TAG_LENGTH_BYTES;
            byte[] ciphertext = new byte[ciphertextLen];
            byte[] tag = new byte[TAG_LENGTH_BYTES];
            System.arraycopy(encrypted, 0, ciphertext, 0, ciphertextLen);
            System.arraycopy(encrypted, ciphertextLen, tag, 0, TAG_LENGTH_BYTES);

            // Node.js layout: [IV][TAG][CIPHERTEXT]
            ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH + TAG_LENGTH_BYTES + ciphertextLen);
            buffer.put(iv);
            buffer.put(tag);
            buffer.put(ciphertext);
            return buffer.array();
        } catch (Exception e) {
            log.error("암호화 실패", e);
            throw new DomainException(DomainExceptionCode.ENCRYPTION_FAILED);
        }
    }

    /**
     * 복호화: [IV 12B][AuthTag 16B][Ciphertext] → 평문
     */
    public String decrypt(byte[] data) {
        if (secretKey == null) {
            throw new DomainException(DomainExceptionCode.DECRYPTION_FAILED);
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] tag = new byte[TAG_LENGTH_BYTES];
            buffer.get(tag);

            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            // Java GCM expects: [ciphertext][tag]
            byte[] combined = new byte[ciphertext.length + TAG_LENGTH_BYTES];
            System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
            System.arraycopy(tag, 0, combined, ciphertext.length, TAG_LENGTH_BYTES);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            byte[] decrypted = cipher.doFinal(combined);
            return new String(decrypted, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("복호화 실패", e);
            throw new DomainException(DomainExceptionCode.DECRYPTION_FAILED);
        }
    }

    public byte[] encryptNumber(long value) {
        return encrypt(String.valueOf(value));
    }

    public long decryptNumber(byte[] data) {
        return Long.parseLong(decrypt(data));
    }
}
