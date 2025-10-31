package com.github.jonasmelchior.service.subscriber;

import com.github.jonasmelchior.data.APNProfile;
import com.github.jonasmelchior.data.Subscriber;

public interface SubscriberService {
    Subscriber getSubscriber(String imsi);
    void saveSubscriber(Subscriber subscriber);
    void purgeUE(Subscriber subscriber);
    void updateSubscription(Subscriber subscriber);
    void withdrawSubscription(Subscriber subscriber);
    void deleteSubscription(Subscriber subscriber, APNProfile apnProfile);
    void deleteAllSubscriptions(Subscriber subscriber);

}
