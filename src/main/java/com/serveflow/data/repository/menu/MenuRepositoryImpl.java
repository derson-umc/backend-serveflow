package com.serveflow.data.repository.menu;

import com.serveflow.data.mapper.MenuMapper;
import com.serveflow.domain.exception.MenuNotFoundException;
import com.serveflow.domain.model.menu.Menu;
import com.serveflow.domain.repository.MenuRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class MenuRepositoryImpl implements MenuRepository {

    private final SpringMenuRepository springRepository;
    private final MenuMapper mapper;

    public MenuRepositoryImpl(SpringMenuRepository springRepository, MenuMapper mapper) {
        this.springRepository = springRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Menu save(Menu menu) {
        boolean isNew = menu.getVersion() == null;
        var entity = isNew
                ? mapper.toEntity(menu)
                : mapper.updateEntity(
                    springRepository.findById(menu.getId())
                        .orElseThrow(() -> new MenuNotFoundException(menu.getId())),
                    menu);

        var saved = springRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Menu findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new MenuNotFoundException(id));
    }

    @Override
    public List<Menu> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
