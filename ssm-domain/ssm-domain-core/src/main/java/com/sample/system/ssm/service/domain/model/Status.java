package com.sample.system.ssm.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Status extends BaseEntity<Long> {

    private String persianDescription;
    private String code;
    private String description;

}
