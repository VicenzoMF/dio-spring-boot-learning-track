package dio.budgeting.application.output;

/**
 * Resumo dos gastos de uma categoria. {@code totalAmount} usa a mesma unidade
 * do valor armazenado nas transações (centavos).
 */
public record CategoryTotalOutput(String category, long totalAmount, int transactionCount) {
}
