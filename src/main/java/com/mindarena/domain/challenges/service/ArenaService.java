package com.mindarena.domain.challenges.service;

import com.mindarena.domain.challenges.model.Arena;
import com.mindarena.domain.challenges.model.ArenaMembership;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.challenges.repository.ArenaMembershipRepository;
import com.mindarena.domain.challenges.repository.ArenaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ArenaService {

    private final ArenaRepository arenaRepository;
    private final ArenaMembershipRepository membershipRepository;

    public ArenaService(ArenaRepository arenaRepository, ArenaMembershipRepository membershipRepository) {
        this.arenaRepository = arenaRepository;
        this.membershipRepository = membershipRepository;
    }

    public List<Arena> findAll() {
        return arenaRepository.findAll();
    }

    public Arena requireArena(Long id) {
        return arenaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Arena not found"));
    }

    public void join(User user, Long arenaId) {
        Arena arena = requireArena(arenaId);
        if (!membershipRepository.existsByUserAndArena(user, arena)) {
            membershipRepository.save(new ArenaMembership(user, arena));
        }
    }

    public List<ArenaMembership> memberships(User user) {
        return membershipRepository.findByUser(user);
    }
}
