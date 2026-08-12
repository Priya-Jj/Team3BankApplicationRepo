/**
 * API client for the banking backend.
 *
 * All HTTP communication with the backend goes through this file. Requests use
 * same-origin URLs that the Vite proxy forwards to the BFF on port 8080. Every
 * request sends "Accept: application/json", which is what makes the BFF return
 * a 401 (rather than a login redirect) when there is no session. The response
 * shapes match the types in types.ts directly, so there is no translation layer.
 */

import type { Account, CashTransactionRequest, CashTransactionResponse, TransferRequest, TransferResponse, User } from './types';

function normalizeAccount(a: unknown): Account {
  const obj = a as Record<string, unknown>;
  return {
    id:
      (obj['accountId'] as string) ??
      (obj['accountid'] as string) ??
      (obj['id'] as string) ??
      '',
    customerId:
      (obj['customerId'] as string) ??
      (obj['customerID'] as string) ??
      (obj['customerid'] as string) ??
      '',
    accountType: obj['accountType'] as Account['accountType'],
    balance: obj['balance'] as number,
    status:
      (obj['accountStatus'] as Account['status']) ??
      (obj['accountstatus'] as Account['status']) ??
      (obj['status'] as Account['status']),
  };
}

export async function getCurrentUser(): Promise<User | null> {
  const response = await fetch('/api/me', {
    headers: { Accept: 'application/json' },
  });
  if (response.status === 401) {
    return null; // not logged in
  }
  if (!response.ok) {
    throw new Error(`Failed to load user: ${response.status}`);
  }
  return response.json();
}

export async function getAccounts(): Promise<Account[]> {
  const response = await fetch('/api/accounts', {
    headers: { Accept: 'application/json' },
  });
  if (!response.ok) {
    throw new Error(`Failed to load accounts: ${response.status}`);
  }
  const data = await response.json();
  if (Array.isArray(data)) {
    return data.map(normalizeAccount);
  }
  return [normalizeAccount(data)];
}

export async function getAccountByCustomerNumber(_customerNumber: string): Promise<Account[]> {
  // The BFF exposes /api/accounts and returns accounts filtered based on the caller's JWT.
  // Ignore customerNumber here and call the BFF endpoint so the server enforces authorization.
  const response = await fetch('/api/accounts', {
    headers: { Accept: 'application/json' },
  });
  if (!response.ok) {
    throw new Error(`Failed to load accounts: ${response.status}`);
  }
  const data = await response.json();
  if (Array.isArray(data)) {
    return data.map(normalizeAccount);
  }
  return [normalizeAccount(data)];
}

export async function getAccountById(accountId: string): Promise<Account[]> {
  const response = await fetch(`/api/accounts/${accountId}`, {
    headers: { Accept: 'application/json' },
  });
  if (!response.ok) {
    throw new Error(`Failed to load account: ${response.status}`);
  }
  const data = await response.json();
  if (Array.isArray(data)) {
    return data.map(normalizeAccount);
  }
  return [normalizeAccount(data)];
}

export async function getTransactionById(accountId: string): Promise<Account[]> {
  const response = await fetch(`/api/accounts/${accountId}/transactions`, {
    headers: { Accept: 'application/json' },
  });
  if (!response.ok) {
    throw new Error(`Failed to load transactions: ${response.status}`);
  }
  return response.json();
}

export async function postTransfer(
  request: TransferRequest
): Promise<TransferResponse> {
  const response = await fetch('/api/transfers', {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });
  if (!response.ok) {
    const message = await safeReadErrorMessage(response);
    throw new Error(message || `Transfer failed: ${response.status}`);
  }
  return response.json();
}

export async function postCashTransaction(
  accountId: string,
  request: CashTransactionRequest
): Promise<CashTransactionResponse> {
  const response = await fetch(`/api/accounts/${accountId}/transactions`, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });
  if (!response.ok) {
    const message = await safeReadErrorMessage(response);
    throw new Error(message || `Cash transaction failed: ${response.status}`);
  }
  return response.json();
}

async function safeReadErrorMessage(response: Response): Promise<string | null> {
  try {
    const body = await response.json();
    if (body && typeof body.message === 'string') {
      return body.message;
    }
    return null;
  } catch {
    return null;
  }
}