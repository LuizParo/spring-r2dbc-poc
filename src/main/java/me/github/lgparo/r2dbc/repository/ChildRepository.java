package me.github.lgparo.r2dbc.repository;

import lombok.AllArgsConstructor;
import me.github.lgparo.r2dbc.domain.Child;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
@AllArgsConstructor
public class ChildRepository {
    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate template;

    public Flux<Child> save(Collection<Child> children) {
//        return template.insert(children);
        return null;
    }

    public Mono<Child> save(Child child) {
        return databaseClient
                .sql("INSERT INTO child (id, name, age, parent_id) VALUES ($1, $2, $3, $4)")
                .bind("$1", child.getId())
                .bind("$2", child.getName())
                .bind("$3", child.getAge())
                .bind("$4", child.getParent().getId())
                .fetch()
                .rowsUpdated()
                .thenReturn(child);
    }

    public Flux<Child> findAll() {
        return template
                .select(Child.class)
                .all();
    }
}