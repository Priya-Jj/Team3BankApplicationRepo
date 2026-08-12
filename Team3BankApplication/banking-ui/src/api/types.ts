/**
 * TypeScript types for the banking API.
 *
 * These types describe the shape of the data exchanged with the backend.
 * They are imported by the API client and by components that work with
 * accounts, customers, and transactions.
 */

export type AccountStatus = 'ACTIVE' | 'INACTIVE';
export type AccountType = 'SAVINGS' | 'CHECKING';

export type Account = {
  id: string;
  customerId: string;
  accountType: AccountType;
  balance: number;
  status?: AccountStatus;
};

export type Customer = {
  customerId: string;
  name: string;
  email: string;
};

export type TransactionStatus = 'COMPLETE' | 'FAILED';
export type TransactionType = 'TRANSFER' | 'DEPOSIT' | 'WITHDRAWAL';

export type Transaction = {
  transactionId: string;
  date: string;
  type: TransactionType;
  amount: number;
  account1: string;
  account2: string | null;
  status: TransactionStatus;
};

export type TransferRequest = {
  fromAccountId: string;
  toAccountId: string;
  amount: number;
};

export type TransferResponse = {
  transactionId: string;
  status: TransactionStatus;
};

export type User = {
  subject: string;
  preferredUsername: string;
  fullName: string;
  roles: string[];
};

export type CashTransactionType = 'DEPOSIT' | 'WITHDRAWAL';

export type CashTransactionRequest = {
  transactionType: CashTransactionType;
  amount: number;
};

export type CashTransactionResponse = {
  transactionId: string;
  status: TransactionStatus;
};

export type CashTransactionRecord = {
  id: string;
  accountId: string;
  customerId: string;
  type: CashTransactionType;
  amount: number;
  timestamp: string;
};
