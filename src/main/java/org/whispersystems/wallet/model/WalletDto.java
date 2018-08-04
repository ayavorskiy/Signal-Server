package org.whispersystems.wallet.model;

import lombok.Value;

@Value
public class WalletDto {
    public static final int MEM_CACHE_VERSION = 1;
    String     phoneNumber;
    WalletType walletType;
    String     walletAddress;

    private WalletDto(String phoneNumber, WalletType walletType, String walletAddress) {
        this.phoneNumber = phoneNumber;
        this.walletType = walletType;
        this.walletAddress = walletAddress;
    }

    public static WalletDto from(WalletEntity walletEntity) {
        return new WalletDto(walletEntity.getPhoneNumber(),
                walletEntity.getWalletType(),
                walletEntity.getWalletAddress());

    }

    public static WalletDtoBuilder wallet() {
        return new WalletDtoBuilder();
    }

    public WalletEntity toEntity() {
        return new WalletEntity(this.getPhoneNumber(),
                this.getWalletType(),
                this.getWalletAddress());
    }

    public static class WalletDtoBuilder {
        private String     phoneNumber;
        private WalletType walletType;
        private String     walletAddress;

        WalletDtoBuilder() {
        }

        public WalletDtoBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public WalletDtoBuilder walletType(String walletType) {
            this.walletType = WalletType.valueOf(walletType);
            return this;
        }

        public WalletDtoBuilder walletType(WalletType walletType) {
            this.walletType = walletType;
            return this;
        }

        public WalletDtoBuilder walletAddress(String walletAddress) {
            this.walletAddress = walletAddress;
            return this;
        }

        public WalletDto build() {
            return new WalletDto(phoneNumber, walletType, walletAddress);
        }
    }
}
