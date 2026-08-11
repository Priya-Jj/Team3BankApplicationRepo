import { useState, type FormEvent } from 'react';
import { postTransfer } from '../api/client';
import type { Account } from '../api/types';
import { formatCurrency } from '../utils/format';

type TransferFormProps = {
  accounts: Account[];
  onTransferComplete: () => void;
};

function isActiveAccount(account: Account): boolean {
  return !account.status || account.status.toUpperCase() === 'ACTIVE';
}

export function TransferForm({ accounts, onTransferComplete }: TransferFormProps) {
  const [fromAccount, setFromAccount] = useState<string>('');
  const [toAccount, setToAccount] = useState<string>('');
  const [amount, setAmount] = useState<string>('');
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [message, setMessage] = useState<string | null>(null);
  const [messageType, setMessageType] = useState<'success' | 'error' | null>(null);

  const activeAccounts = accounts.filter(isActiveAccount);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setMessage(null);
    setMessageType(null);

    const amountNumber = parseFloat(amount);
    if (!fromAccount || !toAccount || isNaN(amountNumber) || amountNumber <= 0) {
      setMessage('Please fill in all fields with valid values.');
      setMessageType('error');
      return;
    }

    if (fromAccount === toAccount) {
      setMessage('From and to accounts must be different.');
      setMessageType('error');
      return;
    }

    const fromAccountDetails = accounts.find((account) => account.id === fromAccount);
    const toAccountDetails = accounts.find((account) => account.id === toAccount);

    if (!fromAccountDetails || !toAccountDetails) {
      setMessage('Please select valid accounts.');
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
        setMessage('Transfer failed. Check the accounts and that the source has sufficient funds.');
        setMessageType('error');
      } else {
        setMessage(`Transfer complete. Transaction ID: ${result.transactionId}`);
        setMessageType('success');
        setFromAccount('');
        setToAccount('');
        setAmount('');
        onTransferComplete();
      }
    } catch (e) {
      setMessage(e instanceof Error ? e.message : 'Transfer failed.');
      setMessageType('error');
    } finally {
      setSubmitting(false);
    }
  }

  if (accounts.length === 0 || activeAccounts.length === 0) {
    return (
      <section className="transfer-form">
        <h2>Transfer Money</h2>
        <p className="status-message">No active accounts available for transfer.</p>
      </section>
    );
  }

  return (
    <section className="transfer-form">
      <h2>Transfer Funds</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-row">
          <label htmlFor="from-account">From Account</label>
          <select
            id="from-account"
            value={fromAccount}
            onChange={(e) => setFromAccount(e.target.value)}
          >
            <option value="">-- Select --</option>
            {activeAccounts.map((a) => (
              <option key={a.id} value={a.id}>
                {a.id} ({a.accountType}, {formatCurrency(a.balance)})
              </option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <label htmlFor="to-account">To Account</label>
          <select
            id="to-account"
            value={toAccount}
            onChange={(e) => setToAccount(e.target.value)}
          >
            <option value="">-- Select --</option>
            {activeAccounts.map((a) => (
              <option key={a.id} value={a.id}>
                {a.id} ({a.accountType}, {formatCurrency(a.balance)})
              </option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <label htmlFor="amount">Amount</label>
          <input
            id="amount"
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
    </section>
  );
}