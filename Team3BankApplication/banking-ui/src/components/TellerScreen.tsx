import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { postTransfer } from '../api/client';
import type { Account, CashTransactionRecord } from '../api/types';
import { formatCurrency } from '../utils/format';
import { DepositWithdrawal } from './DepositWithdrawal';
import { TransactionHistory } from './TransactionHistory';

type TellerScreenProps = {
  accounts: Account[];
  loading: boolean;
  error: string | null;
  onRefreshAccounts?: () => Promise<void> | void;
};

function isActiveAccount(account: Account): boolean {
  return (account.status ?? 'ACTIVE').toUpperCase() === 'ACTIVE';
}

export function TellerScreen({ accounts, loading, error, onRefreshAccounts }: TellerScreenProps) {
  const [selectedCustomerId, setSelectedCustomerId] = useState('');
  const [fromAccount, setFromAccount] = useState('');
  const [toAccount, setToAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [messageType, setMessageType] = useState<'success' | 'error' | null>(null);
  const [transactions, setTransactions] = useState<CashTransactionRecord[]>([]);
  const [activeTab, setActiveTab] = useState<'transfer' | 'cash' | 'history'>('transfer');

  const customerIds = useMemo(() => {
    return Array.from(new Set(accounts.map((account) => account.customerId))).sort();
  }, [accounts]);

  const selectedCustomerAccounts = useMemo(() => {
    if (!selectedCustomerId) {
      return [];
    }
    return accounts.filter((account) => account.customerId === selectedCustomerId);
  }, [accounts, selectedCustomerId]);

  const transferableAccounts = useMemo(() => {
    if (!selectedCustomerId) {
      return [];
    }
    return selectedCustomerAccounts.filter((account) => isActiveAccount(account));
  }, [selectedCustomerAccounts, selectedCustomerId]);

  useEffect(() => {
    if (accounts.length === 0) {
      setSelectedCustomerId('');
      setFromAccount('');
      setToAccount('');
      setAmount('');
      setMessage(null);
      setMessageType(null);
      return;
    }

    if (!selectedCustomerId || !customerIds.includes(selectedCustomerId)) {
      setSelectedCustomerId(customerIds[0] ?? '');
    }
  }, [accounts.length, customerIds, selectedCustomerId]);

  async function handleTransferSubmit(e: FormEvent) {
    e.preventDefault();
    setMessage(null);
    setMessageType(null);

    if (!selectedCustomerId) {
      setMessage('Select a customer before creating a transfer.');
      setMessageType('error');
      return;
    }

    const amountNumber = parseFloat(amount);
    if (!fromAccount || !toAccount || isNaN(amountNumber) || amountNumber <= 0) {
      setMessage('Please select both accounts and enter a valid amount.');
      setMessageType('error');
      return;
    }

    if (fromAccount === toAccount) {
      setMessage('From and to accounts must be different.');
      setMessageType('error');
      return;
    }

    const fromAccountDetails = selectedCustomerAccounts.find((account) => account.id === fromAccount);
    const toAccountDetails = selectedCustomerAccounts.find((account) => account.id === toAccount);

    if (!fromAccountDetails || !toAccountDetails) {
      setMessage('The selected accounts must belong to the chosen customer.');
      setMessageType('error');
      return;
    }

    if (fromAccountDetails.customerId !== toAccountDetails.customerId) {
      setMessage('Transfers are only allowed between accounts that belong to the same customer.');
      setMessageType('error');
      return;
    }

    if (!isActiveAccount(fromAccountDetails) || !isActiveAccount(toAccountDetails)) {
      setMessage('Only active accounts can be used in transfers.');
      setMessageType('error');
      return;
    }

    setSubmitting(true);
    try {
      const result = await postTransfer({
        fromAccountId: fromAccount,
        toAccountId: toAccount,
        amount: amountNumber,
      });

      if (result.status === 'FAILED') {
        setMessage('Transfer failed. Check the selected accounts and available balance.');
        setMessageType('error');
      } else {
        setMessage(`Transfer complete. Transaction ID: ${result.transactionId}`);
        setMessageType('success');
        setFromAccount('');
        setToAccount('');
        setAmount('');
        await onRefreshAccounts?.();
      }
    } catch (e) {
      setMessage(e instanceof Error ? e.message : 'Transfer failed.');
      setMessageType('error');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <p className="status-message">Loading accounts...</p>;
  }

  if (error) {
    return <p className="error-message">Error loading accounts: {error}</p>;
  }

  return (
    <div className="teller-screen">
      <section className="teller-info">
        <h2>Teller Dashboard</h2>
        <p>You have access to all bank accounts and can view them below.</p>
      </section>

      <section className="all-accounts">
        <div className="section-header">
          <h2>All Bank Accounts</h2>
        </div>

        {accounts.length === 0 ? (
          <p className="status-message">No accounts found.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Account ID</th>
                <th>Customer</th>
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
                    <td>{account.customerId}</td>
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
        )}
      </section>

      <section className="teller-tabs">
        <div className="tabs">
          <button type="button" className={activeTab === 'transfer' ? 'active' : ''} onClick={() => setActiveTab('transfer')}>Transfer</button>
          <button type="button" className={activeTab === 'cash' ? 'active' : ''} onClick={() => setActiveTab('cash')}>Deposit / Withdrawal</button>
          <button type="button" className={activeTab === 'history' ? 'active' : ''} onClick={() => setActiveTab('history')}>Recent History</button>
        </div>

        <div className="tab-content">
          {activeTab === 'transfer' && (
            <section className="transfer-form customer-transfer">
              <div className="section-header">
                <h2>Transfer Between Customer Accounts</h2>
              </div>
              <p>Select a customer and transfer funds between that customer's own accounts only.</p>

              <div className="form-row">
                <label htmlFor="customer-select">Customer</label>
                <select
                  id="customer-select"
                  value={selectedCustomerId}
                  onChange={(e) => {
                    setSelectedCustomerId(e.target.value);
                    setFromAccount('');
                    setToAccount('');
                    setAmount('');
                    setMessage(null);
                    setMessageType(null);
                  }}
                >
                  <option value="">-- Select customer --</option>
                  {customerIds.map((customerId) => (
                    <option key={customerId} value={customerId}>
                      {customerId}
                    </option>
                  ))}
                </select>
              </div>

              {!selectedCustomerId ? (
                <p className="status-message">Choose a customer to begin.</p>
              ) : transferableAccounts.length < 2 ? (
                <p className="status-message">This customer does not have enough active accounts to make a transfer.</p>
              ) : (
                <form onSubmit={handleTransferSubmit}>
                  <div className="form-row">
                    <label htmlFor="teller-from-account">From Account</label>
                    <select
                      id="teller-from-account"
                      value={fromAccount}
                      onChange={(e) => setFromAccount(e.target.value)}
                    >
                      <option value="">-- Select --</option>
                      {transferableAccounts.map((account) => (
                        <option key={account.id} value={account.id}>
                          {account.id} ({account.accountType}, {formatCurrency(account.balance)})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-row">
                    <label htmlFor="teller-to-account">To Account</label>
                    <select
                      id="teller-to-account"
                      value={toAccount}
                      onChange={(e) => setToAccount(e.target.value)}
                    >
                      <option value="">-- Select --</option>
                      {transferableAccounts.map((account) => (
                        <option key={account.id} value={account.id}>
                          {account.id} ({account.accountType}, {formatCurrency(account.balance)})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-row">
                    <label htmlFor="teller-transfer-amount">Amount</label>
                    <input
                      id="teller-transfer-amount"
                      type="number"
                      step="0.01"
                      min="0.01"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                    />
                  </div>

                  <button type="submit" disabled={submitting}>
                    {submitting ? 'Processing...' : 'Submit Transfer'}
                  </button>

                  {message && (
                    <p className={messageType === 'success' ? 'success-message' : 'error-message'}>
                      {message}
                    </p>
                  )}
                </form>
              )}
            </section>
          )}

          {activeTab === 'cash' && (
            <DepositWithdrawal accounts={accounts} onRefreshAccounts={onRefreshAccounts} onAddTransaction={(r) => setTransactions((prev) => [r, ...prev].slice(0, 50))} />
          )}

          {activeTab === 'history' && (
            <section className="recent-history">
              <div className="section-header">
                <h2>Recent Transactions</h2>
              </div>
              <TransactionHistory transactions={transactions} emptyMessage="No recent transactions" />
            </section>
          )}
        </div>
      </section>
    </div>
  );
}