package com.example.bankbff.entity;

import com.example.bankbff.dto.TransferResponseDto;

public class TransferResponseDtoContext {
    private final TransferResponseDto transferResponseDto;

    public TransferResponseDtoContext(TransferResponseDto transferResponseDto) {
        this.transferResponseDto = transferResponseDto;
    }

    public TransferResponseDto getTransferResponseDto() {
        return transferResponseDto;
    }
}

