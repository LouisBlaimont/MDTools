package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

/**
 * Generic page response DTO (server-side pagination).
 *
 * @param content page items
 * @param totalElements total items count
 * @param totalPages total pages count
 * @param number current page index (0-based)
 * @param size page size
 */
public record PageResponseDTO<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {}
