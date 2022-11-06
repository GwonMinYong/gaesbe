package com.ssafy.gaese.domain.friends.entity;

import com.ssafy.gaese.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "FriendRequest")
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_date")
    private Date createdDate;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "requestUserId", referencedColumnName = "id")
    User requestUser;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "targetUserId", referencedColumnName = "id")
    User targetUser;

}
