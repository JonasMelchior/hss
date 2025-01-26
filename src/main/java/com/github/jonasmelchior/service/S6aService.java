package com.github.jonasmelchior.service;

import com.github.jonasmelchior.crypto.milenage.MilenageCrypto;
import io.quarkiverse.diameter.DiameterService;
import io.quarkus.logging.Log;
import org.jdiameter.api.*;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6a.ServerS6aSession;
import org.jdiameter.api.s6a.ServerS6aSessionListener;
import org.jdiameter.api.s6a.events.*;
import org.jdiameter.common.impl.app.s6a.JAuthenticationInformationAnswerImpl;
import threegpp.milenage.MilenageResult;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@DiameterService
public class S6aService implements ServerS6aSessionListener
{

    @Override
    public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doAuthenticationInformationRequestEvent(ServerS6aSession session, JAuthenticationInformationRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        Log.info("Received AIR");
        JAuthenticationInformationAnswerImpl jAuthenticationInformationAnswer = null;
        try {
            String imsi = request.getMessage().getAvps().getAvp(1).getUTF8String();
            byte[] plmn = request.getMessage().getAvps().getAvp(1407).getOctetString();
            MilenageCrypto milenageCrypto = new MilenageCrypto(imsi);
            Map<MilenageResult, byte []> milenageResultMap = milenageCrypto.calculateAuthenticationInformationAnswer();

            jAuthenticationInformationAnswer = new JAuthenticationInformationAnswerImpl((Request) request.getMessage(), ResultCode.SUCCESS);

            AvpSet authenticationInfoAvpSet = jAuthenticationInformationAnswer.getMessage().getAvps().addGroupedAvp(1413, 10415, false, false);
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
        } catch (AvpDataException | ExecutionException | InterruptedException | NoSuchAlgorithmException |
                 InvalidKeyException e) {
            jAuthenticationInformationAnswer = new JAuthenticationInformationAnswerImpl((Request) request, ResultCode.AUTHENTICATION_REJECTED);
        }

        session.sendAuthenticationInformationAnswer(jAuthenticationInformationAnswer);
    }

    @Override
    public void doPurgeUERequestEvent(ServerS6aSession session, JPurgeUERequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doUpdateLocationRequestEvent(ServerS6aSession session, JUpdateLocationRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doNotifyRequestEvent(ServerS6aSession session, JNotifyRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doCancelLocationAnswerEvent(ServerS6aSession session, JCancelLocationRequest request, JCancelLocationAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doInsertSubscriberDataAnswerEvent(ServerS6aSession session, JInsertSubscriberDataRequest request, JInsertSubscriberDataAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doDeleteSubscriberDataAnswerEvent(ServerS6aSession session, JDeleteSubscriberDataRequest request, JDeleteSubscriberDataAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doResetAnswerEvent(ServerS6aSession session, JResetRequest request, JResetAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }
}