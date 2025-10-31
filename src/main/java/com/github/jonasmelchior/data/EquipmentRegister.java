package com.github.jonasmelchior.data;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "equipment_register")
public class EquipmentRegister extends PanacheEntity {
    private String imsi;

    @ElementCollection
    @CollectionTable(
            name = "authorized_imei",
            joinColumns = @JoinColumn(name = "equipment_id")
    )
    @Column(name = "imei")
    private List<String> authorizedImei;

}
