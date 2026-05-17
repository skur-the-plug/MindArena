package com.mindarena.service;

import com.mindarena.model.PlayerRank;
import org.springframework.stereotype.Service;

@Service
public class RankService {

    public PlayerRank rankForScore(int score) {
        return PlayerRank.fromScore(score);
    }
}
