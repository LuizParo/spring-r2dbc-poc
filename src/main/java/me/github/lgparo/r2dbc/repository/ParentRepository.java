package me.github.lgparo.r2dbc.repository;

import lombok.AllArgsConstructor;
import me.github.lgparo.r2dbc.domain.Parent;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@AllArgsConstructor
public class ParentRepository {
    private final R2dbcEntityTemplate template;
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
        return template
                .select(Parent.class)
                .matching(query(where("id").is(id)))
                .first();
    }

    public Flux<Parent> findAll() {
        return databaseClient
                .sql("SELECT p.id, p.name, p.age, c.id as child_id, c.name as child_name, c.age as child_age, c.parent_id FROM parent p INNER JOIN child c ON c.parent_id = p.id")
                .map(row ->
                        Parent
                                .builder()
                                .id(row.get("id", String.class))
                                .name(row.get("name", String.class))
                                .age(row.get("age", Integer.class))
                                .build()
                )
                .all();
    }


}
