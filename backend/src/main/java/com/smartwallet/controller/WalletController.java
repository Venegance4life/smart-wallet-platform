package com.smartwallet.controller;

import com.smartwallet.dto.WalletDto;
import com.smartwallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    public WalletDto getMyWallet() {
        return walletService.getMyWallet();
    }
}
