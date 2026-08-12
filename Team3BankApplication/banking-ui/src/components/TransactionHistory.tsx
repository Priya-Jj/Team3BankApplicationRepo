import type { CashTransactionRecord } from '../api/types';
import { formatCurrency } from '../utils/format';

type TransactionHistoryProps = {
  transactions: CashTransactionRecord[];
  emptyMessage?: string;
};

export function TransactionHistory({ transactions, emptyMessage = 'No recent deposits or withdrawals yet.' }: TransactionHistoryProps) {
  if (transactions.length === 0) {
    return (
      <section className="transaction-history">
        <h3>Recent teller activity</h3>
        <p className="status-message">{emptyMessage}</p>
      </section>
    );
  }

  return (
    <section className="transaction-history">
      <h3>Recent teller activity</h3>
      <ul>
        {transactions.map((transaction) => (
          <li key={transaction.id}>
            <div className="transaction-history-main">
              <span className={`transaction-history-type ${transaction.type.toLowerCase()}`}>
                {transaction.type}
              </span>
              <strong>{transaction.accountId}</strong>
            </div>
            <div className="transaction-history-meta">
              <span>{formatCurrency(transaction.amount)}</span>
              <span>{transaction.timestamp}</span>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
