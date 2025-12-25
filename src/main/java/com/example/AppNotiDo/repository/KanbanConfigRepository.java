package com.example.AppNotiDo.repository;

import com.example.AppNotiDo.domain.KanbanConfig;
import com.example.AppNotiDo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KanbanConfigRepository extends JpaRepository<KanbanConfig, Long> {

    Optional<KanbanConfig> findByUser(User user);

    Optional<KanbanConfig> findByUserId(Long userId);

}