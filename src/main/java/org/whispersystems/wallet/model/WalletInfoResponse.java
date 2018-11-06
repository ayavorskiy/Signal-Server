package org.whispersystems.wallet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletInfoResponse {
    @JsonProperty
    private List<WalletInfo> wallets;
}
