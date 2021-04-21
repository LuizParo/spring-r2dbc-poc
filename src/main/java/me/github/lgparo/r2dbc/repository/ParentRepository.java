package me.github.lgparo.r2dbc.repository;

import lombok.AllArgsConstructor;
import me.github.lgparo.r2dbc.domain.Child;
import me.github.lgparo.r2dbc.domain.Parent;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Repository
@AllArgsConstructor
public class ParentRepository {
    private final DatabaseClient databaseClient;

    public Mono<Parent> save(Parent parent) {
        return databaseClient
                .sql("INSERT INTO parent (id, name, age) VALUES ($1, $2, $3)")
                .bind("$1", parent.getId())
                .bind("$2", parent.getName())
                .bind("$3", parent.getAge())
                .fetch()
                .rowsUpdated()
                .thenReturn(parent);
    }

    public Mono<Parent> findById(String id) {
        final String sql = "SELECT p.id, p.name, p.age, c.id as child_id, c.name as child_name, c.age as child_age, c.parent_id " +
                "FROM parent p " +
                "INNER JOIN child c ON c.parent_id = p.id " +
                "WHERE p.id = :id";

        return databaseClient
                .sql(sql)
                .fetch()
                .all()
                .bufferUntilChanged(result -> result.get("id"))
                .map(this::mapResultsToEntity)
                .next();
    }

    public Flux<Parent> findAll() {
        final String sql = "SELECT p.id, p.name, p.age, c.id as child_id, c.name as child_name, c.age as child_age, c.parent_id " +
                "FROM parent p " +
                "INNER JOIN child c ON c.parent_id = p.id";

        return databaseClient
                .sql(sql)
                .fetch()
                .all()
                .bufferUntilChanged(result -> result.get("id"))
                .map(this::mapResultsToEntity);
    }

    private Parent mapResultsToEntity(List<Map<String, Object>> results) {
        final Parent parent = Parent
                .builder()
                .id((String) results.get(0).get("id"))
                .name((String) results.get(0).get("name"))
                .age((int) results.get(0).get("age"))
                .build();

        return parent
                .toBuilder()
                .children(
                        results.stream()
                                .map(childrenResult -> Child
                                        .builder()
                                        .id((String) childrenResult.get("child_id"))
                                        .name((String) childrenResult.get("child_name"))
                                        .age((int) childrenResult.get("child_age"))
                                        .parent(parent)
                                        .build())
                                .collect(toList())
                )
                .build();
    }
}
