package com.github.jonasmelchior.service.s6a;

import com.github.jonasmelchior.data.APNProfile;
import com.github.jonasmelchior.data.Subscriber;
import com.github.jonasmelchior.service.avp.SubscriberAvpTranslator;
import io.quarkiverse.diameter.DiameterConfig;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.jdiameter.api.*;
import org.jdiameter.api.s6a.events.JCancelLocationRequest;
import org.jdiameter.api.s6a.events.JDeleteSubscriberDataRequest;
import org.jdiameter.api.s6a.events.JInsertSubscriberDataRequest;
import org.jdiameter.client.impl.parser.MessageParser;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class S6aHSSRequestGenerator {

    @DiameterConfig
    Stack stack;

    public JCancelLocationRequest createCancelLocationRequest(int cancellationType, Subscriber subscriber) throws IllegalDiameterStateException, InternalException {
        long vendorId = stack.getMetaData().getLocalPeer().getVendorId();
        ApplicationId application = ApplicationId.createByAuthAppId(vendorId, 16777251); // S6a application ID

        MessageParser messageParser = new MessageParser();

        return new JCancelLocationRequest() {
            @Override
            public String getDestinationHost() throws AvpDataException {
                return subscriber.getServingMMEHost();
            }

            @Override
            public String getDestinationRealm() throws AvpDataException {
                return subscriber.getServingMMERealm();
            }

            @Override
            public int getCommandCode() {
                return 317;
            }

            @Override
            public Message getMessage() throws InternalException {
                Message message = messageParser.createEmptyMessage(getCommandCode(), 16777251);
                message.getApplicationIdAvps().add(ApplicationId.createByAuthAppId(vendorId, 16777251));
                message.setRequest(true);
                try {
                    message.getAvps().addAvp(1, subscriber.getImsi().getBytes(StandardCharsets.UTF_8),false, false);
                    message.getAvps().addAvp(277, 1, false, false, true); // NO_STATE_MAINTAINED
                    message.getAvps().addAvp(1420, cancellationType, 10415,false, false);
                    message.getAvps().addAvp(Avp.DESTINATION_REALM, getDestinationRealm(), false);
                    message.getAvps().addAvp(Avp.DESTINATION_HOST, getDestinationHost(), false);
                    message.getAvps().addAvp(Avp.ORIGIN_REALM, getOriginRealm(), false);
                    message.getAvps().addAvp(Avp.ORIGIN_HOST, getOriginHost(), false);
                } catch (AvpDataException e) {
                    Log.info("Couldn't generate AVP''s for CLR");
                }

                return message;
            }

            @Override
            public String getOriginHost() throws AvpDataException {
                return stack.getMetaData().getLocalPeer().getUri().getFQDN();
            }

            @Override
            public String getOriginRealm() throws AvpDataException {
                return stack.getMetaData().getLocalPeer().getRealmName();
            }
        };
    }

    public JInsertSubscriberDataRequest createInsertSubscriberDataRequest(Subscriber subscriber) throws IllegalDiameterStateException, InternalException {
        long vendorId = stack.getMetaData().getLocalPeer().getVendorId();
        ApplicationId application = ApplicationId.createByAuthAppId(vendorId, 16777251); // S6a application ID

        MessageParser messageParser = new MessageParser();

        return new JInsertSubscriberDataRequest() {
            @Override
            public String getDestinationHost() throws AvpDataException {
                return subscriber.getServingMMEHost();
            }

            @Override
            public String getDestinationRealm() throws AvpDataException {
                return subscriber.getServingMMERealm();
            }

            @Override
            public int getCommandCode() {
                return 319;
            }

            @Override
            public Message getMessage() throws InternalException {
                Message message = messageParser.createEmptyMessage(getCommandCode(), 16777251);
                message.getApplicationIdAvps().add(ApplicationId.createByAuthAppId(vendorId, 16777251));
                message.setRequest(true);
                try {
                    message.getAvps().addAvp(1, subscriber.getImsi().getBytes(StandardCharsets.UTF_8),false, false);
                    message.getAvps().addAvp(277, 1, false, false, true); // NO_STATE_MAINTAINED
                    message.getAvps().addAvp(Avp.DESTINATION_REALM, getDestinationRealm(), false);
                    message.getAvps().addAvp(Avp.DESTINATION_HOST, getDestinationHost(), false);
                    message.getAvps().addAvp(Avp.ORIGIN_REALM, getOriginRealm(), false);
                    message.getAvps().addAvp(Avp.ORIGIN_HOST, getOriginHost(), false);
                    SubscriberAvpTranslator.populateSubscriptionData(
                            message.getAvps().addGroupedAvp(1400, 10415, false, false),
                            subscriber
                    );
                } catch (AvpDataException | UnknownHostException e) {
                    Log.error("Couldn't generate AVP's for CLR: ", e);
                }

                return message;
            }

            @Override
            public String getOriginHost() throws AvpDataException {
                return stack.getMetaData().getLocalPeer().getUri().getFQDN();
            }

            @Override
            public String getOriginRealm() throws AvpDataException {
                return stack.getMetaData().getLocalPeer().getRealmName();
            }
        };
    }

    /**
     * @param subscriber: Subscriber for which one or all APN configurations will be deleted
     * @param apnProfile: APNProfile to be deleted. Null if all are to be deleted
     * @return
     * @throws IllegalDiameterStateException
     * @throws InternalException
     */
    public JDeleteSubscriberDataRequest createDeleteSubscriberDataRequest(Subscriber subscriber, APNProfile apnProfile) throws IllegalDiameterStateException, InternalException {
        long vendorId = stack.getMetaData().getLocalPeer().getVendorId();
        ApplicationId application = ApplicationId.createByAuthAppId(vendorId, 16777251); // S6a application ID

        MessageParser messageParser = new MessageParser();

        return new JDeleteSubscriberDataRequest() {
            @Override
            public String getDestinationHost() throws AvpDataException {
                return subscriber.getServingMMEHost();
            }

            @Override
            public String getDestinationRealm() throws AvpDataException {
                return subscriber.getServingMMERealm();
            }

            @Override
            public int getCommandCode() {
                return 316;
            }

            @Override
            public Message getMessage() {
                Message message = messageParser.createEmptyMessage(getCommandCode(), 16777251);
                message.getApplicationIdAvps().add(ApplicationId.createByAuthAppId(vendorId, 16777251));
                message.setRequest(true);
                try {
                    message.getAvps().addAvp(1, subscriber.getImsi().getBytes(StandardCharsets.UTF_8),false, false);
                    message.getAvps().addAvp(277, 1, false, false, true); // NO_STATE_MAINTAINED
                    message.getAvps().addAvp(Avp.DESTINATION_REALM, getDestinationRealm(), false);
                    message.getAvps().addAvp(Avp.DESTINATION_HOST, getDestinationHost(), false);
                    message.getAvps().addAvp(Avp.ORIGIN_REALM, getOriginRealm(), false);
                    message.getAvps().addAvp(Avp.ORIGIN_HOST, getOriginHost(), false);
                    if (apnProfile == null) {
                        message.getAvps().addAvp(1421, 1 << 1, 10415, false, false); // DSR-flags bit 1, Complete APN Configuration Profile Withdrawal
                    }
                    else {
                        message.getAvps().addAvp(1421, 1 << 3, 10415, false, false); // DSR-flags bit 3, PDN subscription contexts withdrawal
                        Optional<Map.Entry<Integer, String>> apnContext = subscriber.getApnContextMap().entrySet()
                                .stream().filter( apnContext1 -> apnContext1.getValue().equals(apnProfile.getApnName()))
                                .findFirst();
                        if (apnContext.isPresent()) {
                            message.getAvps().addAvp(1423, apnContext.get().getKey(), 10415, false, false);
                        }
                        else {
                            throw new APNContextException("An error occurred, couldn't find context of APN profile configuration " + apnProfile.getApnName() + " for IMSI " + subscriber.getImsi());
                        }
                    }
                } catch (AvpDataException | APNContextException e) {
                    Log.error("Couldn't generate AVP's for DSDR: ", e);
                    return null;
                }

                return message;
            }

            @Override
            public String getOriginHost() throws AvpDataException {
                return stack.getMetaData().getLocalPeer().getUri().getFQDN();
            }

            @Override
            public String getOriginRealm() throws AvpDataException {
                return stack.getMetaData().getLocalPeer().getRealmName();
            }
        };
    }
}
