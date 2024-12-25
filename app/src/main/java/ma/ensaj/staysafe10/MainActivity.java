package ma.ensaj.staysafe10;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import ma.ensaj.staysafe10.model.User;
import ma.ensaj.staysafe10.ui.auth.user.UserViewModel;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private UserViewModel userViewModel;
    private TextView userID, userEmail, userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        // Ajout de valeurs par défaut pour vérifier que les TextView fonctionnent
        userID.setText("ID: Non chargé");
        userEmail.setText("Email: Non chargé");
        userPhone.setText("Phone: Non chargé");

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Log.d(TAG, "Received user update: " + user.getEmail());
                updateUI(user);
            } else {
                Log.d(TAG, "Received null user");
            }
        });
    }

    private void initViews() {
        userID = findViewById(R.id.id);
        userEmail = findViewById(R.id.emailUser);
        userPhone = findViewById(R.id.phoneUser);
    }

    private void updateUI(User user) {
        if (user.getId() != null) {
            userID.setText("ID: " + user.getId());
        }
        if (user.getEmail() != null) {
            userEmail.setText("Email: " + user.getEmail());
        }
        if (user.getPhone() != null) {
            userPhone.setText("Phone: " + user.getPhone());
        }
        Log.d(TAG, "UI updated with user data");
    }
}