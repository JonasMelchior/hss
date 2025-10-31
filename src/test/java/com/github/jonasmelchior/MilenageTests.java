package com.github.jonasmelchior;

import com.github.jonasmelchior.crypto.milenage.MilenageCrypto;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import threegpp.milenage.MilenageResult;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@QuarkusTest
public class MilenageTests {
    @Test
    void testCryptoStuff() throws ExecutionException, InterruptedException, DecoderException, NoSuchAlgorithmException, InvalidKeyException {
//        MilenageCrypto milenageCrypto = new MilenageCrypto("238025123456789");
//        Map<MilenageResult, byte []> milenageResultMap = milenageCrypto.calculateAuthenticationInformationAnswer();
//        milenageCrypto.calculateAUTN(
//                milenageCrypto.getSqn(),
//                milenageResultMap.get(MilenageResult.AK),
//                milenageCrypto.getAmf(),
//                milenageResultMap.get(MilenageResult.MAC_A)
//        );
//        milenageCrypto.calculateKasme(
//                milenageResultMap.get(MilenageResult.CK),
//                milenageResultMap.get(MilenageResult.IK),
//                Hex.decodeHex("32F810"),
//                milenageCrypto.getSqn(),
//                milenageResultMap.get(MilenageResult.AK)
//        );
    }
}
