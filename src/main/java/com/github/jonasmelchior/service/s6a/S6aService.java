package com.github.jonasmelchior.service.s6a;

import com.github.jonasmelchior.crypto.milenage.MilenageCrypto;
import com.github.jonasmelchior.crypto.milenage.MilenageKeyDecodingException;
import com.github.jonasmelchior.data.APNProfile;
import com.github.jonasmelchior.data.Subscriber;
import com.github.jonasmelchior.service.avp.MilenageAvpTranslator;
import com.github.jonasmelchior.service.avp.SubscriberAvpTranslator;
import com.github.jonasmelchior.service.subscriber.SubscriberService;
import io.quarkiverse.diameter.DiameterConfig;
import io.quarkiverse.diameter.DiameterService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jdiameter.api.*;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s13.ServerS13Session;
import org.jdiameter.api.s13.ServerS13SessionListener;
import org.jdiameter.api.s6a.ClientS6aSession;
import org.jdiameter.api.s6a.ServerS6aSession;
import org.jdiameter.api.s6a.ServerS6aSessionListener;
import org.jdiameter.api.s6a.events.*;
import org.jdiameter.client.impl.parser.MessageParser;
import org.jdiameter.common.impl.DiameterUtilities;
import org.jdiameter.common.impl.app.s6a.JAuthenticationInformationAnswerImpl;
import org.jdiameter.common.impl.app.s6a.JCancelLocationRequestImpl;
import org.jdiameter.common.impl.app.s6a.JPurgeUEAnswerImpl;
import org.jdiameter.common.impl.app.s6a.JUpdateLocationAnswerImpl;
import threegpp.milenage.MilenageResult;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

//TODO: set supported-features for all messages
@DiameterService
public class S6aService implements ServerS6aSessionListener
{

    @Inject
    SubscriberService subscriberService;
    @Inject
    S6aHSSRequestGenerator s6aHSSRequestGenerator;

    @DiameterConfig
    Stack stack;

    @Override
    public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Transactional
    @Override
    public void doAuthenticationInformationRequestEvent(ServerS6aSession session, JAuthenticationInformationRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            Log.info("Received AIR from " + request.getOriginHost());
        } catch (AvpDataException e) {
            Log.info("Received AIR. Couldn't decode Origin Host AVP");
        }

        JAuthenticationInformationAnswerImpl jAuthenticationInformationAnswer;
        Subscriber subscriber = null;

        try {
            String imsi = request.getMessage().getAvps().getAvp(1).getUTF8String();
            byte[] plmn = request.getMessage().getAvps().getAvp(1407).getOctetString();

            subscriber = subscriberService.getSubscriber(imsi);
            if (subscriber == null) {
                Log.error("Couldn't find subscriber in database");
                jAuthenticationInformationAnswer = new JAuthenticationInformationAnswerImpl((Request) request.getMessage(), ResultCode.AUTHENTICATION_REJECTED);
                session.sendAuthenticationInformationAnswer(jAuthenticationInformationAnswer);
                return;
            }

            MilenageCrypto milenageCrypto = new MilenageCrypto(subscriber.getK(), subscriber.getOpc(), subscriber.getAmf(), subscriber.getSqn());
            subscriber.setSqn(subscriber.getSqn() + 1);
            Map<MilenageResult, byte []> milenageResultMap = milenageCrypto.calculateAuthenticationInformationAnswer();

            jAuthenticationInformationAnswer = new JAuthenticationInformationAnswerImpl((Request) request.getMessage(), ResultCode.SUCCESS);
            AvpSet avps = jAuthenticationInformationAnswer.getMessage().getAvps();
            avps.addAvp(277, 1, false, false, true); // NO_STATE_MAINTAINED
            MilenageAvpTranslator.populateAuthInfo(
                    avps.addGroupedAvp(1413, 10415, false, false),
                    plmn,
                    milenageCrypto,
                    milenageResultMap
            );
        } catch (AvpDataException | ExecutionException | InterruptedException | NoSuchAlgorithmException |
                 InvalidKeyException | MilenageKeyDecodingException e) {
            Log.error("Couldn't generate AIA: " + e);
            jAuthenticationInformationAnswer = new JAuthenticationInformationAnswerImpl((Request) request, ResultCode.AUTHENTICATION_REJECTED);
        }

        session.sendAuthenticationInformationAnswer(jAuthenticationInformationAnswer);
        setServingMME((Request) request.getMessage(), subscriber);
    }


    @Transactional
    @Override
    public void doUpdateLocationRequestEvent(ServerS6aSession session, JUpdateLocationRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            Log.info("Received ULR from " + request.getOriginHost());
        } catch (AvpDataException e) {
            Log.info("Received ULR. Couldn't decode Origin Host AVP");
        }

        JUpdateLocationAnswerImpl jUpdateLocationAnswer;
        Subscriber subscriber = null;

        try {
            String imsi = request.getMessage().getAvps().getAvp(1).getUTF8String();

            subscriber = subscriberService.getSubscriber(imsi);
            if (subscriber == null) {
                Log.error("Couldn't find subscriber in database");
                jUpdateLocationAnswer = new JUpdateLocationAnswerImpl((Request) request, ResultCode.UNABLE_TO_COMPLY);
                session.sendUpdateLocationAnswer(jUpdateLocationAnswer);
                return;
            }

            // Build the Update-Location-Answer
            jUpdateLocationAnswer = new JUpdateLocationAnswerImpl((Request) request.getMessage(), ResultCode.SUCCESS);
            AvpSet avps = jUpdateLocationAnswer.getMessage().getAvps();
            avps.addAvp(277, 1, false, false, true); // NO_STATE_MAINTAINED
            // Subscription-Data (AVP 1400)
            SubscriberAvpTranslator.populateSubscriptionData(
                    avps.addGroupedAvp(1400, 10415, false, false),
                    subscriber
            );
            //TODO: set ULA-flags
        } catch (AvpDataException | UnknownHostException e) {
            Log.error("Error while processing ULR: ", e);
            jUpdateLocationAnswer = new JUpdateLocationAnswerImpl((Request) request.getMessage(), ResultCode.UNABLE_TO_COMPLY);
        }

        session.sendUpdateLocationAnswer(jUpdateLocationAnswer);
        setServingMME((Request) request.getMessage(), subscriber);
    }

    @Override
    public void doPurgeUERequestEvent(ServerS6aSession session, JPurgeUERequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            Log.info("Received PUR from " + request.getOriginHost());
        } catch (AvpDataException e) {
            Log.info("Received PUR. Couldn't decode Origin Host AVP");
        }

        JPurgeUEAnswerImpl jPurgeUEAnswer;
        Subscriber subscriber = null;

        try {
            String imsi = request.getMessage().getAvps().getAvp(1).getUTF8String();

            subscriber = subscriberService.getSubscriber(imsi);
            if (subscriber == null) {
                Log.error("Couldn't find subscriber in database");
                jPurgeUEAnswer = new JPurgeUEAnswerImpl((Request) request, 5030);
                session.sendPurgeUEAnswer(jPurgeUEAnswer);
                return;
            }

            subscriberService.purgeUE(subscriber);
            jPurgeUEAnswer = new JPurgeUEAnswerImpl((Request) request.getMessage(), ResultCode.SUCCESS);
            // TODO: Maybe implement PUA-flags
        } catch (AvpDataException e) {
            Log.error("Error while processing PUR: ", e);
            jPurgeUEAnswer = new JPurgeUEAnswerImpl((Request) request.getMessage(), ResultCode.UNABLE_TO_COMPLY);
        }

        session.sendPurgeUEAnswer(jPurgeUEAnswer);
    }

    @Override
    public void doNotifyRequestEvent(ServerS6aSession session, JNotifyRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doCancelLocationAnswerEvent(ServerS6aSession session, JCancelLocationRequest request, JCancelLocationAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            Log.info("Received Cancel Location Answer from " + answer.getOriginHost());
        } catch (AvpDataException e) {
            Log.info("Received Cancel Location Answer. Couldn't decode Origin Host AVP");
        }
    }

    @Override
    public void doInsertSubscriberDataAnswerEvent(ServerS6aSession session, JInsertSubscriberDataRequest request, JInsertSubscriberDataAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            Log.info("Received Insert Subscriber Data Answer from " + answer.getOriginHost());
        } catch (AvpDataException e) {
            Log.info("Received Insert Subscriber Data Answer. Couldn't decode Origin Host AVP");
        }
    }

    @Override
    public void doDeleteSubscriberDataAnswerEvent(ServerS6aSession session, JDeleteSubscriberDataRequest request, JDeleteSubscriberDataAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            Log.info("Received Delete Subscriber Data Answer from " + answer.getOriginHost());
        } catch (AvpDataException e) {
            Log.info("Received Delete Subscriber Data Answer. Couldn't decode Origin Host AVP");
        }
    }

    @Override
    public void doResetAnswerEvent(ServerS6aSession session, JResetRequest request, JResetAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    public void sendDeleteSubscriberDataRequest(Subscriber subscriber, APNProfile apnProfile) throws IllegalDiameterStateException, InternalException, RouteException, OverloadException {
        long vendorId = stack.getMetaData().getLocalPeer().getVendorId();
        ApplicationId application = ApplicationId.createByAuthAppId(vendorId, 16777251); // S6a application ID
        ServerS6aSession serverS6aSession = stack.getSessionFactory().getNewAppSession(null, application, ServerS6aSession.class);

        JDeleteSubscriberDataRequest jDeleteSubscriberDataRequest = s6aHSSRequestGenerator.createDeleteSubscriberDataRequest(subscriber, apnProfile);

        if (jDeleteSubscriberDataRequest != null) {
            Log.info("Sending DSD");
            serverS6aSession.sendDeleteSubscriberDataRequest(jDeleteSubscriberDataRequest);
        }
    }

    public void sendInsertSubscriberDataRequest(Subscriber subscriber) throws IllegalDiameterStateException, InternalException, RouteException, OverloadException {
        long vendorId = stack.getMetaData().getLocalPeer().getVendorId();
        ApplicationId application = ApplicationId.createByAuthAppId(vendorId, 16777251); // S6a application ID
        ServerS6aSession serverS6aSession = stack.getSessionFactory().getNewAppSession(null, application, ServerS6aSession.class);

        JInsertSubscriberDataRequest jInsertSubscriberDataRequest = s6aHSSRequestGenerator.createInsertSubscriberDataRequest(subscriber);

        if (jInsertSubscriberDataRequest != null) {
            Log.info("Sending CLR");
            serverS6aSession.sendInsertSubscriberDataRequest(jInsertSubscriberDataRequest);
        }
    }

    private void sendCancelLocationRequest(Request request, Subscriber subscriber) throws IllegalDiameterStateException, InternalException, RouteException, OverloadException {
        long vendorId = stack.getMetaData().getLocalPeer().getVendorId();
        ApplicationId application = ApplicationId.createByAuthAppId(vendorId, 16777251); // S6a application ID
        ServerS6aSession serverS6aSession = stack.getSessionFactory().getNewAppSession(null, application, ServerS6aSession.class);

        JCancelLocationRequest jCancelLocationRequest = null;

        if (request.getCommandCode() == 316) {
            // Cancellation-Type = MME-Update Procedure
            jCancelLocationRequest = s6aHSSRequestGenerator.createCancelLocationRequest(0, subscriber);
        }
        else if (request.getCommandCode() == 318) {
            // Cancellation-Type = Initial Attach Procedure
            jCancelLocationRequest = s6aHSSRequestGenerator.createCancelLocationRequest(4, subscriber);
        }

        if (jCancelLocationRequest != null) {
            Log.info("Sending CLR");
            serverS6aSession.sendCancelLocationRequest(jCancelLocationRequest);
        }
    }

    public void sendCancelLocationRequest(Subscriber subscriber) throws IllegalDiameterStateException, InternalException, RouteException, OverloadException {
        long vendorId = stack.getMetaData().getLocalPeer().getVendorId();
        ApplicationId application = ApplicationId.createByAuthAppId(vendorId, 16777251); // S6a application ID
        ServerS6aSession serverS6aSession = stack.getSessionFactory().getNewAppSession(null, application, ServerS6aSession.class);

        JCancelLocationRequest jCancelLocationRequest = s6aHSSRequestGenerator.createCancelLocationRequest(2, subscriber);

        // Cancellation-Type = Subscription Withdrawal
        jCancelLocationRequest.getMessage().getAvps().addAvp(1420, 2, 10415, false, false);

        Log.info("Sending CLR");
        serverS6aSession.sendCancelLocationRequest(jCancelLocationRequest);
    }

    private void setServingMME(Request request, Subscriber subscriber) {
        try {
            if (subscriber.getServingMMEHost() != null &&
                    !request.getAvps().getAvp(264).getUTF8String().equals(subscriber.getServingMMEHost())) {
                Log.info("Sending CLR to " + request.getAvps().getAvp(264).getUTF8String());
                sendCancelLocationRequest(request, subscriber);
            }
        } catch (AvpDataException | RouteException | IllegalDiameterStateException | OverloadException | InternalException e) {
            Log.error("Error occurred sending CLR to MME: " + e.getMessage());
        }

        try {
            subscriber.setServingMMERealm(request.getAvps().getAvp(Avp.ORIGIN_REALM).getUTF8String());
            subscriber.setServingMMEHost(request.getAvps().getAvp(Avp.ORIGIN_HOST).getUTF8String());
            subscriber.setPurged(false);
        } catch (AvpDataException e) {
            Log.error("Couldn't set origin realm and host for subscriber: " + subscriber.getImsi());
        }

        subscriberService.saveSubscriber(subscriber);
    }
}