package com.example.AppNotiDo.repository;

import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.TaskPriority;
import com.example.AppNotiDo.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatusAndUserId(TaskStatus status, Long userId);

    List<Task> findByPriorityAndUserId(TaskPriority priority, Long userId);

    List<Task> findByStatusAndPriorityAndUserId(TaskStatus status, TaskPriority priority, Long userId);

    Page<Task> findByUserId(Long userId, Pageable pageable);
}