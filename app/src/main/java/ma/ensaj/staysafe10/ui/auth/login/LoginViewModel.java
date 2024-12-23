package ma.ensaj.staysafe10.ui.auth.login;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ma.ensaj.staysafe10.R;
import ma.ensaj.staysafe10.data.remote.ApiService;
import ma.ensaj.staysafe10.model.LoginRequest;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.viewpager.widget.ViewPager;

// Retrofit pour la gestion des appels API
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import ma.ensaj.staysafe10.model.LoginResponse;
import ma.ensaj.staysafe10.network.RetrofitClient;
import ma.ensaj.staysafe10.ui.auth.adapter.LoginAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<String> loginStatusLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LiveData<String> getLoginStatusLiveData() {
        return loginStatusLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void login(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Appel de l'API via Retrofit
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    loginStatusLiveData.setValue(response.body().getToken());  // Token reçu
                } else {
                    errorLiveData.setValue("Login failed");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                errorLiveData.setValue("Network error");
            }
        });
    }


}



