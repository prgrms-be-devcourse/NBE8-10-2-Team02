package com.back.domain.member.memberGame.entity;

import com.back.domain.game.game.entity.Game;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.memberGame.StatusEnum;
import com.back.domain.review.entity.Review;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor
public class MemberGame extends BaseEntity {

    private String platform;
    private double playtime;
    private boolean isFavorite;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @ManyToOne(fetch = LAZY)
    private Member member;

    @ManyToOne(fetch = LAZY)
    private Game game;

    @OneToOne(fetch = LAZY)
    private Review review;

    public MemberGame(String platform, double playtime, boolean isFavorite, StatusEnum status, Member member, Game game) {
        this.platform = platform;
        this.playtime = playtime;
        this.isFavorite = isFavorite;
        this.status = status;
        this.member = member;
        this.game = game;
        this.review = null;
    }
    public void checkActorCanAccess(Member actor) {
        if (!member.equals(actor))
            throw new ServiceException("403-1", "%d번 게임에 대한 권한이 없습니다.".formatted(getId()));
    }

    // Specific setters for mutable fields only
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setPlaytime(double playtime) {
        this.playtime = playtime;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public void setReview(Review review) {
        this.review = review;
    }
}
