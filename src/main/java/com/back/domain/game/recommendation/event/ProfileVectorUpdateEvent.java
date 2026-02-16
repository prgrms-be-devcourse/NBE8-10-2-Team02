package com.back.domain.game.recommendation.event;

public record ProfileVectorUpdateEvent(
        int memberId,
        String reason
) {
}
