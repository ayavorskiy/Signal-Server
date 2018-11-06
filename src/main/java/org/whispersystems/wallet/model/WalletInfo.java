package org.whispersystems.wallet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletInfo {
    @JsonProperty
    private WalletType walletType;
    @JsonProperty
    private String     walletAddress;

    public static WalletInfo from(WalletDto walletDto) {
        return new WalletInfo(walletDto.getWalletType(),
                              walletDto.getWalletAddress());
    }
}
