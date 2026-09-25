package dio.budgeting.application;

import dio.budgeting.application.output.CategoryTotalOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Nova consulta financeira: quanto já foi gasto em uma categoria.
 * Também fica disponível para o modelo via Tool Calling
 * (ex.: "quanto gastei no mercado?").
 */
@Service
public class SumTransactionsByCategoryUseCase {
    private final TransactionRepository transactionRepository;

    public SumTransactionsByCategoryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "sum-transactions-by-category",
            description = "Calcula o total gasto (em centavos) e a quantidade de transações de uma categoria")
    public CategoryTotalOutput execute(@ToolParam(description = "Categoria de uma transação") Category category) {
        List<Transaction> transactions = transactionRepository.findAllByCategory(category);
        long total = transactions.stream().mapToLong(Transaction::getAmount).sum();
        return new CategoryTotalOutput(category.name(), total, transactions.size());
    }
}
