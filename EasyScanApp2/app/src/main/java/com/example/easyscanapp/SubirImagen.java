package com.example.easyscanapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import id.zelory.compressor.Compressor;
import io.reactivex.annotations.Nullable;

public class SubirImagen extends AppCompatActivity {

    ImageView foto;
    Button subir, seleccionar;
    DatabaseReference imgref;
    StorageReference storageReference;
    ProgressDialog cargando;

    Bitmap thumb_bitmap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subir_imagen);

        foto = findViewById(R.id.img_foto);
        seleccionar = findViewById(R.id.boton4);
        subir = findViewById(R.id.boton5);
        imgref = FirebaseDatabase.getInstance().getReference().child("Fotos_subidas");
        storageReference = FirebaseStorage.getInstance().getReference().child("img_comprimidas");
        cargando = new ProgressDialog(this);
        seleccionar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                CropImage.startPickImageActivity(SubirImagen.this);
            }
        });


    }// fin del oncreate

    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode ==Activity.RESULT_OK){
            Uri imageuri = CropImage.getPickImageResultUri(this,data);

            // Recortar imagen...

            CropImage.activity(imageuri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setRequestedSize(640,480)
                    .setAspectRatio(2,1).start(SubirImagen.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                File url = new File(resultUri.getPath());

                Picasso.with(this).load(url).into(foto);
                // comprimiendo imagen --------
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(640)
                            .setMaxHeight(480)
                            .setQuality(90)
                            .compressToBitmap(url);
                }catch (IOException e){
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream);
                final byte [] thumb_byte = byteArrayOutputStream.toByteArray();
                ///// Fin del compresor :D

                int p = (int) (Math.random()* 25 + 1);int s = (int) (Math.random() * 25 + 1);
                int t = (int) (Math.random()* 25 + 1);int c = (int) (Math.random() * 25 + 1);
                int numero1 = (int) (Math.random() * 1012 + 2111);
                int numero2 = (int) (Math.random() * 1012 + 2111);
                String[] elementos = {"a", "b", "c", "d","e", "f", "g", "h","i", "j", "k",
                        "l","m", "n","o", "p", "q", "r","s", "t","u", "v", "w", "x","y", "z"};
                final String aleatorio = elementos[p] + elementos[s] +
                        numero1 + elementos[t] + elementos[c] + numero2 + "comprimido.jpg";
                subir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        cargando.setTitle("Subiendo foto...");
                        cargando.setMessage("Espere por favor...");
                        cargando.show();

                        final StorageReference ref = storageReference.child(aleatorio);
                        UploadTask uploadTask = ref.putBytes(thumb_byte);

                        //subir imagen en storage -----...

                        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()){
                                    throw Objects.requireNonNull(task.getException());

                                }
                                return ref.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                Uri downloaduri = task.getResult();
                                imgref.push().child("urlfoto").setValue(downloaduri.toString());
                                cargando.dismiss();

                                Toast.makeText(SubirImagen.this,"Imagen cargada con exito", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });

            }
        }
    }


    public void Regresar (View view)
    {
        Intent regresar = new Intent(this, MainActivity.class);
        startActivity(regresar);


    }
}
