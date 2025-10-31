package com.github.jonasmelchior.repository;

import com.github.jonasmelchior.data.EquipmentRegister;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EquipmentRegisterRepository implements PanacheRepository<EquipmentRegister> {
    public EquipmentRegister findByImsi(String imsi) {
        return find("imsi", imsi).firstResult();
    }
}
