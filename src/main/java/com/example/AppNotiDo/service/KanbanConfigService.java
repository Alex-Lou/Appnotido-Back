package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.KanbanConfig;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.KanbanConfigDTO;
import com.example.AppNotiDo.mapper.KanbanConfigMapper;
import com.example.AppNotiDo.repository.KanbanConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KanbanConfigService {

    private final KanbanConfigRepository kanbanConfigRepository;

    public KanbanConfigService(KanbanConfigRepository kanbanConfigRepository) {
        this.kanbanConfigRepository = kanbanConfigRepository;
    }

    @Transactional(readOnly = true)
    public KanbanConfigDTO getConfigForUser(User user) {
        return kanbanConfigRepository.findByUser(user)
                .map(KanbanConfigMapper::toDTO)
                .orElse(KanbanConfigMapper.getDefaultDTO());
    }

    @Transactional
    public KanbanConfigDTO saveConfig(User user, KanbanConfigDTO dto) {
        KanbanConfig config = kanbanConfigRepository.findByUser(user)
                .orElseGet(() -> {
                    KanbanConfig newConfig = new KanbanConfig();
                    newConfig.setUser(user);
                    return newConfig;
                });

        KanbanConfigMapper.updateFromDTO(config, dto);
        KanbanConfig saved = kanbanConfigRepository.save(config);

        return KanbanConfigMapper.toDTO(saved);
    }

    @Transactional
    public KanbanConfigDTO addTagColumn(User user, String tag) {
        KanbanConfigDTO config = getConfigForUser(user);

        if (!config.getActiveTagColumns().contains(tag)) {
            config.getActiveTagColumns().add(tag);
            config.getColumnsOrder().add(tag);
        }

        return saveConfig(user, config);
    }

    @Transactional
    public KanbanConfigDTO removeTagColumn(User user, String tag) {
        KanbanConfigDTO config = getConfigForUser(user);

        config.getActiveTagColumns().remove(tag);
        config.getColumnsOrder().remove(tag);

        return saveConfig(user, config);
    }

    @Transactional
    public KanbanConfigDTO toggleStatusColumn(User user, String status) {
        KanbanConfigDTO config = getConfigForUser(user);

        if (config.getVisibleStatusColumns().contains(status)) {
            // Ne pas permettre de masquer toutes les colonnes
            if (config.getVisibleStatusColumns().size() > 1) {
                config.getVisibleStatusColumns().remove(status);
            }
        } else {
            config.getVisibleStatusColumns().add(status);
        }

        return saveConfig(user, config);
    }

    @Transactional
    public KanbanConfigDTO resetToDefault(User user) {
        KanbanConfigDTO defaultConfig = KanbanConfigMapper.getDefaultDTO();
        return saveConfig(user, defaultConfig);
    }
}