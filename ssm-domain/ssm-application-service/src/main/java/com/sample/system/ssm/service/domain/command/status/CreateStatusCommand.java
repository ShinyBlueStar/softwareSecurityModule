package com.sample.system.ssm.service.domain.command.status;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for creating a new status
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateStatusCommand {
    
    @NotNull(message = "Code is required")
    private final String code;
    
    @NotNull(message = "Description is required")
    private final String description;
    
    private final String persianDescription;
}
