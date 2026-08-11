import type { Account } from '../api/types';
import { formatCurrency } from '../utils/format';

type AccountListProps = {
  accounts: Account[];
  loading: boolean;
  error: string | null;
};

export function AccountList({ accounts, loading, error }: AccountListProps) {
  if (loading) {
    return <p className="status-message">Loading accounts...</p>;
  }

  if (error) {
    return <p className="error-message">Error loading accounts: {error}</p>;
  }

  if (accounts.length === 0) {
    return <p className="status-message">No accounts found.</p>;
  }

  return (
    <section className="account-list">
      <h2>Your Accounts</h2>
      <table>
        <thead>
          <tr>
            <th>Account</th>
            <th>Type</th>
            <th>Status</th>
            <th>Balance</th>
          </tr>
        </thead>
        <tbody>
          {accounts.map((account) => {
            const statusValue = (account.status ?? 'ACTIVE').toUpperCase();
            return (
              <tr key={account.id}>
                <td>{account.id}</td>
                <td>{account.accountType}</td>
                <td>
                  <span className={`account-status ${statusValue === 'ACTIVE' ? 'account-status-active' : 'account-status-inactive'}`}>
                    {statusValue}
                  </span>
                </td>
                <td>{formatCurrency(account.balance)}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </section>
  );
}