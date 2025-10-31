package com.github.jonasmelchior.service.subscriber;


import com.github.jonasmelchior.data.APNProfile;
import com.github.jonasmelchior.data.Subscriber;
import com.github.jonasmelchior.repository.SubscriberRepository;
import com.github.jonasmelchior.service.s6a.S6aService;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.RouteException;

import java.util.ArrayList;

@ApplicationScoped
public class DatabaseSubscriberService implements SubscriberService{

    @Inject
    SubscriberRepository subscriberRepository;
    @Inject
    S6aService s6aService;

    @Override
    @Transactional
    public Subscriber getSubscriber(String imsi) {
        return subscriberRepository.findByImsi(imsi);
    }

    @Transactional
    public void saveSubscriber(Subscriber subscriber) {
        subscriberRepository.persist(subscriber);
    }

    @Transactional
    public void purgeUE(Subscriber subscriber) {
        subscriber.setServingMMERealm(null);
        subscriber.setServingMMEHost(null);
        subscriber.setPurged(true);
        subscriberRepository.persist(subscriber);
    }

    @Override
    public void withdrawSubscription(Subscriber subscriber) {
        try {
            s6aService.sendCancelLocationRequest(subscriber);
        } catch (IllegalDiameterStateException | InternalException | RouteException | OverloadException e) {
            Log.error("An error occurred sending Cancel Location Request of IMSI: " + subscriber.getImsi() + ": " + e.getMessage());
        }
    }

    @Override
    public void deleteSubscription(Subscriber subscriber, APNProfile apnProfile) {
        if (subscriber.getDefaultApn().getApnName().equals(apnProfile.getApnName())) {
            Log.error("Not allowed to delete default APN configuration");
            return;
        }
        else {
            try {
                s6aService.sendDeleteSubscriberDataRequest(subscriber, apnProfile);
            } catch (RouteException | IllegalDiameterStateException | OverloadException | InternalException e) {
                Log.error("An error occurred sending Delete Subscriber Data Request of IMSI: " + subscriber.getImsi() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void deleteAllSubscriptions(Subscriber subscriber) {
        try {
            s6aService.sendDeleteSubscriberDataRequest(subscriber, null);
        } catch (RouteException | IllegalDiameterStateException | OverloadException | InternalException e) {
            Log.error("An error occurred sending Delete Subscriber Data Request of IMSI: " + subscriber.getImsi() + ": " + e.getMessage());
        }
    }


    @Transactional
    public void updateSubscription(Subscriber subscriber) {
        try {
            s6aService.sendInsertSubscriberDataRequest(subscriber);
        } catch (IllegalDiameterStateException | InternalException | RouteException | OverloadException e) {
            Log.error("An error occurred sending Insert Subscriber Data Request of IMSI: " + subscriber.getImsi() + ": " + e.getMessage());
        }
    }
}
