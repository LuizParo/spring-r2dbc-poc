package me.github.lgparo.r2dbc.service;

import lombok.AllArgsConstructor;
import me.github.lgparo.r2dbc.domain.Parent;
import me.github.lgparo.r2dbc.repository.ChildRepository;
import me.github.lgparo.r2dbc.repository.ParentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ParentService {
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;

    public Mono<Parent> save(Parent parent) {
        return parentRepository.save(parent);
    }

    public Mono<Parent> findById(String id) {
        return parentRepository.findById(id);
    }

    public Flux<Parent> findAll() {
        return parentRepository.findAll();
    }
}
