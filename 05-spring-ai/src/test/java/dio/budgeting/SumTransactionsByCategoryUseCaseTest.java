package dio.budgeting;

import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.SumTransactionsByCategoryUseCase;
import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SumTransactionsByCategoryUseCaseTest {
    private PersistTransactionUseCase persist;
    private SumTransactionsByCategoryUseCase sum;

    @BeforeEach
    void setUp() {
        var repository = new InMemoryTransactionRepository();
        persist = new PersistTransactionUseCase(repository);
        sum = new SumTransactionsByCategoryUseCase(repository);
    }

    @Test
    void should_sumOnlyTheRequestedCategory() {
        persist.execute(new PersistTransactionInput("Mercado", 5000, Category.GROCERIES));
        persist.execute(new PersistTransactionInput("Feira", 2500, Category.GROCERIES));
        persist.execute(new PersistTransactionInput("Remédio", 3000, Category.PHARMA));

        var total = sum.execute(Category.GROCERIES);

        assertThat(total.category()).isEqualTo("GROCERIES");
        assertThat(total.totalAmount()).isEqualTo(7500);
        assertThat(total.transactionCount()).isEqualTo(2);
    }

    @Test
    void should_returnZero_when_categoryHasNoTransactions() {
        var total = sum.execute(Category.AUTO);

        assertThat(total.totalAmount()).isZero();
        assertThat(total.transactionCount()).isZero();
    }
}
