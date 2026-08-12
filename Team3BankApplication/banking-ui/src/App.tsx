/**
 * Root component.
 *
 * Reads the auth state and decides what to render:
 *  - loading: a "checking sign-in state" message
 *  - not logged in: the SignInScreen
 *  - logged in (account holder): the accounts list and transfer form
 *  - logged in (teller): the teller dashboard with all accounts and create account form
 *
 * App owns the accounts data (lifted up in Lab 4.5) and, new in this lab, only
 * loads it once a user is authenticated. After a successful transfer,
 * onTransferComplete re-fetches accounts so balances update.
 */

import { useState, useEffect, useCallback } from 'react';
import { Header } from './components/Header';
import { AccountList } from './components/AccountList';
import { TellerScreen } from './components/TellerScreen';
import { TransferForm } from './components/TransferForm';
import { SignInScreen } from './components/SignInScreen';
import { useAuth } from './auth/AuthContext';
import { getAccounts, getAccountByCustomerNumber } from './api/client';
import type { Account } from './api/types';
import './App.css';

export function App() {
  const { user, loading: authLoading } = useAuth();

  const [accounts, setAccounts] = useState<Account[]>([]);
  const [accountsLoading, setAccountsLoading] = useState<boolean>(false);
  const [accountsError, setAccountsError] = useState<string | null>(null);

  const loadAccounts = useCallback(async () => {
    if (!user) return;
    setAccountsLoading(true);
    setAccountsError(null);
    try {
      let data: Account[] = [];
      const isTeller = user?.roles.includes('teller') ?? false;
      if (isTeller) {
        data = await getAccounts();
      } else {
        // Account holders: fetch only their accounts by customer number (subject)
        data = await getAccountByCustomerNumber(user.subject);
      }
      setAccounts(data);
    } catch (e) {
      setAccountsError(e instanceof Error ? e.message : 'Unknown error');
      setAccounts([]);
    } finally {
      setAccountsLoading(false);
    }
  }, [user]);

  // Load accounts once a user becomes available (the auth gate).
  useEffect(() => {
    if (user) {
      loadAccounts();
    }
  }, [user, loadAccounts]);

  // Check if user is a teller
  const isTeller = user?.roles.includes('teller') ?? false;

  return (
    <div className="app">
      <Header />
      <main>
        {authLoading && <p className="status-message">Checking sign-in state...</p>}
        {!authLoading && !user && <SignInScreen />}
        {!authLoading && user && isTeller && (
          <TellerScreen
            accounts={accounts}
            loading={accountsLoading}
            error={accountsError}
            onRefreshAccounts={loadAccounts}
          />
        )}
        {!authLoading && user && !isTeller && (
          <>
            <AccountList
              accounts={accounts}
              loading={accountsLoading}
              error={accountsError}
            />
            <TransferForm accounts={accounts} onTransferComplete={loadAccounts} />
          </>
        )}
      </main>
    </div>
  );
}