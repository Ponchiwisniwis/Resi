package com.example.resi_modulo1_2;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    boolean check = false;
    boolean isOpen = false;
    boolean fab = false;

    private Animation fab_open_anim, fab_close_anim, fab_rotate_anim;

    Uri imagenUri;
    GridView gvImagenes;
    FloatingActionButton fab_Seleccion, fab_Camara, fab_Galeria, fab_Video;
    TextView tv_Camara, tv_Galeria, tv_Video;
    EditText etComentario;
    Button btnEnviar;
    ProgressDialog progressDialog;
    //Video imports
    RequestQueue rQueue;
    ArrayList<HashMap<String, String>> arraylist;

    List<Uri> listaImagenes = new ArrayList<>();
    List<String> listaBase64Imagenes = new ArrayList<>();

    GridViewAdapter baseAdapter;

    String URL_UPLOAD_IMAGENES = "https://banyu.com.mx/alfonso/RecibirImagen.php";
    String URL_UPLOAD_VIDEO = "https://banyu.com.mx/alfonso/upload.php?";
    String Nombre;
    String Cadena;
    String Comentario;
    String aux;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle parametros = this.getIntent().getExtras();
        if(parametros != null)
        {
            Nombre = parametros.getString("Nombre");
            Cadena = parametros.getString("Cadena");
            Comentario = parametros.getString("Comentario");
            check = parametros.getBoolean("check");
            if(check)
            {
                subirImagenes();
            }
        }

        gvImagenes = findViewById(R.id.gvImagenes);
        fab_Seleccion = findViewById(R.id.fab_Seleccion);
        fab_Camara = findViewById(R.id.fab_Camara);
        fab_Galeria = findViewById(R.id.fab_Galeria);
        fab_Video = findViewById(R.id.fab_Video);
        tv_Camara = findViewById(R.id.tv_Camara);
        tv_Galeria = findViewById(R.id.tv_Galeria);
        tv_Video = findViewById(R.id.tv_Video);
        etComentario = findViewById(R.id.edComentario);
        btnEnviar = findViewById(R.id.btnEnviar);

        fab_open_anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_open);
        fab_close_anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_close);
        fab_rotate_anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_rotate);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        fab_Seleccion.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(isOpen)
                {
                    isOpen = false;
                    Ocultar();
                }
                else
                {
                    isOpen = true;
                    Mostrar();
                }
            }
        });

        fab_Camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tomarFoto();
                isOpen = false;
                Ocultar();
            }
        });

        fab_Galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galeria();
                isOpen = false;
                Ocultar();
            }
        });

        fab_Video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Video();
                isOpen = false;
                Ocultar();
            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                subirImagenes();
                btnEnviar.setVisibility(View.INVISIBLE);
                etComentario.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void Ocultar(){
        fab_Camara.startAnimation(fab_close_anim);
        fab_Galeria.startAnimation(fab_close_anim);
        fab_Video.startAnimation(fab_close_anim);
        tv_Camara.startAnimation(fab_close_anim);
        tv_Galeria.startAnimation(fab_close_anim);
        tv_Video.startAnimation(fab_close_anim);
    }

    private void Mostrar(){
        fab_Camara.startAnimation(fab_open_anim);
        fab_Galeria.startAnimation(fab_open_anim);
        fab_Video.startAnimation(fab_open_anim);
        tv_Camara.startAnimation(fab_open_anim);
        tv_Galeria.startAnimation(fab_open_anim);
        tv_Video.startAnimation(fab_open_anim);
        //fab_Seleccion.startAnimation(fab_rotate_anim);
    }

    private void galeria()
    {
        check=false;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona las imagenes"), 1);
        btnEnviar.setVisibility(View.VISIBLE);
        etComentario.setVisibility(View.VISIBLE);
    }

    public void tomarFoto()
    {
        Intent camara = new Intent(MainActivity.this, Camara.class);
        startActivity(camara);
        finish();
    }

    private void Video(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent,2);
    }

    public void mostrarProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Subiendo...");
        progressDialog.setMessage("Espera Por Favor");
        progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    public void subirImagenes()
    {
        if(check){
            //aqui es foto
            enviarImagenes(Nombre, Cadena, Comentario);
            check = false;
            Nombre = "";
            Cadena = "";
            Comentario = "";
        }
        else
        {
            //aqui es galeria
            for(int i = 0 ; i < listaImagenes.size() ; i++)
            {
                try
                {
                    mostrarProgressDialog();
                    Nombre = nombreRandom();
                    InputStream is = getContentResolver().openInputStream(listaImagenes.get(i));
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    String cadena = convertirBase64(bitmap);
                    Comentario = etComentario.getText().toString();
                    enviarImagenes(Nombre+i, cadena, Comentario);
                    bitmap.recycle();
                    limpiarTodo();
                    progressDialog.dismiss();
                } catch (IOException e)
                {
                }
            }
        }

    }

    public void enviarImagenes(final String nombre, final String cadena, final String comentario)
    {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPLOAD_IMAGENES,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("nombre", nombre);
                params.put("imagen", cadena);
                params.put("comentario", comentario);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public String convertirBase64(Bitmap bitmap)
    {
        Bitmap bm = scaleDown(bitmap, 500, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        byte[] bytes = baos.toByteArray();
        String encode = Base64.encodeToString(bytes, Base64.DEFAULT);
        return encode;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter)
    {
        float ratio = Math.min((float) maxImageSize / realImage.getWidth(), (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());
        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
        return newBitmap;
    }

    public String nombreRandom(){
        String temp;
        Random num = new Random();
        int num1 = 99+num.nextInt(1000);
        int num2 = 999+num.nextInt(1000);
        int num3 = 9999+num.nextInt(1000);
        temp = String.valueOf(num1)+String.valueOf(num2)+String.valueOf(num3);
        return temp;
    }

    public void limpiarTodo(){
        check = false;
        Nombre = "";
        Cadena = "";
        Comentario = "";
        listaImagenes.clear();
        listaBase64Imagenes.clear();
        etComentario.setText("");
        baseAdapter = new GridViewAdapter(MainActivity.this, listaImagenes);
        gvImagenes.setAdapter(baseAdapter);
    }
    //Video
    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
    //Video
    private void Subir(final String pdfname, Uri pdffile){

        InputStream iStream = null;
        try {
            iStream = getContentResolver().openInputStream(pdffile);
            final byte[] inputData = getBytes(iStream);
            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URL_UPLOAD_VIDEO,
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            Log.d("ressssssoo",new String(response.data));
                            rQueue.getCache().clear();
                            try {
                                JSONObject jsonObject = new JSONObject(new String(response.data));
                                Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                jsonObject.toString().replace("\\\\","");

                                if (jsonObject.getString("status").equals("true")) {
                                    Log.d("come::: >>>  ","yessssss");
                                    arraylist = new ArrayList<HashMap<String, String>>();
                                    JSONArray dataArray = jsonObject.getJSONArray("data");

                                    for (int i = 0; i < dataArray.length(); i++) {
                                        JSONObject dataobj = dataArray.getJSONObject(i);
                                        aux = dataobj.optString("pathToFile");
                                    }
                                }
                            } catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    // params.put("tags", "ccccc");  add string parameters
                    return params;
                }
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    params.put("filename", new DataPart(pdfname ,inputData));
                    return params;
                }
            };

            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            rQueue = Volley.newRequestQueue(MainActivity.this);
            rQueue.add(volleyMultipartRequest);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 5)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
            }
            else
            {
                Toast.makeText(MainActivity.this, "Habilita el uso de la camara por favor", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (keyCode)
            {
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //Imagenes
        if(resultCode == RESULT_OK && requestCode == 1)
        {
            ClipData clipData = data.getClipData();
            if(clipData == null)
            {
                imagenUri = data.getData();
                listaImagenes.add(imagenUri);
            }
            else
            {
                for (int i = 0; i < clipData.getItemCount(); i++)
                {
                    listaImagenes.add(clipData.getItemAt(i).getUri());
                }
            }
        }
        //Video
        else if(resultCode == RESULT_OK && requestCode == 2)
        {
            Uri uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = String.valueOf(Calendar.getInstance().getTimeInMillis()+".mp4");
            Log.d("ooooooo",displayName);
            Subir(displayName,uri);
        }
        baseAdapter = new GridViewAdapter(MainActivity.this, listaImagenes);
        gvImagenes.setAdapter(baseAdapter);
    }
}

//Animacion del boton de seleccion no sirve