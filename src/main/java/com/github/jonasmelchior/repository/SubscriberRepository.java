package com.github.jonasmelchior.repository;

import com.github.jonasmelchior.data.Subscriber;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SubscriberRepository implements PanacheRepository<Subscriber> {
    public Subscriber findByImsi(String imsi) {
        return find("imsi", imsi).firstResult();
    }
}
