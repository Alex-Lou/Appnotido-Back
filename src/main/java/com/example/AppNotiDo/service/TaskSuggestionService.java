package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.TaskStatus;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskSuggestionService {

    private final TaskRepository taskRepository;

    public List<Task> getIncompleteTasksForDate(User user, LocalDate targetDate) {
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        System.out.println("=== DEBUG SUGGESTIONS ===");
        System.out.println("User: " + user.getUsername());
        System.out.println("Date range: " + startOfDay + " -> " + endOfDay);

        // Trouver TOUTES les tâches réactivables échues (même avant hier)
        List<Task> result = taskRepository.findByUserAndDueDateBeforeAndStatusNotAndReactivableTrue(
                user,
                LocalDateTime.now(), // Toutes les dates passées
                TaskStatus.DONE
        );

        System.out.println("Résultats trouvés: " + result.size());
        result.forEach(t -> System.out.println("  - " + t.getTitle() + " dueDate=" + t.getDueDate()));

        return result;
    }



    @Transactional
    public void moveTasksToToday(List<Long> taskIds, User user) {
        List<Task> tasks = taskRepository.findAllById(taskIds);

        List<Task> tasksToUpdate = tasks.stream()
                .filter(task -> task.getUser().getId().equals(user.getId()))
                .peek(task -> {
                    task.setDueDate(null);
                    task.setReactivable(false);
                })
                .collect(Collectors.toList());

        taskRepository.saveAll(tasksToUpdate);
    }

}
