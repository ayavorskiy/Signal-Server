package org.whispersystems.wallet.controller;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.wallet.model.*;
import org.whispersystems.wallet.service.WalletsManager;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.whispersystems.wallet.model.WalletDto.wallet;

@Path("/v1/wallets")
@Slf4j
@RequiredArgsConstructor
public class WalletController {
    private final WalletsManager walletsManager;

    @Timed
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WalletInfoResponse getWallets(@Auth Account account) {
        String phoneNumber = account.getNumber();
        log.debug("Retrieving all personal wallets for number: {}", phoneNumber);
        List<WalletDto> allWalletsForAccount = walletsManager.getAllWallets(phoneNumber);
        log.debug("All wallets retrieved for number: {}, wallets: {}", phoneNumber, allWalletsForAccount);

        return new WalletInfoResponse(convertToInfo(allWalletsForAccount));
    }

    @Timed
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{number}")
    public WalletInfoResponse getWalletsForNumber(@Auth Account account,
                                                  @PathParam("number") String number) {
        String requesterNumber = account.getNumber();
        log.debug("Retrieving all wallets for number: {}, requested by number: {}", number, requesterNumber);
        List<WalletDto> allWalletsForAccount = walletsManager.getAllWallets(number);
        log.debug("All wallets retrieved for number: {}, wallets: {}, requested by number: {}", number, allWalletsForAccount, requesterNumber);
        return new WalletInfoResponse(convertToInfo(allWalletsForAccount));
    }

    @Timed
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{number}/{type}")
    public Response getWalletForNumberAndType(@Auth Account account,
                                                @PathParam("number") String number,
                                                @PathParam("type") WalletType walletType) {
        String requesterNumber = account.getNumber();
        log.debug("Retrieving wallet with type: {} for number: {}, requested by number: {}", walletType, number, requesterNumber);
        Optional<WalletInfo> wallet = walletsManager.getWallet(number, walletType)
                                                    .map(WalletInfo::from);
        if (wallet.isPresent()) {
            log.debug("Wallet with type: {}, retrieved for number: {}, wallet: {}, requested by number: {}", walletType, number, wallet, requesterNumber);
            return Response.status(OK)
                           .entity(wallet.get())
                           .build();
        }
        log.warn("No wallet found for number: {}, with walletType: {}, requested by number: {}", number, walletType, requesterNumber);
        return Response.status(NO_CONTENT)
                       .build();
    }

    @Timed
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void setWallet(@Auth Account account,
                          @Valid WalletRequest walletRequest) {
        String requesterNumber = account.getNumber();
        log.debug("Adding new wallet: {} for number: {}", walletRequest, requesterNumber);

        WalletDto walletDto = wallet().phoneNumber(requesterNumber)
                                      .walletAddress(walletRequest.getWalletAddress())
                                      .walletType(walletRequest.getWalletType())
                                      .build();

        walletsManager.addWallet(walletDto);
        log.debug("New wallet:{} has been added for number: {}", walletRequest, requesterNumber);
    }

    private List<WalletInfo> convertToInfo(List<WalletDto> allWalletsForAccount) {
        return allWalletsForAccount.stream()
                                   .map(WalletInfo::from)
                                   .collect(toList());
    }
}
