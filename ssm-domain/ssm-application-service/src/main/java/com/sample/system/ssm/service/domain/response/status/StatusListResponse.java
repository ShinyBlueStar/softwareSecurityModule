package com.sample.system.ssm.service.domain.response.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sample.system.ssm.service.domain.response.base.PageResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class StatusListResponse extends PageResponse {

    @JsonProperty("list")
    private List<GetStatusResponse> list;
}

