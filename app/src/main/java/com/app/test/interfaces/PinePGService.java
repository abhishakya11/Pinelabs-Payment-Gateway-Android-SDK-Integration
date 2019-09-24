package com.app.test.interfaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PinePGService {
    @GET("/api/MobileApp/transactionStatus?")
    Call<String> getTransactionStatus(@Query("merchantid") String merchantid, @Query("uniqueMerchantTxnId") String uniqueMerchantTxnId,
                                      @Query("merchantAccessCode") String merchantAccessCode);
}
