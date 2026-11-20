package com.barnizgallery.backend.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.repository.OrderRepository;

/**
 * Readable unique order codes such as {@code MUS-261120-K7Q2XM}: the UTC date and six random characters without
 * the easily confused ones (0/O, 1/I/L).
 */
@Component
public class OrderCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyMMdd").withZone(ZoneOffset.UTC);

    private final SecureRandom random = new SecureRandom();
    private final OrderRepository orderRepository;
    private final Clock clock;

    public OrderCodeGenerator(OrderRepository orderRepository, Clock clock) {
        this.orderRepository = orderRepository;
        this.clock = clock;
    }

    public String next() {
        String code;
        do {
            StringBuilder suffix = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                suffix.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            code = "MUS-" + DATE.format(clock.instant()) + "-" + suffix;
        } while (orderRepository.existsByOrderCode(code));
        return code;
    }
}
