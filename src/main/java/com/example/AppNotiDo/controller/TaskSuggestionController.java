package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.service.TaskSuggestionService;
import com.example.AppNotiDo.service.UserService; // ← AJOUTE
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // ← CHANGE
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks/suggestions")
@RequiredArgsConstructor
public class TaskSuggestionController {

    private final TaskSuggestionService taskSuggestionService;
    private final UserService userService; // ← AJOUTE

    @GetMapping("/by-date")
    public ResponseEntity<List<Task>> getSuggestionsForDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) { // ← CHANGE

        User user = userService.findByUsername(userDetails.getUsername()); // ← AJOUTE
        List<Task> suggestions = taskSuggestionService.getIncompleteTasksForDate(user, date);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/move-to-today")
    public ResponseEntity<Map<String, String>> moveTasksToToday(
            @RequestBody List<Long> taskIds,
            @AuthenticationPrincipal UserDetails userDetails) { // ← CHANGE

        User user = userService.findByUsername(userDetails.getUsername()); // ← AJOUTE
        taskSuggestionService.moveTasksToToday(taskIds, user);
        return ResponseEntity.ok(Map.of("message", "Tasks moved successfully"));
    }
}
