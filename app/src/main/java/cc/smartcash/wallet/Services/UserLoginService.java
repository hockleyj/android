package cc.smartcash.wallet.Services;

import cc.smartcash.wallet.Models.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UserLoginService {

    @POST("user/authenticate")
    @FormUrlEncoded
    Call<LoginResponse> getToken(@Field("username") String username,
                                 @Field("password") String password,
                                 @Field("grant_type") String grantType,
                                 @Field("client_id") String clientId,
                                 @Field("TwoFactorAuthentication") String twoFactorAuthentication,
                                 @Field("client_type") String clientType,
                                 @Field("client_ip") String clientIp,
                                 @Field("client_secret") String clientSecret);
}