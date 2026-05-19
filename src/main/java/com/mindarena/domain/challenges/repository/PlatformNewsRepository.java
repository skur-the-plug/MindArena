package com.mindarena.domain.challenges.repository;

import com.mindarena.domain.challenges.model.PlatformNews;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformNewsRepository extends JpaRepository<PlatformNews, Long> {
    List<PlatformNews> findTop20ByOrderByCreatedAtDesc();
}
