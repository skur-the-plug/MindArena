package com.mindarena.repository;

import com.mindarena.model.PlatformNews;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformNewsRepository extends JpaRepository<PlatformNews, Long> {
    List<PlatformNews> findTop20ByOrderByCreatedAtDesc();
}
