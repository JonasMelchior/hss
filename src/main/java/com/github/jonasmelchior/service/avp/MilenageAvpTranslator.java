package com.github.jonasmelchior.service.avp;

import com.github.jonasmelchior.crypto.milenage.MilenageCrypto;
import org.jdiameter.api.AvpSet;
import threegpp.milenage.MilenageResult;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class MilenageAvpTranslator {
    public static void populateAuthInfo(AvpSet authenticationInfoAvpSet, byte[] plmn, MilenageCrypto milenageCrypto, Map<MilenageResult, byte []> milenageResultMap) throws NoSuchAlgorithmException, InvalidKeyException {

        AvpSet EUtranVectorAvpSet = authenticationInfoAvpSet.addGroupedAvp(1414, 10415, false, false);
        // RAND
        EUtranVectorAvpSet.addAvp(1447, milenageCrypto.getRand(), 10415, false, false);
        // XRES
        EUtranVectorAvpSet.addAvp(1448, milenageResultMap.get(MilenageResult.RES), 10415, false, false);
        // AUTN
        EUtranVectorAvpSet.addAvp(1449, milenageCrypto.calculateAUTN(
                milenageCrypto.getSqn(),
                milenageResultMap.get(MilenageResult.AK),
                milenageCrypto.getAmf(),
                milenageResultMap.get(MilenageResult.MAC_A)
        ), 10415, false, false);
        //KASME
        EUtranVectorAvpSet.addAvp(1450, milenageCrypto.calculateKasme(
                milenageResultMap.get(MilenageResult.CK),
                milenageResultMap.get(MilenageResult.IK),
                plmn,
                milenageCrypto.getSqn(),
                milenageResultMap.get(MilenageResult.AK)
        ), 10415, false, false);
    }
}
