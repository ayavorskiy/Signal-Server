package org.whispersystems.wallet.controller;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.whispersystems.dropwizard.simpleauth.AuthValueFactoryProvider;
import org.whispersystems.textsecuregcm.tests.util.AuthHelper;
import org.whispersystems.textsecuregcm.util.SystemMapper;
import org.whispersystems.wallet.model.WalletDto;
import org.whispersystems.wallet.model.WalletInfo;
import org.whispersystems.wallet.model.WalletInfoResponse;
import org.whispersystems.wallet.model.WalletRequest;
import org.whispersystems.wallet.model.WalletType;
import org.whispersystems.wallet.service.WalletsManager;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.whispersystems.wallet.model.WalletDto.wallet;
import static org.whispersystems.wallet.model.WalletType.BTC;

public class WalletControllerTest {
    public static final String WALLETS_URL = "/v1/wallets/";
    private static WalletsManager walletsManager = Mockito.mock(WalletsManager.class);

    @ClassRule
    public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
                                                                     .addProvider(AuthHelper.getAuthFilter())
                                                                     .addProvider(new AuthValueFactoryProvider.Binder())
                                                                     .setMapper(SystemMapper.getMapper())
                                                                     .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
                                                                     .addResource(new WalletController(walletsManager))
                                                                     .build();

    @Test
    public void shouldReturnWalletsForAccount() {
        WalletDto validNumberWallet = wallet().phoneNumber(AuthHelper.VALID_NUMBER)
                                              .walletAddress("some wallet address")
                                              .walletType(BTC)
                                              .build();
        WalletDto validNumberOtherWallet = wallet().phoneNumber(AuthHelper.VALID_NUMBER)
                                                   .walletAddress("some other wallet address")
                                                   .walletType(WalletType.ETH)
                                                   .build();

        when(walletsManager.getAllWallets(AuthHelper.VALID_NUMBER))
                .thenReturn(asList(validNumberOtherWallet, validNumberWallet));

        WalletInfoResponse response = stubRequestFor(WALLETS_URL).get(WalletInfoResponse.class);

        assertEquals(2, response.getWallets().size());
        assertTrue(Stream.of(validNumberWallet, validNumberOtherWallet)
                         .map(WalletInfo::from)
                         .allMatch(wallet -> response.getWallets().contains(wallet)));
    }

    @Test
    public void shouldReturnEmptyResponseForAccount() {
        when(walletsManager.getAllWallets(AuthHelper.VALID_NUMBER))
                .thenReturn(emptyList());

        WalletInfoResponse response = stubRequestFor(WALLETS_URL).get(WalletInfoResponse.class);

        assertEquals(0, response.getWallets().size());
    }

    @Test
    public void shouldReturnWalletsForNumber() {
        String someOtherNumber = "+386987787878";
        WalletDto someWallet = wallet().phoneNumber(someOtherNumber)
                                       .walletAddress("some wallet address")
                                       .walletType(BTC)
                                       .build();

        when(walletsManager.getAllWallets(someOtherNumber))
                .thenReturn(singletonList(someWallet));

        WalletInfoResponse response = stubRequestFor(WALLETS_URL + someOtherNumber).get(WalletInfoResponse.class);

        assertEquals(1, response.getWallets().size());
        assertEquals(WalletInfo.from(someWallet), response.getWallets().get(0));
    }

    @Test
    public void shouldReturnWalletsForNumberAndType() {
        String someOtherNumber = "+386987787878";
        WalletType walletType = BTC;
        WalletDto someWallet = wallet().phoneNumber(someOtherNumber)
                                       .walletAddress("some wallet address for type test")
                                       .walletType(walletType)
                                       .build();

        when(walletsManager.getWallet(someOtherNumber, walletType))
                .thenReturn(Optional.of(someWallet));

        WalletInfo response = stubRequestFor(WALLETS_URL + someOtherNumber + "/" + walletType)
                .get(WalletInfo.class);

        assertEquals(WalletInfo.from(someWallet), response);
    }

    @Test
    public void shouldReturn404IfNoWalletForNumberAndType() {
        String someOtherNumber = "+386987787878";
        WalletType walletType = BTC;

        when(walletsManager.getWallet(someOtherNumber, walletType))
                .thenReturn(Optional.empty());

        Response response = stubRequestFor(WALLETS_URL + someOtherNumber + "/" + walletType).get();

        assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldPersistNewWalletForAccount() {
        String walletType = WalletType.ETH.name();
        String walletAddress = "some eth wallet address";

        Response response = stubRequestFor(WALLETS_URL).put(entity(new WalletRequest(walletType, walletAddress), APPLICATION_JSON_TYPE));

        assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());

        verify(walletsManager)
                .addWallet(wallet()
                                   .phoneNumber(AuthHelper.VALID_NUMBER)
                                   .walletType(walletType)
                                   .walletAddress(walletAddress)
                                   .build());
    }

    @Test
    public void shouldDeleteWallet() {
        String walletType = WalletType.ETH.name();
        String walletAddress = "some eth wallet address";

        Response response = stubRequestFor(WALLETS_URL + walletType + "/" + walletAddress).delete();

        assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());

        verify(walletsManager)
                .deleteWallet(wallet()
                                      .phoneNumber(AuthHelper.VALID_NUMBER)
                                      .walletType(walletType)
                                      .walletAddress(walletAddress)
                                      .build());

    }

    private Invocation.Builder stubRequestFor(String url) {
        return RESOURCES.getJerseyTest()
                        .target(url)
                        .request()
                        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD));
    }
}