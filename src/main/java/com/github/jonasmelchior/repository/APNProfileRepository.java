package com.github.jonasmelchior.repository;

import com.github.jonasmelchior.data.APNProfile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class APNProfileRepository implements PanacheRepository<APNProfile> {
}
