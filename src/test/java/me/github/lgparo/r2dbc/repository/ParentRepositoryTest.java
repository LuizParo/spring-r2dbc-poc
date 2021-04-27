package me.github.lgparo.r2dbc.repository;

import me.github.lgparo.r2dbc.config.DatabaseTestAutoConfiguration;
import me.github.lgparo.r2dbc.domain.Child;
import me.github.lgparo.r2dbc.domain.Parent;
import me.github.lgparo.r2dbc.utils.initializers.DatabaseContainerInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

@SpringBootTest(
        classes = {
                DatabaseTestAutoConfiguration.class,
                ParentRepository.class,
                ChildRepository.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ContextConfiguration(initializers = DatabaseContainerInitializer.class)
@ExtendWith(SpringExtension.class)
class ParentRepositoryTest {
    private static final String PARENT_ID_1 = "parent-1";
    private static final String PARENT_ID_2 = "parent-2";
    private static final String PARENT_ID_3 = "parent-3";

    private static final String CHILD_ID_1 = "child-1";
    private static final String CHILD_ID_2 = "child-2";
    private static final String CHILD_ID_3 = "child-3";
    private static final String CHILD_ID_4 = "child-4";

    private static final Parent PARENT_1 = Parent
            .builder()
            .id(PARENT_ID_1)
            .name(UUID.randomUUID().toString())
            .age(35)
            .build();

    private static final Parent PARENT_2 = Parent
            .builder()
            .id(PARENT_ID_2)
            .name(UUID.randomUUID().toString())
            .age(40)
            .build();

    private static final Parent PARENT_3 = Parent
            .builder()
            .id(PARENT_ID_3)
            .name(UUID.randomUUID().toString())
            .age(45)
            .build();

    private static final Child CHILD_1 = Child
            .builder()
            .id(CHILD_ID_1)
            .name(UUID.randomUUID().toString())
            .age(10)
            .parent(PARENT_1)
            .build();

    private static final Child CHILD_2 = Child
            .builder()
            .id(CHILD_ID_2)
            .name(UUID.randomUUID().toString())
            .age(11)
            .parent(PARENT_1)
            .build();

    private static final Child CHILD_3 = Child
            .builder()
            .id(CHILD_ID_3)
            .name(UUID.randomUUID().toString())
            .age(12)
            .parent(PARENT_2)
            .build();

    private static final Child CHILD_4 = Child
            .builder()
            .id(CHILD_ID_4)
            .name(UUID.randomUUID().toString())
            .age(13)
            .parent(PARENT_2)
            .build();

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        parentRepository
                .save(PARENT_1)
                .block();

        parentRepository
                .save(PARENT_2)
                .block();

        parentRepository
                .save(PARENT_3)
                .block();

        childRepository
                .save(List.of(CHILD_1, CHILD_2, CHILD_3, CHILD_4))
                .block();
    }

    @AfterEach
    void tearDown() {
        cleanTable("child");
        cleanTable("parent");
    }

    @Nested
    @DisplayName("method: findById(String)")
    class FindByIdMethod {

        @Test
        @DisplayName("when called with existing ID, then it should return the matching parent")
        void whenCalledWithExistingId_shouldReturnTheMatchingRecord() {
            final Parent expectedParent = PARENT_2
                    .toBuilder()
                    .children(List.of(CHILD_3, CHILD_4))
                    .build();

            StepVerifier
                    .create(parentRepository.findById(PARENT_ID_2))
                    .expectNext(expectedParent)
                    .verifyComplete();
        }

        @Test
        @DisplayName("when called with existing ID, and parent does not have children, then it should return the matching parent")
        void whenCalledWithExistingId_andParentDoesNotHaveChildren_shouldReturnTheMatchingRecord() {
            StepVerifier
                    .create(parentRepository.findById(PARENT_ID_3))
                    .expectNext(PARENT_3)
                    .verifyComplete();
        }

        @Test
        @DisplayName("when called with unknown ID, then it should return empty publisher")
        void whenCalledWithUnknownId_shouldReturnEmptyPublisher() {
            StepVerifier
                    .create(parentRepository.findById(UUID.randomUUID().toString()))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("method: findAll()")
    class FindAllMethod {

        @Test
        @DisplayName("when called, and there are records persisted, then it should return them all")
        void whenCalled_andThereAreRecordsPersisted_shouldReturnThemAll() {
            final Parent expectedParent1 = PARENT_1
                    .toBuilder()
                    .children(List.of(CHILD_1, CHILD_2))
                    .build();

            final Parent expectedParent2 = PARENT_2
                    .toBuilder()
                    .children(List.of(CHILD_3, CHILD_4))
                    .build();

            StepVerifier
                    .create(parentRepository.findAll())
                    .expectNext(expectedParent1)
                    .expectNext(expectedParent2)
                    .expectNext(PARENT_3)
                    .verifyComplete();
        }

        @Test
        @DisplayName("when called, and there are no records persisted, then it should return empty publisher")
        void whenCalled_andThereAreNoRecordsPersisted_shouldReturnPublisher() {
            cleanTable("child");
            cleanTable("parent");

            StepVerifier
                    .create(parentRepository.findAll())
                    .verifyComplete();
        }
    }

    private void cleanTable(String table) {
        databaseClient
                .sql("DELETE FROM " + table)
                .then()
                .block();
    }
}