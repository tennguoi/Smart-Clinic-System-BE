package com.example.demo.service;

import com.example.demo.dto.DoctorStatsDetailItem;
import com.example.demo.dto.DoctorStatsDetailResponse;
import com.example.demo.dto.DoctorStatsKpis;
import com.example.demo.dto.DoctorStatsResponse;
import com.example.demo.dto.DoctorStatsSeriesItem;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Queue;
import com.example.demo.entity.Queue.QueueStatus;
import com.example.demo.enums.DoctorStatsRange;
import com.example.demo.repository.QueueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoctorStatsService {

    private static final Logger log = LoggerFactory.getLogger(DoctorStatsService.class);
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    private final QueueRepository queueRepository;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final Duration cacheTtl;

    public DoctorStatsService(QueueRepository queueRepository,
                              ObjectMapper objectMapper,
                              ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                              @Value("${app.stats.cache.ttl-minutes:15}") long cacheTtlMinutes) {
        this.queueRepository = queueRepository;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        this.cacheTtl = Duration.ofMinutes(Math.max(cacheTtlMinutes, 1));
    }

    @Transactional(readOnly = true)
    public DoctorStatsResponse getDoctorStats(UUID doctorId, DoctorStatsRange range, LocalDate referenceDate) {
        LocalDate ref = referenceDate != null ? referenceDate : LocalDate.now(SYSTEM_ZONE);
        DateRange window = resolveRange(range, ref);

        if (range == DoctorStatsRange.MONTH) {
            String cacheKey = buildCacheKey(doctorId, range, ref);
            DoctorStatsResponse cached = readCache(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        List<Queue> queues = queueRepository.findDoctorQueuesWithinRange(
                doctorId,
                window.start(),
                window.end(),
                QueueStatus.Cancelled
        );

        DoctorStatsResponse response = buildResponse(queues, range, window);

        if (range == DoctorStatsRange.MONTH) {
            writeCache(buildCacheKey(doctorId, range, ref), response);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public DoctorStatsDetailResponse getDoctorStatsDetail(UUID doctorId,
                                                          DoctorStatsRange range,
                                                          LocalDate referenceDate,
                                                          int page,
                                                          int pageSize) {
        LocalDate ref = referenceDate != null ? referenceDate : LocalDate.now(SYSTEM_ZONE);
        DateRange window = resolveRange(range, ref);

        int safePage = Math.max(page - 1, 0);
        int safeSize = pageSize <= 0 ? 10 : Math.min(pageSize, 200);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "checkInTime"));

        Page<Queue> queuePage = queueRepository.findDoctorQueuesWithinRange(
                doctorId,
                window.start(),
                window.end(),
                QueueStatus.Cancelled,
                pageable
        );

        List<DoctorStatsDetailItem> items = queuePage.getContent().stream()
                .map(this::mapToDetailItem)
                .collect(Collectors.toList());

        return DoctorStatsDetailResponse.builder()
                .items(items)
                .totalItems(queuePage.getTotalElements())
                .build();
    }

    private DoctorStatsResponse buildResponse(List<Queue> queues, DoctorStatsRange range, DateRange window) {
        Map<Integer, BucketAggregate> bucketMap = new HashMap<>();
        long totalScheduled = 0;
        long totalCompleted = 0;
        long durationSum = 0;
        long durationCount = 0;

        for (Queue queue : queues) {
            LocalDateTime bucketTimestamp = resolveBucketTimestamp(queue);
            if (bucketTimestamp == null) {
                continue;
            }

            int bucketKey = computeBucketKey(range, window, bucketTimestamp);
            if (bucketKey < 0) {
                continue;
            }

            BucketAggregate aggregate = bucketMap.computeIfAbsent(bucketKey, k -> new BucketAggregate());
            aggregate.scheduled++;
            totalScheduled++;

            if (queue.getStatus() == QueueStatus.Completed) {
                aggregate.completed++;
                totalCompleted++;
            }

            Long duration = calculateDuration(queue);
            if (duration != null) {
                aggregate.durationSum += duration;
                aggregate.durationCount++;

                if (queue.getStatus() == QueueStatus.Completed) {
                    durationSum += duration;
                    durationCount++;
                }
            }
        }

        List<DoctorStatsSeriesItem> series = bucketMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> mapToSeriesItem(entry.getKey(), entry.getValue(), range, window))
                .collect(Collectors.toList());

        markPeakNotes(series, range);

        DoctorStatsKpis kpis = DoctorStatsKpis.builder()
                .totalVisits(totalCompleted)
                .averageDurationMinutes(durationCount == 0 ? null : roundTo(durationSum / (double) durationCount, 1))
                .completionRate(totalScheduled == 0 ? null : roundTo(totalCompleted / (double) totalScheduled, 2))
                .build();

        return DoctorStatsResponse.builder()
                .kpis(kpis)
                .series(series)
                .table(series)
                .totalItems(totalScheduled)
                .build();
    }

    private void markPeakNotes(List<DoctorStatsSeriesItem> series, DoctorStatsRange range) {
        if (series.isEmpty()) {
            return;
        }
        long maxVisits = series.stream()
                .mapToLong(DoctorStatsSeriesItem::getVisits)
                .max()
                .orElse(0);
        if (maxVisits <= 0) {
            return;
        }
        String note = range == DoctorStatsRange.DAY ? "Giờ cao điểm" : "Ngày cao điểm";
        series.stream()
                .filter(item -> item.getVisits() == maxVisits)
                .forEach(item -> item.setNote(note));
    }

    private DoctorStatsSeriesItem mapToSeriesItem(int bucketKey,
                                                  BucketAggregate aggregate,
                                                  DoctorStatsRange range,
                                                  DateRange window) {
        Double avg = aggregate.durationCount == 0
                ? null
                : roundTo(aggregate.durationSum / (double) aggregate.durationCount, 1);
        Double completion = aggregate.scheduled == 0
                ? null
                : roundTo(aggregate.completed / (double) aggregate.scheduled, 2);

        return DoctorStatsSeriesItem.builder()
                .label(formatLabel(range, window, bucketKey))
                .visits(aggregate.scheduled)
                .avgTime(avg)
                .completion(completion)
                .build();
    }

    private DoctorStatsDetailItem mapToDetailItem(Queue queue) {
        Patient patient = queue.getPatient();
        Long duration = calculateDuration(queue);
        String status = queue.getStatus() != null ? queue.getStatus().name() : null;

        return DoctorStatsDetailItem.builder()
                .queueId(queue.getQueueId())
                .patientName(patient != null ? patient.getFullName() : null)
                .patientPhone(patient != null ? patient.getPhone() : null)
                .status(status)
                .roomName(queue.getAssignedRoom() != null ? queue.getAssignedRoom().getRoomName() : null)
                .checkInTime(formatDateTime(queue.getCheckInTime()))
                .startTime(formatDateTime(queue.getStartTime()))
                .endTime(formatDateTime(queue.getEndTime()))
                .durationMinutes(duration)
                .note(status != null && status.equals(QueueStatus.Completed.name()) && duration != null && duration < 10
                        ? "Xử lý nhanh"
                        : null)
                .build();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private int computeBucketKey(DoctorStatsRange range, DateRange window, LocalDateTime timestamp) {
        return switch (range) {
            case DAY -> {
                int hour = timestamp.getHour();
                yield Math.max(0, Math.min(23, hour)); // Đảm bảo trong khoảng 0-23
            }
            case WEEK -> {
                long days = ChronoUnit.DAYS.between(window.start().toLocalDate(), timestamp.toLocalDate());
                yield (int) Math.max(0, Math.min(6, days)); // Đảm bảo trong khoảng 0-6 (7 ngày)
            }
            case MONTH -> {
                LocalDate timestampDate = timestamp.toLocalDate();
                LocalDate windowStart = window.start().toLocalDate();
                // Chỉ tính nếu cùng tháng, năm với window
                if (timestampDate.getYear() == windowStart.getYear() && 
                    timestampDate.getMonth() == windowStart.getMonth()) {
                    yield timestampDate.getDayOfMonth() - 1;
                }
                yield -1; // Ngoài phạm vi, sẽ bị skip
            }
        };
    }

    private String formatLabel(DoctorStatsRange range, DateRange window, int bucketKey) {
        return switch (range) {
            case DAY -> {
                if (bucketKey < 0 || bucketKey > 23) {
                    yield "00:00"; // Fallback
                }
                yield window.start().plusHours(bucketKey).format(HOUR_FORMATTER);
            }
            case WEEK -> {
                if (bucketKey < 0 || bucketKey > 6) {
                    yield window.start().toLocalDate().format(DAY_FORMATTER); // Fallback
                }
                yield window.start().toLocalDate().plusDays(bucketKey).format(DAY_FORMATTER);
            }
            case MONTH -> {
                if (bucketKey < 0) {
                    yield window.start().toLocalDate().format(DAY_FORMATTER); // Fallback
                }
                yield window.start().toLocalDate().plusDays(bucketKey).format(DAY_FORMATTER);
            }
        };
    }

    private LocalDateTime resolveBucketTimestamp(Queue queue) {
        if (queue.getStartTime() != null) {
            return queue.getStartTime();
        }
        if (queue.getCheckInTime() != null) {
            return queue.getCheckInTime();
        }
        if (queue.getCreatedAt() != null) {
            return queue.getCreatedAt();
        }
        return null; // Trả về null nếu không có timestamp nào
    }

    private Long calculateDuration(Queue queue) {
        if (queue.getStartTime() == null || queue.getEndTime() == null) {
            return null;
        }
        long minutes = Duration.between(queue.getStartTime(), queue.getEndTime()).toMinutes();
        return minutes < 0 ? null : minutes;
    }

    private DateRange resolveRange(DoctorStatsRange range, LocalDate reference) {
        return switch (range) {
            case DAY -> new DateRange(reference.atStartOfDay(), reference.plusDays(1).atStartOfDay());
            case WEEK -> {
                LocalDate startOfWeek = reference.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new DateRange(startOfWeek.atStartOfDay(), startOfWeek.plusDays(7).atStartOfDay());
            }
            case MONTH -> {
                LocalDate firstDay = reference.withDayOfMonth(1);
                yield new DateRange(firstDay.atStartOfDay(), firstDay.plusMonths(1).atStartOfDay());
            }
        };
    }

    private DoctorStatsResponse readCache(String cacheKey) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) {
                return null;
            }
            return objectMapper.readValue(cached, DoctorStatsResponse.class);
        } catch (Exception ex) {
            log.warn("Failed to read doctor stats cache {}", cacheKey, ex);
            return null;
        }
    }

    private void writeCache(String cacheKey, DoctorStatsResponse response) {
        if (redisTemplate == null) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, payload, cacheTtl);
        } catch (Exception ex) {
            log.warn("Failed to write doctor stats cache {}", cacheKey, ex);
        }
    }

    private String buildCacheKey(UUID doctorId, DoctorStatsRange range, LocalDate reference) {
        return String.format("doctor-stats:%s:%s:%s", range.name().toLowerCase(), doctorId, reference);
    }

    private Double roundTo(double value, int scale) {
        return BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {}

    private static class BucketAggregate {
        private long scheduled;
        private long completed;
        private long durationSum;
        private long durationCount;
    }
}

