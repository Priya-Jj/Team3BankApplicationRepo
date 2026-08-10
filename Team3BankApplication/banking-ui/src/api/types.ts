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
  accountNumber: string;
  status: AccountStatus;
  balance: number;
  type: AccountType;
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
  fromAccountNumber: string;
  toAccountNumber: string;
  amount: number;
};

export type TransferResponse = {
  transactionId: string;
  status: TransactionStatus;
};
