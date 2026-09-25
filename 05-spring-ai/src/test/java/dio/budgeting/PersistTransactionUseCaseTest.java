package dio.budgeting;

import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistTransactionUseCaseTest {
    private InMemoryTransactionRepository repository;
    private PersistTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
        useCase = new PersistTransactionUseCase(repository);
    }

    @Test
    void should_persist_when_transactionIsValid() {
        var output = useCase.execute(new PersistTransactionInput("  Mercado  ", 5000, Category.GROCERIES));

        assertThat(output.description()).isEqualTo("Mercado");
        assertThat(output.category()).isEqualTo("GROCERIES");
        assertThat(repository.size()).isEqualTo(1);
    }

    @Test
    void should_reject_when_amountIsZeroOrNegative() {
        assertThatThrownBy(() -> useCase.execute(new PersistTransactionInput("Farmácia", 0, Category.PHARMA)))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("maior que zero");
        assertThatThrownBy(() -> useCase.execute(new PersistTransactionInput("Farmácia", -10, Category.PHARMA)))
                .isInstanceOf(InvalidTransactionException.class);

        assertThat(repository.size()).isZero();
    }

    @Test
    void should_reject_when_descriptionIsBlank() {
        assertThatThrownBy(() -> useCase.execute(new PersistTransactionInput("   ", 1000, Category.AUTO)))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("descrição");
        assertThatThrownBy(() -> useCase.execute(new PersistTransactionInput(null, 1000, Category.AUTO)))
                .isInstanceOf(InvalidTransactionException.class);
    }

    @Test
    void should_reject_when_categoryIsMissing() {
        assertThatThrownBy(() -> useCase.execute(new PersistTransactionInput("Gasolina", 1000, null)))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("categoria");
    }
}
