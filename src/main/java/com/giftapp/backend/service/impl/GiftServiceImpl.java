package com.giftapp.backend.service.impl;

import com.giftapp.backend.config.RecommendationConfig;
import com.giftapp.backend.dto.GiftDTO;
import com.giftapp.backend.dto.RecommendationRequest;
import com.giftapp.backend.dto.RecommendationResponse;
import com.giftapp.backend.entity.Gift;
import com.giftapp.backend.repository.GiftRepository;
import com.giftapp.backend.service.GiftService;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Service
public class GiftServiceImpl implements GiftService {

    private final GiftRepository giftRepository;
    private final RecommendationConfig recommendationConfig;

    public GiftServiceImpl(GiftRepository giftRepository, RecommendationConfig recommendationConfig) {
        this.giftRepository = giftRepository;
        this.recommendationConfig = recommendationConfig;
    }

    @Override
    public GiftDTO addGift(GiftDTO dto) {
        if (giftRepository.existsByCategoryIgnoreCaseAndColourIgnoreCaseAndNameIgnoreCase(
                dto.getCategory(), dto.getColour(), dto.getName())) {
            throw new RuntimeException(
                    "A gift with category '" + dto.getCategory() +
                    "', colour '" + dto.getColour() +
                    "' and name '" + dto.getName() + "' already exists.");
        }
        Gift gift = new Gift();
        gift.setName(dto.getName());
        gift.setCategory(dto.getCategory());
        gift.setColour(dto.getColour());
        gift.setPrice(dto.getPrice());
        gift.setGender(dto.getGender());
        gift.setOccasion(dto.getOccasion());
        gift.setMinAge(dto.getMinAge());
        gift.setMaxAge(dto.getMaxAge());

        Gift saved = giftRepository.save(gift);
        return convertToDTO(saved);
    }

    private GiftDTO convertToDTO(Gift gift) {
        GiftDTO dto = new GiftDTO();
        dto.setId(gift.getId());
        dto.setName(gift.getName());
        dto.setCategory(gift.getCategory());
        dto.setColour(gift.getColour());
        dto.setPrice(gift.getPrice());
        dto.setGender(gift.getGender());
        dto.setOccasion(gift.getOccasion());
        dto.setMinAge(gift.getMinAge());
        dto.setMaxAge(gift.getMaxAge());
        return dto;
    }

    @Override
    public Page<GiftDTO> searchGifts(String name, Double minPrice, Double maxPrice, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Gift> gifts;

        if (name != null) {
            gifts = giftRepository.findByNameContainingIgnoreCase(name, pageRequest);
        } else if (minPrice != null && maxPrice != null) {
            gifts = giftRepository.findByPriceBetween(minPrice, maxPrice, pageRequest);
        } else {
            gifts = giftRepository.findAll(pageRequest);
        }

        return gifts.map(this::convertToDTO);
    }

    @Override
    public List<RecommendationResponse> recommendGifts(RecommendationRequest request) {
        return giftRepository.findAll().stream()
                .map(gift -> {
                    int score = calculateScore(gift, request);
                    return new RecommendationResponse(
                            gift.getId(),
                            gift.getName(),
                            gift.getCategory(),
                            gift.getColour(),
                            gift.getPrice(),
                            score
                    );
                })
                // remove weak matches
                .filter(r -> r.getScore() > recommendationConfig.getMinScore())
                // sort descending
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                // limit results
                .limit(5)
                .toList();
    }

    private int calculateScore(Gift gift, RecommendationRequest req) {
        int score = 0;

        // Gender
        if (gift.getGender() != null && req.getGender() != null) {
            if (gift.getGender().equalsIgnoreCase(req.getGender())) {
                score += recommendationConfig.getGenderWeight();
            } else if (gift.getGender().equalsIgnoreCase("UNISEX")) {
                score += recommendationConfig.getUnisexWeight();
            }
        }

        // Occasion
        if (gift.getOccasion() != null && req.getOccasion() != null) {
            if (gift.getOccasion().equalsIgnoreCase(req.getOccasion())) {
                score += recommendationConfig.getOccasionWeight();
            }
        }

        // Age
        if (gift.getMinAge() != null && gift.getMaxAge() != null) {
            if (req.getAge() >= gift.getMinAge() && req.getAge() <= gift.getMaxAge()) {
                score += recommendationConfig.getAgeWeight();
            }
        }

        // Budget
        if (gift.getPrice() != null) {
            if (req.getBudget() != null && gift.getPrice() <= req.getBudget()) {
                score += recommendationConfig.getBudgetWeight();
            }
        }

        // Colour
        if (req.getColour() != null &&
                gift.getColour() != null &&
                gift.getColour().equalsIgnoreCase(req.getColour())) {
            score += recommendationConfig.getColourWeight();
        }

        return score;
    }
}