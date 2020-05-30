package com.example.resi_modulo1_2;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.exifinterface.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Camara extends AppCompatActivity
{

    int TOMAR_FOTO = 2;

    boolean check = true;

    String CARPETA_RAIZ = "MisFotos-";
    String CARPETAS_IMAGENES = "Banyu";
    String RUTA_IMAGEN = CARPETA_RAIZ + CARPETAS_IMAGENES;
    String path;
    String Nombre="";
    String Comentario="";
    String Cadena="";

    ImageView ivCamara;
    EditText edComentario;
    FloatingActionButton fabEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);
        getSupportActionBar().hide();

        ivCamara = findViewById(R.id.ivCamara);
        edComentario = findViewById(R.id.etComentario);
        fabEnviar = findViewById(R.id.fabEnviar);

        edComentario.setVisibility(View.INVISIBLE);
        fabEnviar.setVisibility(View.INVISIBLE);

        fabEnviar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Comentario = edComentario.getText().toString();
                Intent regresar = new Intent(Camara.this, MainActivity.class);
                regresar.putExtra("Nombre", Nombre);
                regresar.putExtra("Cadena", Cadena);
                regresar.putExtra("Comentario", Comentario);
                regresar.putExtra("check", check);
                startActivity(regresar);
                limpiarTodo();
                finish();
            }
        });
        //checar aqui el deprecated
        File fileImagen = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGEN);
        boolean isCreada = fileImagen.exists();
        if(!isCreada)
        {
            isCreada = fileImagen.mkdirs();
        }
        Nombre = nombreRandom();
        //checar aqui el deprecated
        path = Environment.getExternalStorageDirectory()+File.separator+RUTA_IMAGEN+File.separator+Nombre;
        File imagen = new File(path);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            String authorities = this.getPackageName()+".provider";
            Uri imageUri = FileProvider.getUriForFile(this, authorities, imagen);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        }
        else
        {
            intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(imagen));
        }
        startActivityForResult(intent,TOMAR_FOTO);
    }

    public static int readPictureDegree(String path)
    {
        int degree  = 0;
        try
        {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return degree;
    }

    public String nombreRandom()
    {
        String temp;
        Random num = new Random();
        String num1 = String.valueOf(99+num.nextInt(1000));
        String num2 = String.valueOf(999+num.nextInt(1000));
        String num3 = String.valueOf(9999+num.nextInt(1000));
        temp = num1+num2+num3;
        return temp;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter)
    {
        float ratio = Math.min((float) maxImageSize / realImage.getWidth(), (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());
        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
        return newBitmap;
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

    public void limpiarTodo()
    {
        check = false;
        Nombre = "";
        Cadena = "";
        Comentario = "";
        path = "";
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Toast.makeText(this,"Regresando...", Toast.LENGTH_SHORT).show();
        Intent regresar = new Intent(Camara.this, MainActivity.class);
        startActivity(regresar);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == TOMAR_FOTO)
        {
            MediaScannerConnection.scanFile(Camara.this, new String[]{path},null, new MediaScannerConnection.OnScanCompletedListener()
            {
                @Override
                public void onScanCompleted(String s, Uri uri)
                {
                }
            });
            Bitmap bm = BitmapFactory.decodeFile(path);
            int correcion = readPictureDegree(path);
            ivCamara.animate().setDuration(1).rotation(correcion).start();
            ivCamara.setImageBitmap(bm);
            edComentario.setVisibility(View.VISIBLE);
            fabEnviar.setVisibility(View.VISIBLE);
            Nombre = nombreRandom();
            Cadena = convertirBase64(bm);
        }
    }
}
