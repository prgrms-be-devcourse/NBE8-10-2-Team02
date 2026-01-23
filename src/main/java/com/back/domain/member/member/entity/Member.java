package com.back.domain.member.member.entity;

import com.back.domain.game.game.entity.Game;
import com.back.domain.member.memberGame.entity.MemberGame;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.UUID;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {

    @Column(unique = true)
    private String email;
    private String password;

    @Column(unique = true)
    private String apiKey;

    @Column(unique = true, length = 30, nullable = false)
    private String nickname;

    @CreatedDate
    private LocalDateTime createDate;

    @LastModifiedDate
    private LocalDateTime modifyDate;

    @OneToMany(mappedBy = "member", fetch = LAZY, cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<MemberGame> library = new ArrayList<>();

    public Member(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.apiKey = UUID.randomUUID().toString();
    }
    public MemberGame addMemberGame(String platform, double playtime, boolean isFavorite, Game game) {
        MemberGame memberGame = new MemberGame(platform, playtime, isFavorite, this, game);
        library.add(memberGame);
        return memberGame;
    }
    public void updateMemberGame(Game game) {

    }

    public Optional<MemberGame> getMemberGameById(int id) {//make it so that it returns detailDto
                return library
                        .stream()
                        .filter(memberGame -> memberGame.getId() == id)
                        .findFirst();
    }

    public boolean removeGame(MemberGame memberGame) {
               if (memberGame == null) return false;
           return library.remove(memberGame);
    }
}