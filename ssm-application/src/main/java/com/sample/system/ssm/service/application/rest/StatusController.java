package com.sample.system.ssm.service.application.rest;


import com.sample.system.ssm.service.domain.handler.command.StatusCommandHandler;
import com.sample.system.ssm.service.domain.command.base.BaseSearchQuery;
import com.sample.system.ssm.service.domain.command.status.CreateStatusCommand;
import com.sample.system.ssm.service.domain.command.status.UpdateStatusCommand;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.handler.query.StatusQueryHandler;
import com.sample.system.ssm.service.domain.response.status.CreateStatusResponse;
import com.sample.system.ssm.service.domain.response.status.StatusListResponse;
import com.sample.system.ssm.service.domain.response.status.UpdateStatusResponse;
import com.sample.system.ssm.service.domain.response.base.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/ssm/status", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Status Management", description = "API for managing status messages")
public class StatusController {

    private final StatusCommandHandler statusCommandHandler;
    private final StatusQueryHandler statusQueryHandler;

    @PostMapping(path = "/create")
    @Operation(
            security = {@SecurityRequirement(name = "bearerAuth")},
            summary = "Create status",
            description = "Create a new status with persian description"
    )
    public ResponseEntity<BaseResponse<CreateStatusResponse>> createStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON payload for creating a new status",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "1000",
                                              "description": "Sample description",
                                              "persianDescription": "توضیح وضعیت"
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid CreateStatusCommand command) throws SsmDomainException {

        log.info("Creating status with reqBody: {}", command);
        CreateStatusResponse response = statusCommandHandler.createStatus(command);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @PutMapping(path = "/update")
    @Operation(
            security = {@SecurityRequirement(name = "bearerAuth")},
            summary = "Update status persianDescription",
            description = "Update persianDescription for an existing status using its code"
    )
    public ResponseEntity<BaseResponse<UpdateStatusResponse>> updateStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON payload for updating status persianDescription",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "1000",
                                              "persianDescription": "توضیح وضعیت به‌روزرسانی شده"
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid UpdateStatusCommand command) throws SsmDomainException {

        log.info("Updating status persianDescription for body: {}", command);
        UpdateStatusResponse response = statusCommandHandler.updateStatusPersianDescription(command);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @PostMapping(path = "/list")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "جستجوی وضعیت",
            description = "Search and list statuses with pagination and filters")
    public ResponseEntity<BaseResponse<StatusListResponse>> listStatuses(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای جستجو",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                              {
                                                "parameterMap": {
                                                  "id": "1",
                                                  "code": "1000",
                                                  "description": "Success",
                                                  "persianDescription": "موفق",
                                                  "page": "0",
                                                  "size": "10"
                                                }
                                              }
                                            """
                            )
                    )
            ) BaseSearchQuery baseSearchQuery) throws SsmDomainException {
        log.info("StatusController.listStatuses requestBody={}", baseSearchQuery);
        var map = baseSearchQuery != null ? baseSearchQuery.getMap() : new java.util.HashMap<String, String>();
        StatusListResponse listResponse = statusQueryHandler.listStatuses(map, "", "");
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, listResponse));
    }
}
