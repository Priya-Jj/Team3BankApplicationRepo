import type { CashTransactionRecord, AuditRecord } from '../api/types';
import { formatCurrency } from '../utils/format';

type TransactionHistoryProps = {
  transactions: (AuditRecord | CashTransactionRecord)[];
  emptyMessage?: string;
};

function formatDateIsoToMMDDYYYY(iso?: string) {
  if (!iso) return '';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const mm = String(d.getUTCMonth() + 1).padStart(2, '0');
  const dd = String(d.getUTCDate()).padStart(2, '0');
  const yyyy = String(d.getUTCFullYear());
  return `${mm}/${dd}/${yyyy}`;
}

export function TransactionHistory({
  transactions,
  emptyMessage = 'No recent deposits or withdrawals yet.',
}: TransactionHistoryProps) {
  if (transactions.length === 0) {
    return (
      <section className="transaction-history">
        <p className="status-message">{emptyMessage}</p>
      </section>
    );
  }

  const sortedTransactions = [...transactions].sort((a, b) => {
    const dateA = new Date(
      (a as AuditRecord).changedAt ?? (a as CashTransactionRecord).timestamp ?? 0
    ).getTime();
    const dateB = new Date(
      (b as AuditRecord).changedAt ?? (b as CashTransactionRecord).timestamp ?? 0
    ).getTime();
    return dateB - dateA;
  });

  return (
    <section className="transaction-history">
      <table className="audit-table">
        <thead>
          <tr>
            <th>Account ID</th>
            <th>Action</th>
            <th>Old Balance</th>
            <th>New Balance</th>
            <th>Changed At</th>
          </tr>
        </thead>
        <tbody>
          {sortedTransactions.map((transaction) => {
            const isAudit = (transaction as AuditRecord).changedAt !== undefined;
            const accountId = transaction.accountId ?? '';
            const action = isAudit
              ? (transaction as AuditRecord).actionType
              : ((transaction as CashTransactionRecord).type ?? '');
            const oldBal = isAudit ? (transaction as AuditRecord).oldBalance : undefined;
            const newBal = isAudit ? (transaction as AuditRecord).newBalance : undefined;
            const changedAt = isAudit
              ? (transaction as AuditRecord).changedAt
              : (transaction as CashTransactionRecord).timestamp;
            return (
              <tr key={transaction.id}>
                <td>{accountId}</td>
                <td>{action}</td>
                <td>{typeof oldBal === 'number' ? formatCurrency(oldBal) : '-'}</td>
                <td>{typeof newBal === 'number' ? formatCurrency(newBal) : '-'}</td>
                <td>{formatDateIsoToMMDDYYYY(changedAt)}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </section>
  );
}
