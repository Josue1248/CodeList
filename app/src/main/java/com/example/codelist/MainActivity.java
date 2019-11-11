package com.example.codelist;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.codelist.ui.home.HomeFragment;
import com.example.codelist.ui.home.HomeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private static final int CODIGO_PERMISOS_CAMARA = 1, CODIGO_INTENT = 2;
    private boolean cameraAccessGranted = false, accessByButton = false;
    public Button ScannerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraAccessCheck();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        ScannerButton = findViewById(R.id.scanner_button);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_list, R.id.navigation_history)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        ScannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cameraAccessGranted) {
                    Toast.makeText(MainActivity.this, "Por favor permite que la app acceda a la cámara", Toast.LENGTH_SHORT).show();
                    accessByButton = true;
                    cameraAccessCheck();
                    return;
                }
                initiateScan();
            }
        });
    }

    private void initiateScan() {
        Intent i = new Intent(MainActivity.this, Scanner.class);
        startActivityForResult(i, CODIGO_INTENT);
    }

    private void accessDenied() {
        // Esto se llama cuando el usuario hace click en "Denegar" o
        // cuando lo denegó anteriormente
        Toast.makeText(MainActivity.this, "No puedes escanear si no das permiso", Toast.LENGTH_SHORT).show();
    }

    private void cameraAccessCheck() {
        int estadoDePermiso = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if (estadoDePermiso == PackageManager.PERMISSION_GRANTED) {
            // En caso de que haya dado permisos ponemos la bandera en true
            // y llamar al método
            cameraAccessGranted = true;
        } else {
            // Si no, pedimos permisos. Ahora mira onRequestPermissionsResult
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    CODIGO_PERMISOS_CAMARA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CODIGO_INTENT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String code = data.getStringExtra("codigo");
                    Bundle bundle = new Bundle();
                    bundle.putString("codigo", code);
                    // set Fragmentclass Arguments
                    HomeFragment fragobj = new HomeFragment();
                    fragobj.setArguments(bundle);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.navigation_home, fragobj);
                    //transaction.addToBackStack(null);
                    transaction.commitAllowingStateLoss();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODIGO_PERMISOS_CAMARA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Escanear directamente solo si fue pedido desde el botón
                    if (accessByButton) {
                        initiateScan();
                    }
                    cameraAccessGranted = true;
                } else {
                    accessDenied();
                }
                break;
        }
    }

}
