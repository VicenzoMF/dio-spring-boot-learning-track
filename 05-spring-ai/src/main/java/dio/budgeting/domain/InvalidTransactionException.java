package dio.budgeting.domain;

/**
 * Lançada quando os dados de uma nova transação violam uma regra do domínio.
 */
public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
