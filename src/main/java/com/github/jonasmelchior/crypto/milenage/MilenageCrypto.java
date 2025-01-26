package com.github.jonasmelchior.crypto.milenage;

import threegpp.milenage.Milenage;
import threegpp.milenage.MilenageBufferFactory;
import threegpp.milenage.MilenageResult;
import threegpp.milenage.biginteger.BigIntegerBuffer;
import threegpp.milenage.biginteger.BigIntegerBufferFactory;
import threegpp.milenage.cipher.Ciphers;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MilenageCrypto {
    private Milenage<BigIntegerBuffer> milenage;
    private byte[] rand;
    private byte[] sqn;
    private byte[] amf;

    public MilenageCrypto(String imsi) {
        MilenageBufferFactory<BigIntegerBuffer> bufferFactory = BigIntegerBufferFactory.getInstance();
        //TODO: Change this
        byte[] testKBytes = new byte[] {
                (byte) 0xCB, (byte) 0xB6, (byte) 0x22, (byte) 0x36,
                (byte) 0xA2, (byte) 0x83, (byte) 0x1C, (byte) 0xAB,
                (byte) 0x2A, (byte) 0xC6, (byte) 0x13, (byte) 0x77,
                (byte) 0x5F, (byte) 0x00, (byte) 0x50, (byte) 0x63
        };
        byte[] testOPcBytes = new byte[] {
                (byte) 0xA7, (byte) 0x3D, (byte) 0x56, (byte) 0x9C,
                (byte) 0xE4, (byte) 0x12, (byte) 0x4F, (byte) 0x8B,
                (byte) 0x3A, (byte) 0x7F, (byte) 0x99, (byte) 0x21,
                (byte) 0xD8, (byte) 0x65, (byte) 0x44, (byte) 0x2E
        };

        Cipher K = Ciphers.createRijndaelCipher(testKBytes);
        byte[] OPc = Milenage.calculateOPc(testOPcBytes, K, bufferFactory);
        this.milenage = new Milenage<>(OPc, K, bufferFactory);
    }

    public Map<MilenageResult, byte []> calculateAuthenticationInformationAnswer() throws ExecutionException, InterruptedException {
        this.amf = new byte[]{(byte) 0x80, (byte) 0x00};
        this.rand = new byte[]{
                (byte) 0x7f, (byte) 0x29, (byte) 0x4c, (byte) 0x83,
                (byte) 0x51, (byte) 0x0d, (byte) 0x69, (byte) 0x8a,
                (byte) 0x3c, (byte) 0x4b, (byte) 0x5a, (byte) 0x91,
                (byte) 0x11, (byte) 0xb0, (byte) 0x65, (byte) 0xdb
        };
        this.sqn = new byte[]{
                (byte) 0xa1, (byte) 0x7d, (byte) 0x4b, (byte) 0x93,
                (byte) 0x12, (byte) 0x56
        };

        return this.milenage.calculateAll(this.rand, sqn, amf, Executors.newSingleThreadExecutor());
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
