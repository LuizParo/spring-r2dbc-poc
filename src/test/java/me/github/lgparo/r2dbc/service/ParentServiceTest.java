package me.github.lgparo.r2dbc.service;

import me.github.lgparo.r2dbc.config.DatabaseTestAutoConfiguration;
import me.github.lgparo.r2dbc.domain.Child;
import me.github.lgparo.r2dbc.domain.Parent;
import me.github.lgparo.r2dbc.repository.ChildRepository;
import me.github.lgparo.r2dbc.repository.ParentRepository;
import me.github.lgparo.r2dbc.utils.initializers.DatabaseContainerInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(
        classes = {
                DatabaseTestAutoConfiguration.class,
                ParentRepository.class,
                ChildRepository.class,
                ParentService.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@EnableTransactionManagement
@ContextConfiguration(initializers = DatabaseContainerInitializer.class)
@ExtendWith(SpringExtension.class)
class ParentServiceTest {
    private static final String PARENT_ID = "parent-1";

    private static final String CHILD_ID_1 = "child-1";
    private static final String CHILD_ID_2 = "child-2";

    private static final Parent PARENT = Parent
            .builder()
            .id(PARENT_ID)
            .name(UUID.randomUUID().toString())
            .age(35)
            .build();

    private static final Child CHILD_1 = Child
            .builder()
            .id(CHILD_ID_1)
            .name(UUID.randomUUID().toString())
            .age(10)
            .parent(PARENT)
            .build();

    private static final Child CHILD_2 = Child
            .builder()
            .id(CHILD_ID_2)
            .name(UUID.randomUUID().toString())
            .age(11)
            .parent(PARENT)
            .build();

    @Autowired
    private ParentService parentService;

    @Autowired
    private ParentRepository parentRepository;

    @SpyBean
    private ChildRepository childRepository;

    @Autowired
    private DatabaseClient databaseClient;

    @AfterEach
    void tearDown() {
        cleanTable("child");
        cleanTable("parent");
    }

    @Nested
    @DisplayName("method: save(Parent)")
    class SaveMethod {

        @Test
        @DisplayName("when an error happens while persisting the children, then it should rollback the whole operation")
        void whenAnErrorHappensWhilePersistingTheChildren_shouldRollbackTheWholeOperation() {
            doReturn(Mono.error(new RuntimeException()))
                    .when(childRepository)
                    .save(anyCollection());

            StepVerifier
                    .create(parentService.save(PARENT))
                    .verifyError();

            StepVerifier
                    .create(parentRepository.findById(PARENT_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("when no errors are thrown in the operation, then it should persist parent and children")
        void whenNoErrorsAreThrownInTheOperation_shouldPersistParentAndChildren() {
            final Parent expectedParent = PARENT
                    .toBuilder()
                    .children(List.of(CHILD_1, CHILD_2))
                    .build();

            StepVerifier
                    .create(parentService.save(expectedParent))
                    .verifyComplete();

            StepVerifier
                    .create(parentRepository.findById(PARENT_ID))
                    .expectNext(expectedParent)
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