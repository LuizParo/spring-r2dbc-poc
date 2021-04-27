package me.github.lgparo.r2dbc.repository;

import lombok.AllArgsConstructor;
import me.github.lgparo.r2dbc.domain.Child;
import me.github.lgparo.r2dbc.domain.Parent;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
@AllArgsConstructor
public class ChildRepository {
    private final DatabaseClient databaseClient;

    public Mono<Void> save(Collection<Child> children) {
        return Flux
                .fromIterable(children)
                .flatMap(this::save)
                .then();
    }

    public Flux<Child> findAll() {
        final String sql = "SELECT c.id, c.name, c.age, c.parent_id, p.name as parent_name, p.age as parent_age " +
                "FROM child c " +
                "INNER JOIN parent p " +
                "ON p.id = c.parent_id " +
                "ORDER BY c.id";

        return databaseClient
                .sql(sql)
                .map(row -> {
                    final Parent parent = Parent
                            .builder()
                            .id(row.get("parent_id", String.class))
                            .name(row.get("parent_name", String.class))
                            .age(row.get("parent_age", Integer.class))
                            .build();

                    return Child
                            .builder()
                            .id(row.get("id", String.class))
                            .name(row.get("name", String.class))
                            .age(row.get("age", Integer.class))
                            .parent(parent)
                            .build();
                })
                .all();
    }

    private Mono<Void> save(Child child) {
        return databaseClient
                .sql("INSERT INTO child (id, name, age, parent_id) VALUES ($1, $2, $3, $4)")
                .bind("$1", child.getId())
                .bind("$2", child.getName())
                .bind("$3", child.getAge())
                .bind("$4", child.getParent().getId())
                .fetch()
                .rowsUpdated()
                .then();
    }
}