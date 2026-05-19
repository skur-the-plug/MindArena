package com.mindarena.domain.rankings.service;

import com.mindarena.domain.rankings.model.PlayerRank;
import org.springframework.stereotype.Service;

@Service
public class RankService {

    public PlayerRank rankForScore(int score) {
        return PlayerRank.fromScore(score);
    }
}
