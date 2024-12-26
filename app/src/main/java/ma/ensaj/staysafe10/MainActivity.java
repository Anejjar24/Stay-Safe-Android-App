package ma.ensaj.staysafe10;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ma.ensaj.staysafe10.ui.auth.user.UserViewModel;
import ma.ensaj.staysafe10.ui.location.MapsActivity;
import ma.ensaj.staysafe10.ui.profile.ProfileActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button goToProfile,goToMap;
    private UserViewModel userViewModel;
    private TextView userID, userEmail, userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       initViews();
//
//        // Ajout de valeurs par défaut pour vérifier que les TextView fonctionnent
//        userID.setText("ID: Non chargé");
//        userEmail.setText("Email: Non chargé");
//        userPhone.setText("Phone: Non chargé");
//
//
//        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
//        userViewModel.getCurrentUser().observe(this, user -> {
//            if (user != null) {
//                Log.d(TAG, "Received user update: " + user.getEmail());
//                updateUI(user);
//            } else {
//                Log.d(TAG, "Received null user");
//            }
//        });
//

        goToProfile.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting ProfileActivity", e);
            }
        });

        goToMap.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting ProfileActivity", e);
            }
        });

    }

    private void initViews() {

        goToProfile= findViewById(R.id.button2);
        goToMap= findViewById(R.id.button3);

    }

//    private void updateUI(User user) {
//        if (user.getId() != null) {
//            userID.setText("ID: " + user.getId());
//        }
//        if (user.getEmail() != null) {
//            userEmail.setText("Email: " + user.getEmail());
//        }
//        if (user.getPhone() != null) {
//            userPhone.setText("Phone: " + user.getPhone());
//        }
//        Log.d(TAG, "UI updated with user data");
//    }
}