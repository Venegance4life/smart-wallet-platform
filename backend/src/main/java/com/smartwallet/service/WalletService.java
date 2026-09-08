package com.smartwallet.service;

import com.smartwallet.dto.WalletDto;
import com.smartwallet.entity.User;
import com.smartwallet.entity.Wallet;
import com.smartwallet.exception.ResourceNotFoundException;
import com.smartwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CurrentUserProvider currentUserProvider;

    public WalletDto getMyWallet() {
        User user = currentUserProvider.getCurrentUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        return toDto(wallet);
    }

    private WalletDto toDto(Wallet wallet) {
        return new WalletDto(wallet.getId(), wallet.getBalance(), wallet.getCurrency());
    }
}
