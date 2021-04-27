package me.github.lgparo.r2dbc.service;

import lombok.AllArgsConstructor;
import me.github.lgparo.r2dbc.domain.Parent;
import me.github.lgparo.r2dbc.repository.ChildRepository;
import me.github.lgparo.r2dbc.repository.ParentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ParentService {
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;

    @Transactional(rollbackFor = Throwable.class)
    public Mono<Void> save(Parent parent) {
        return parentRepository
                .save(parent)
                .then(childRepository.save(parent.getChildren()))
                .then();
    }

    public Flux<Parent> findAll() {
        return parentRepository.findAll();
    }
}
