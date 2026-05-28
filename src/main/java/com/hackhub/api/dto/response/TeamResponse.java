package com.hackhub.api.dto.response;

import java.util.List;

public record TeamResponse(Long id, String name, Long createdByUserId, List<Long> memberIds) {
}
