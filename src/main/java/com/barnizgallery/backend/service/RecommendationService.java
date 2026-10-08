package com.barnizgallery.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.response.RecommendationResponse;
import com.barnizgallery.backend.mapper.InteractionMapper;
import com.barnizgallery.backend.model.entity.Recommendation;
import com.barnizgallery.backend.model.entity.Room;
import com.barnizgallery.backend.model.entity.Visitor;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.patterns.strategy.RecommendationResult;
import com.barnizgallery.backend.repository.RecommendationRepository;
import com.barnizgallery.backend.repository.RoomRepository;

/**
 * Computes and stores room recommendations. The algorithm is chosen through the
 * {@link AiFacade} (which uses the Strategy pattern).
 */
@Service
public class RecommendationService {

    private final AiFacade aiFacade;
    private final RecommendationRepository recommendationRepository;
    private final RoomRepository roomRepository;
    private final VisitorService visitorService;

    public RecommendationService(AiFacade aiFacade, RecommendationRepository recommendationRepository,
            RoomRepository roomRepository, VisitorService visitorService) {
        this.aiFacade = aiFacade;
        this.recommendationRepository = recommendationRepository;
        this.roomRepository = roomRepository;
        this.visitorService = visitorService;
    }

    /** Computes new recommendations, replaces the previous ones and returns them by score. */
    @Transactional
    public List<RecommendationResponse> compute(Integer visitorId, String strategyName) {
        Visitor visitor = visitorService.getVisitor(visitorId);
        List<RecommendationResult> results = aiFacade.recommendRooms(visitor, strategyName);

        recommendationRepository.deleteByVisitorId(visitorId);
        Map<Integer, Room> rooms = roomRepository.findAllById(results.stream().map(RecommendationResult::roomId)
                .toList()).stream().collect(Collectors.toMap(Room::getRoomId, Function.identity()));

        LocalDateTime now = LocalDateTime.now();
        List<Recommendation> saved = new ArrayList<>();
        for (RecommendationResult result : results) {
            Room room = rooms.get(result.roomId());
            if (room == null) {
                continue;
            }
            Recommendation recommendation = new Recommendation();
            recommendation.setVisitor(visitor);
            recommendation.setRoom(room);
            recommendation.setScore(result.score());
            recommendation.setReason(result.reason());
            recommendation.setRecommendationDate(now);
            saved.add(recommendation);
        }
        return recommendationRepository.saveAll(saved).stream()
                .map(InteractionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> latest(Integer visitorId) {
        visitorService.getVisitor(visitorId);
        return recommendationRepository.findByVisitorWithRoom(visitorId).stream()
                .map(InteractionMapper::toResponse)
                .toList();
    }
}
