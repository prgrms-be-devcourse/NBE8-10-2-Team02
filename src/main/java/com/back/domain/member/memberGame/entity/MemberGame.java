package com.back.domain.member.memberGame.entity;

import com.back.domain.game.game.entity.Game;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.entity.Review;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = LAZY)
    private Member member;

    @ManyToOne(fetch = LAZY)
    private Game game;

    @ManyToOne(fetch = LAZY)
    private Review review;

    public MemberGame(String platform, double playtime, boolean isFavorite, Member member, Game game) {
        this.platform = platform;
        this.playtime = playtime;
        this.isFavorite = isFavorite;
        this.member = member;
        this.game = game;
        this.review = null;
    }

    public void updatePlatform(String platform){
        this.platform = platform;
    }

    public void updateIsFavorite(boolean isFavorite){
        this.isFavorite = isFavorite;
    }

    public void updatePlaytime(double playtime){
        this.playtime = playtime;
    }

    public void updateReview(Review review){
        this.review = review;
    }

    public void checkActorCanAccess(Member actor) {
        if (!member.equals(actor))
            throw new ServiceException("403-1", "%d번 게임에 대한 권한이 없습니다.".formatted(getId()));
    }
}
