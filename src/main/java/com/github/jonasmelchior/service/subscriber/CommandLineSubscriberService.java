package com.github.jonasmelchior.service.subscriber;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.jonasmelchior.data.APNProfile;
import com.github.jonasmelchior.data.Subscriber;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
@IfBuildProfile("test-json")  // active for dev profile
public class CommandLineSubscriberService implements SubscriberService{

    private Subscriber subscriber;

    @Inject
    @ConfigProperty(name = "subscriber.data")
    String subscriberDataPath;

    @Override
    public Subscriber getSubscriber(String imsi) {
        try {
            Path path = Path.of(subscriberDataPath);
            String json = Files.readString(path);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            this.subscriber = mapper.readValue(json, Subscriber.class);

            Log.infof("Loaded subscriber data from %s", subscriberDataPath);
        } catch (Exception e) {
            Log.errorf("Failed to load subscriber data from %s: %s", subscriberDataPath, e.getMessage());
        }

        return this.subscriber;
    }

    @Override
    public void saveSubscriber(Subscriber subscriber) {

    }

    @Override
    public void purgeUE(Subscriber subscriber) {

    }

    @Override
    public void updateSubscription(Subscriber subscriber) {

    }

    @Override
    public void withdrawSubscription(Subscriber subscriber) {

    }

    @Override
    public void deleteSubscription(Subscriber subscriber, APNProfile apnProfile) {

    }

    @Override
    public void deleteAllSubscriptions(Subscriber subscriber) {

    }

}
