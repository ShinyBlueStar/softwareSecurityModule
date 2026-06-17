package com.sample.system.ssm.service.domain.response.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {

    private Long requestId;
    private String timestamp;
    private Integer currentPage;
    private Integer PageSize;
    private Long totalElement;
    private Integer totalPages;
}
