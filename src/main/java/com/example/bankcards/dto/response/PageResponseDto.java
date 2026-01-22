package com.example.bankcards.dto.response;

import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages
) {}