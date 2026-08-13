import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { postCashTransaction, postDeposit, getCurrentUser } from '../api/client';
import { TransactionHistory } from './TransactionHistory';
import type { Account, CashTransactionRecord, CashTransactionType } from '../api/types';
import { formatCurrency } from '../utils/format';

type DepositWithdrawalProps = {
  accounts: Account[];
  onRefreshAccounts?: () => Promise<void> | void;
  onAddTransaction?: (record: CashTransactionRecord) => void;
};

export function DepositWithdrawal({ accounts, onRefreshAccounts }: DepositWithdrawalProps) {
  const [selectedCustomerId, setSelectedCustomerId] = useState('');
  const [fromAccount, setFromAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [transactionType, setTransactionType] = useState<CashTransactionType>('DEPOSIT');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [messageType, setMessageType] = useState<'success' | 'error' | null>(null);
  const [transactions, setTransactions] = useState<CashTransactionRecord[]>([]);
  const [isTeller, setIsTeller] = useState(false);

  const customerIds = useMemo(() => {
    return Array.from(new Set(accounts.map((account) => account.customerId))).sort();
  }, [accounts]);

  const selectedCustomerAccounts = useMemo(() => {
    if (!selectedCustomerId) return [];
    return accounts.filter((account) => account.customerId === selectedCustomerId);
  }, [accounts, selectedCustomerId]);

  useEffect(() => {
    if (!selectedCustomerId && customerIds.length > 0) {
      setSelectedCustomerId(customerIds[0]);
    }
  }, [customerIds, selectedCustomerId]);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const user = await getCurrentUser();
        if (!mounted) return;
        setIsTeller(!!(user && Array.isArray(user.roles) && user.roles.includes('teller')));
      } catch {
        // ignore
      }
    })();
    return () => { mounted = false; };
  }, []);

  async function handleTransactionSubmit(e: FormEvent) {
    e.preventDefault();
    setMessage(null);
    setMessageType(null);

    if (!selectedCustomerId) {
      setMessage('Select a customer for the transaction.');
      setMessageType('error');
      return;
    }

    const amountNumber = parseFloat(amount);
    if (isNaN(amountNumber) || amountNumber <= 0) {
      setMessage('Enter a valid amount for the transaction.');
      setMessageType('error');
      return;
    }

    const accountDetails = selectedCustomerAccounts.find((account) => account.id === fromAccount);
    if (!accountDetails) {
      setMessage('Select a valid account for the transaction.');
      setMessageType('error');
      return;
    }

    if (transactionType === 'WITHDRAWAL' && accountDetails.balance < amountNumber) {
      setMessage('Insufficient balance for the withdrawal.');
      setMessageType('error');
      return;
    }

    setSubmitting(true);
    try {
      if (transactionType === 'DEPOSIT') {
        if (!isTeller) {
          setMessage('Deposits are allowed for tellers only.');
          setMessageType('error');
          setSubmitting(false);
          return;
        }

        const result = await postDeposit(fromAccount, amountNumber);
        setMessage(`Transaction successful. Transaction ID: ${result.txnId}`);
        setMessageType('success');
        setFromAccount('');
        setAmount('');
        setTransactionType('DEPOSIT');
        const newRecord = {
          id: result.txnId ?? '',
          accountId: fromAccount,
          customerId: selectedCustomerId,
          type: transactionType,
          amount: amountNumber,
          timestamp: new Date().toLocaleString(),
        } as CashTransactionRecord;
        setTransactions((prev) => [newRecord, ...prev].slice(0, 8));
        await onRefreshAccounts?.();
      } else {
        const result = await postCashTransaction(fromAccount, {
          transactionType,
          amount: amountNumber,
        });

        if (result.status === 'FAILED') {
          setMessage('Transaction failed. Please try again.');
          setMessageType('error');
        } else {
          setMessage(`Transaction successful. Transaction ID: ${result.transactionId}`);
          setMessageType('success');
          setFromAccount('');
          setAmount('');
          setTransactionType('DEPOSIT');
          const newRecord = {
            id: result.transactionId,
            accountId: fromAccount,
            customerId: selectedCustomerId,
            type: transactionType,
            amount: amountNumber,
            timestamp: new Date().toLocaleString(),
          } as CashTransactionRecord;
          setTransactions((prev) => [newRecord, ...prev].slice(0, 8));
          await onRefreshAccounts?.();
        }
      }
    } catch (e) {
      if (e instanceof Error && (e.message.includes('403') || /forbidden/i.test(e.message))) {
        setMessage('You are not authorized to perform deposits (teller only).');
      } else {
        setMessage(e instanceof Error ? e.message : 'Transaction failed.');
      }
      setMessageType('error');
    } finally {
      setSubmitting(false);
    }
  }

  const visibleTransactions = useMemo(() => {
    const filtered = transactions.filter((transaction) => transaction.customerId === selectedCustomerId);
    if (!fromAccount) {
      return filtered.slice(0, 6);
    }
    return filtered.filter((transaction) => transaction.accountId === fromAccount).slice(0, 6);
  }, [fromAccount, selectedCustomerId, transactions]);

  return (
    <section className="transfer-form customer-transaction">
        <div className="section-header">
          <h2>Deposit / Withdrawal</h2>
        </div>
        <p>Select a customer and perform deposit or withdrawal transactions on their accounts.</p>

        <div className="form-row">
    <label htmlFor="dw-customer-select">Customer</label>
    <select
      id="dw-customer-select"
      value={selectedCustomerId}
      onChange={(e) => {
        setSelectedCustomerId(e.target.value);
        setFromAccount('');
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
      ) : (
        <form onSubmit={handleTransactionSubmit}>
          <div className="form-row">
            <label htmlFor="dw-account">Account</label>
            <select id="dw-account" value={fromAccount} onChange={(e) => setFromAccount(e.target.value)}>
              <option value="">-- Select --</option>
              {selectedCustomerAccounts.map((account) => (
                <option key={account.id} value={account.id}>
                  {account.id} ({account.accountType}, {formatCurrency(account.balance)})
                </option>
              ))}
            </select>
          </div>

          <div className="form-row">
            <label htmlFor="dw-amount">Amount</label>
            <input id="dw-amount" type="number" step="0.01" min="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} />
          </div>

          <div className="form-row transaction-type">
            <label>Transaction Type</label>
            <div>
              <label>
                <input type="radio" value="DEPOSIT" checked={transactionType === 'DEPOSIT'} onChange={() => setTransactionType('DEPOSIT')} />
                Deposit
              </label>
              <label>
                <input type="radio" value="WITHDRAWAL" checked={transactionType === 'WITHDRAWAL'} onChange={() => setTransactionType('WITHDRAWAL')} />
                Withdrawal
              </label>
            </div>
          </div>

          <button type="submit" disabled={submitting}>{submitting ? 'Processing...' : 'Submit Transaction'}</button>

          {message && <p className={messageType === 'success' ? 'success-message' : 'error-message'}>{message}</p>}
        </form>
      )}
    </section>
  );
}