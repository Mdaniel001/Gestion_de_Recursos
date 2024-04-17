package co.edu.uniminuto;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //1. Declaracion de los Objetos de la interface que se usaran en la puerta
    public static final int REQUEST_CODE = 25;
    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 101;
    private static final int REQUEST_READ_STORAGE_PERMISSION = 102;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 103;
    private static final int REQUEST_ACCESS_COARSE_LOCATION_PERMISSION = 105;

    private Button btnCheckPermissions;
    private Button btnRequestPermissions;
    private Button btnRequestPermissionRecordAudio;
    private Button btnRequestPermissionLocation;
    private Button btnRequestPermissionExternalStorage;
    private Button btnRequestPermissionReadStorage;
    private TextView tvCamera;
    private TextView tvExternalWS;
    private TextView tvReadExternalS;

    private TextView tvRecordAudio;
    private TextView tvAccessCoarseLocation;
    private TextView tvResponse;
    //1.1 Objetos para recursos
    private TextView versipmAndroid;
    private int versionSDK;
    private ProgressBar pbLevelBatt;
    private TextView tvLevelBatt;
    private TextView tvConexion;
    private TextView tvLevelBaterry;

    //Par Guardar Archivo .txt
    private EditText etNombreArchivo;
    private Button btnGuardarArchivo;

    IntentFilter batFilter;
    CameraManager cameraManager;
    String cameraId;
    private Button btnOn;
    private Button btnOff;
    ConnectivityManager conexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //3. llamado del metodo de enlace de objetos
         initObject();


         //Llamado metodo para llamado de Estado conocexion a Internet
         actualizarEstadoConexion();

        //4. Enlace de botones a los sus metodos.

        btnCheckPermissions.setOnClickListener(this::voidCheckPermissions);
        btnRequestPermissions.setOnClickListener(this::voidRequestPermissions);
        btnRequestPermissionExternalStorage.setOnClickListener(this::voidCheckPermissionExternalStorage);
        btnRequestPermissionReadStorage.setOnClickListener(this::voidCheckPermissionReadStorage);
        btnRequestPermissionRecordAudio.setOnClickListener(this::voidCheckPermissionRecordAudio);
        btnRequestPermissionLocation.setOnClickListener(this::voidRequestPermissionLocation);

        //Enlace Botones para linterna.

        btnOn.setOnClickListener(this::onLight);
        btnOff.setOnClickListener(this::offLight);

        //bateria

        batFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);  //Instanciamos la bateria
        registerReceiver(receiver, batFilter);

        //enlace Boton Guardar Archivo
        btnGuardarArchivo.setOnClickListener(this::guardarArchivo);



    }


    //11. Captura de conexion a Internet
    private void actualizarEstadoConexion() {
        if (isConnectedToInternet()) {
            tvConexion.setText("Conectado a Internet");
        } else {
            tvConexion.setText("No estás conectado a Internet");
        }
    }


    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

            return activeNetwork != null && ((NetworkInfo) activeNetwork).isConnectedOrConnecting();
        }

        return false;
    }

    //12. metodo para Guardar Archivo en Diaspositivo

    private void guardarArchivo(View view) {
        String fileName = etNombreArchivo.getText().toString().trim();

        if (fileName.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un nombre de archivo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!fileName.endsWith(".txt")) {
            fileName += ".txt";
        }
        String versionSO = Build.VERSION.RELEASE;
        String batteryLevel = tvLevelBatt.getText().toString();

        String content = "Versión SO: " + versionSO + "\nNivel de batería: " + batteryLevel;

        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this, "Archivo guardado correctamente", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
        }
    }







    //10. Bateria creacion del receiver- Capturar Nivel Bateria

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int levelBaterry = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            pbLevelBatt.setProgress(levelBaterry);
            tvLevelBatt.setText("Level Baterry: "+levelBaterry+ " %");
        }

    };


    //9. Metodos de encendio Botones Linterana

    private void offLight(View view) {//apagado
        try {

            cameraManager.setTorchMode(cameraId, false);//para que se apague el flash o linterna

        }catch (Exception e){

            Toast.makeText(this,"no se puede encender la linterna", Toast.LENGTH_SHORT).show();
            Log.i("FLASH",e.getMessage());

        }
    }

    private void onLight(View view) {
        try {
            cameraManager= (CameraManager) getSystemService(Context.CAMERA_SERVICE);//Se debe castear el servicio.
            cameraId=cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);//para que se encienda el falsh

        }catch (Exception e){
            Toast.makeText(this,"no se puede encender la linterna", Toast.LENGTH_SHORT).show();
            Log.i("FLASH",e.getMessage());
        }
    }


    //8. Implementacion del OnResume para la version de android

    @Override
    protected void onResume() {
        super.onResume();
        String versionSO = Build.VERSION.RELEASE;
        versionSDK = Build.VERSION.SDK_INT;
        versipmAndroid.setText("Version SO:"+versionSO+" / SDK:"+versionSDK);
    }






    //5. Verificacion De Permisos
    private void voidCheckPermissions(View view) {
        //Si hay permiso ---> 0 ---> si no --> 1
        int statusCamera = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA);
        int statusWES = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int statusRES = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE);

        int statusAccessCoarseLocation =ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int statusRecordAudio =ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);

        tvCamera.setText("Status Camara:"+statusCamera);
        tvExternalWS.setText("Status WES:"+statusWES);
        tvReadExternalS.setText("Status RES:"+statusRES);
        tvAccessCoarseLocation.setText("Status Location:"+statusAccessCoarseLocation);
        tvRecordAudio.setText("Status Record Audio :"+statusRecordAudio);

        btnRequestPermissions.setEnabled(true);
    }

    //6. Solicitud de permiso de camara
    private void voidRequestPermissions(View view) {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, 
                    new String[]{Manifest.permission.CAMERA},REQUEST_CODE);
        }
    }


      //6.1 Solicitud permisos External Storage
    private  void voidCheckPermissionExternalStorage (View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Solicita el permiso de almacenamiento externo
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE_PERMISSION );

        }
    }


    //6.2 Solicitud permisos Read Storage

    private  void voidCheckPermissionReadStorage(View view){
        // Verifica si el permiso de lectura de almacenamiento no ha sido otorgado
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Solicita el permiso de lectura de almacenamiento
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE_PERMISSION);
        }
    }



    //6.3 Solicitud permisos Record Audio
    private  void voidCheckPermissionRecordAudio(View view){
        // Verifica si el permiso de grabación de audio no ha sido otorgado
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Solicita el permiso de grabación de audio
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }


    //6.4 Solicitud permisos localizacion
    private void voidRequestPermissionLocation(View view){
        // Verifica si el permiso de acceso a la ubicación aproximada no ha sido otorgado
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Solicita el permiso de acceso a la ubicación aproximada
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION_PERMISSION);
        }
    }




    //7. Gestion de respuesa del usuario respecto a la solicitud del permiso Camara

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        tvResponse.setText(""+grantResults[0]);
        if (requestCode == REQUEST_CODE){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                new AlertDialog.Builder(this)
                        .setTitle("Box Permissions")
                        .setMessage("You denied the permissions Camera")
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        }).create().show();

            }else{
                Toast.makeText(this, "Usted no aprovo el permiso", Toast.LENGTH_LONG);
            }
        }else{
            Toast.makeText(this, "Usted aprovo el permiso", Toast.LENGTH_SHORT);
        }
    }





    ///2. Enlace de objetos
    private void initObject(){
        btnCheckPermissions = findViewById(R.id.btnCheckPermission);
        btnRequestPermissions = findViewById(R.id.btnRequestPermission);
        btnRequestPermissions.setEnabled(false);
        btnRequestPermissionRecordAudio=findViewById(R.id.btnRequestPermissionRecordAudio);
        btnRequestPermissionLocation=findViewById(R.id.btnRequestPermissionLocation);
        btnRequestPermissionExternalStorage=findViewById(R.id.btnRequestPermissionExternalStorage);
        btnRequestPermissionReadStorage=findViewById(R.id.btnRequestPermissionReadStorage);
        tvCamera = findViewById(R.id.tvCamera);
        tvExternalWS = findViewById(R.id.tvEws);
        tvReadExternalS = findViewById(R.id.tvRs);
        tvRecordAudio=findViewById(R.id.tvRecordAudio);
        tvConexion=findViewById(R.id.tvConexion);
        tvAccessCoarseLocation=findViewById(R.id.tvAccessCoarseLocation);
        tvResponse = findViewById(R.id.tvResponse);
        versipmAndroid = findViewById(R.id.tvVersionAndroid);
        pbLevelBatt = findViewById(R.id.pbLevelBatery);
        tvLevelBatt = findViewById(R.id.tvLevelBaterry);
        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);
        etNombreArchivo = findViewById(R.id.etNombreArchivo);
        btnGuardarArchivo = findViewById(R.id.btnGuardarArchivo);
    }
}