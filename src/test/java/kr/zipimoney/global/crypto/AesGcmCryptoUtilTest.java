package kr.zipimoney.global.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AesGcmCryptoUtilTest {

    // 32-byte test key (64 hex chars)
    private static final String TEST_KEY = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private final AesGcmCryptoUtil cryptoUtil = new AesGcmCryptoUtil(TEST_KEY);

    @Test
    @DisplayName("문자열 암호화 후 복호화하면 원문과 동일해야 한다")
    void encryptAndDecrypt_string() {
        String plaintext = "Hello, World!";

        byte[] encrypted = cryptoUtil.encrypt(plaintext);
        String decrypted = cryptoUtil.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("숫자 암호화 후 복호화하면 원래 값과 동일해야 한다")
    void encryptAndDecrypt_number() {
        long value = 50_000_000L;

        byte[] encrypted = cryptoUtil.encryptNumber(value);
        long decrypted = cryptoUtil.decryptNumber(encrypted);

        assertThat(decrypted).isEqualTo(value);
    }

    @Test
    @DisplayName("동일한 평문을 두 번 암호화하면 다른 암호문이 나와야 한다 (IV가 랜덤)")
    void encrypt_sameInput_differentOutput() {
        String plaintext = "test";

        byte[] encrypted1 = cryptoUtil.encrypt(plaintext);
        byte[] encrypted2 = cryptoUtil.encrypt(plaintext);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("암호화된 바이트 레이아웃은 [IV 12B][Tag 16B][Ciphertext]")
    void encrypt_correctByteLayout() {
        String plaintext = "test";

        byte[] encrypted = cryptoUtil.encrypt(plaintext);

        // IV(12) + Tag(16) + Ciphertext(>=4 for "test")
        assertThat(encrypted.length).isGreaterThanOrEqualTo(12 + 16 + 4);
    }

    @Test
    @DisplayName("한국어 문자열 암복호화")
    void encryptAndDecrypt_korean() {
        String plaintext = "안녕하세요, 세계!";

        byte[] encrypted = cryptoUtil.encrypt(plaintext);
        String decrypted = cryptoUtil.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("큰 숫자 암복호화")
    void encryptAndDecrypt_largeNumber() {
        long value = 999_999_999_999L;

        byte[] encrypted = cryptoUtil.encryptNumber(value);
        long decrypted = cryptoUtil.decryptNumber(encrypted);

        assertThat(decrypted).isEqualTo(value);
    }
}
