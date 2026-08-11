package com.example.bankbff.entity;

import com.example.bankbff.dto.TransferRequestDto;

public class TransferRequestDtoContext {
    private final TransferRequestDto transferRequestDto;

    public TransferRequestDtoContext(TransferRequestDto transferRequestDto) {
        this.transferRequestDto = transferRequestDto;
    }

    public TransferRequestDto getTransferRequestDto() {
        return transferRequestDto;
    }
}

