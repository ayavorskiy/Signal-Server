package org.whispersystems.wallet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequest {
    @NotEmpty
    @JsonProperty
    private String walletType;
    @NotEmpty
    @JsonProperty
    private String     walletAddress;
}
