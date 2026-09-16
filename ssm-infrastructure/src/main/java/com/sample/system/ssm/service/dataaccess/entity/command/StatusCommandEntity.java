package com.sample.system.ssm.service.dataaccess.entity.command;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Status")
@Entity
public class StatusCommandEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ssm_status_seq")
    @SequenceGenerator(name = "ssm_status_seq", sequenceName = "ssm_status_seq", allocationSize = 1)
    private Long id;

    private String code;
    private String description;
    private String persianDescription;
}