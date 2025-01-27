package cc.smartcash.wallet.ViewModels;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import cc.smartcash.wallet.Models.FullTransaction;
import cc.smartcash.wallet.Models.TransactionDetails;
import cc.smartcash.wallet.Models.TransactionResponse;
import cc.smartcash.wallet.Utils.ApiUtil;
import cc.smartcash.wallet.Utils.URLS;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionViewModel extends ViewModel {

    public static final String TAG = TransactionViewModel.class.getSimpleName();

    private MutableLiveData<FullTransaction> transaction;
    private MutableLiveData<TransactionResponse> transactionDetails;

    public LiveData<FullTransaction> getTransaction(String hash, Context context) {
        transaction = new MutableLiveData<>();
        loadTransaction(hash, context);

        return transaction;
    }

    public LiveData<TransactionResponse> getTransactionDetails(String token, TransactionDetails details, Context context) {
        transactionDetails = new MutableLiveData<>();
        loadDetails(token, details, context);

        return this.transactionDetails;
    }

    private void loadDetails(String token, TransactionDetails details, Context context) {
        Call<TransactionResponse> call = ApiUtil.getTransactionDetailsService().getDetails("Bearer " + token, details);

        call.enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                if (response.isSuccessful()) {
                    TransactionResponse apiResponse = response.body();
                    transactionDetails.setValue(apiResponse);
                } else {
                    try {
                        transactionDetails.setValue(null);
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                Log.e(TAG, "Erro ao buscar os detalhes da transaction:" + t.getMessage());
                transactionDetails.setValue(null);
            }
        });
    }

    public void loadTransaction(String hash, Context context) {
        Call<FullTransaction> call = ApiUtil.getTransactionService().getTransaction(URLS.URL_INSIGHT_EXPLORER + hash);

        call.enqueue(new Callback<FullTransaction>() {
            @Override
            public void onResponse(Call<FullTransaction> call, Response<FullTransaction> response) {
                if (response.isSuccessful()) {
                    FullTransaction apiResponse = response.body();
                    transaction.setValue(apiResponse);
                } else {
                    try {
                        transaction.setValue(null);
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<FullTransaction> call, Throwable t) {
                Log.e(TAG, "Erro ao buscar a transaction:" + t.getMessage());
                transaction.setValue(null);
            }
        });
    }

}
