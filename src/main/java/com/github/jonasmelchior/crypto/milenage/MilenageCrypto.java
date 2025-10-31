package com.github.jonasmelchior.crypto.milenage;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import threegpp.milenage.Milenage;
import threegpp.milenage.MilenageBufferFactory;
import threegpp.milenage.MilenageResult;
import threegpp.milenage.biginteger.BigIntegerBuffer;
import threegpp.milenage.biginteger.BigIntegerBufferFactory;
import threegpp.milenage.cipher.Ciphers;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MilenageCrypto {
    private Milenage<BigIntegerBuffer> milenage;
    private byte[] rand;
    private byte[] sqn;
    private byte[] amf;

    public MilenageCrypto(String KKey, String opc, String amf, Long sqn) throws MilenageKeyDecodingException {
        MilenageBufferFactory<BigIntegerBuffer> bufferFactory = BigIntegerBufferFactory.getInstance();

        byte[] kBytes;
        byte[] opcBytes;
        byte[] amfBytes;
        byte[] sqnBytes;

        try {
            kBytes = Hex.decodeHex(KKey);
            opcBytes = Hex.decodeHex(opc);
            amfBytes = Hex.decodeHex(amf);
            sqnBytes = longTo6Bytes(sqn);
        } catch (DecoderException e) {
            throw new MilenageKeyDecodingException("Failed to decode milenage keys: " + e.getMessage());
        }

        this.amf = amfBytes;
        this.sqn = sqnBytes;

        Cipher K = Ciphers.createRijndaelCipher(kBytes);
        byte[] OPc = Milenage.calculateOPc(opcBytes, K, bufferFactory);
        this.milenage = new Milenage<>(OPc, K, bufferFactory);
    }

    public Map<MilenageResult, byte []> calculateAuthenticationInformationAnswer() throws ExecutionException, InterruptedException {
        this.rand = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(rand);

        return this.milenage.calculateAll(this.rand, sqn, amf, Executors.newSingleThreadExecutor());
    }

    private static byte[] longTo6Bytes(Long value) {
        byte[] fullBytes = ByteBuffer.allocate(Long.BYTES).putLong(value).array(); // 8 bytes
        byte[] sixBytes = new byte[6];
        System.arraycopy(fullBytes, 2, sixBytes, 0, 6); // take least significant 6 bytes
        return sixBytes;
    }

    public byte[] getRand() {
        return rand;
    }

    public byte[] getSqn() {
        return sqn;
    }

    public byte[] getAmf() {
        return amf;
    }

    public byte[] calculateAUTN(byte[] sqn, byte[] ak, byte[] amf, byte[] mac_a) {
        byte[] xorSqnAk = xorArrays(sqn, ak);
        byte[] AUTN = new byte[16];
        System.arraycopy(xorSqnAk, 0, AUTN, 0, xorSqnAk.length);
        System.arraycopy(amf, 0, AUTN, xorSqnAk.length, amf.length);
        System.arraycopy(mac_a, 0, AUTN, xorSqnAk.length + amf.length, mac_a.length);

        return AUTN;
    }

    public byte[] calculateKasme(byte[] ck, byte[] ik, byte[] plmn, byte[] sqn, byte[] ak) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] k = new byte[128];
        System.arraycopy(ck, 0, k, 0, ck.length);
        System.arraycopy(ik, 0, k, ck.length, ik.length);

        byte[] s = new byte[14];
        System.arraycopy(plmn, 0, s, 0, 3);
        s[4] = 0x00;
        s[5] = 0x03;

        System.arraycopy(xorArrays(sqn, ak), 0, s, 6, 6);
        s[12] = 0x00;
        s[13] = 0x06;

        SecretKeySpec secretKeySpec = new SecretKeySpec(k, "HmacSHA512");
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(secretKeySpec);
        return mac.doFinal(s);
    }

    private byte[] xorArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length];
        for (int i = 0; i < array1.length; i++) {
            result[i] = (byte) (array1[i] ^ array2[i]);
        }
        return result;
    }
}
