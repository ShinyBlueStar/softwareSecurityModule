package com.sample.system.ssm.service.domain.handler.query;

import com.sample.system.ssm.service.domain.model.Status;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.mapper.StatusDataMapper;
import com.sample.system.ssm.service.domain.ports.input.service.StatusService;
import com.sample.system.ssm.service.domain.response.status.StatusListResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class StatusQueryHandler {

    private final StatusDataMapper statusDataMapper;
    private final StatusService statusService;

    public StatusListResponse listStatuses(Map<String, String> map, String caller, String ip) throws SsmDomainException {
        Page<Status> responses = statusService.listStatuses(map, caller, ip);
        log.info("Statuses retrieval completed by QueryHandler - Count: {}",
                responses != null ? responses.getContent().size() : 0);
        return statusDataMapper.statusesToGetListResponse(responses);
    }
}

