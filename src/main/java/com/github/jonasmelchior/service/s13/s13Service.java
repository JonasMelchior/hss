package com.github.jonasmelchior.service.s13;

import com.github.jonasmelchior.data.EquipmentRegister;
import com.github.jonasmelchior.data.Subscriber;
import io.quarkus.logging.Log;
import org.jdiameter.api.*;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s13.ServerS13Session;
import org.jdiameter.api.s13.ServerS13SessionListener;
import org.jdiameter.api.s13.events.JMEIdentityCheckRequest;
import org.jdiameter.common.impl.app.s13.JMEIdentityCheckAnswerImpl;
import org.jdiameter.common.impl.app.s6a.JAuthenticationInformationAnswerImpl;

public class s13Service implements ServerS13SessionListener {
    @Override
    public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doMEIdentityCheckRequestEvent(ServerS13Session session, JMEIdentityCheckRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            Log.info("Received MEIC from " + request.getOriginHost());
        } catch (AvpDataException e) {
            Log.info("Received MEIC. Couldn't decode Origin Host AVP");
        }

        JMEIdentityCheckAnswerImpl jmeIdentityCheckAnswer;
        EquipmentRegister equipmentRegister = null;

        try {
            String imsi = null;
            if (request.getMessage().getAvps().getAvp(1) != null) {
                imsi = request.getMessage().getAvps().getAvp(1).getUTF8String();
            }

            // TODO: Lookup authorized IMEIS, and generate appropriate MEIdentityCheckAnswer

        } catch (AvpDataException e) {
            throw new RuntimeException(e);
        }
    }
}
