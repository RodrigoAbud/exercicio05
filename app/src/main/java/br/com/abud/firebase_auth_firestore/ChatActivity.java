package br.com.abud.firebase_auth_firestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mensagensRecyclerView;
    private ChatAdapter adapter;
    private List<Mensagem> mensagens;
    private FirebaseUser fireUser;
    private CollectionReference collMensagensReferences;

    private EditText mensagemEdittext;

    private double latitude;
    private double longitude;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_PERMISSION_COD_GPS = 1001;
    private static final int REQ_COD_CAMERA = 1001;

    private void setupFirebase (){
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        //Observavel
        collMensagensReferences = FirebaseFirestore.getInstance().collection("mensagem");
        //Observador
        collMensagensReferences.addSnapshotListener((result, e) -> {
            mensagens.clear();
            for (DocumentSnapshot doc : result.getDocuments()){
                Mensagem m = doc.toObject(Mensagem.class);
                mensagens.add(m);
            }
            Collections.sort(mensagens);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupFirebase();
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000,
                    10,
                    locationListener
            );
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1001);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mensagemEdittext = findViewById(R.id.mensagemEditText);
        mensagensRecyclerView = findViewById(R.id.mensagensRecyclerView);
        mensagens = new ArrayList<>();
        adapter = new ChatAdapter(this,mensagens);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mensagensRecyclerView.setAdapter(adapter);
        mensagensRecyclerView.setLayoutManager(llm);

        ////////////////////////////////////enviar localização//////////////////////////////////////
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_COD_GPS){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            2000,
                            10,
                            locationListener);
                }
            }else {
                Toast.makeText(this,getString(R.string.no_gps_no_app),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void enviarMensagem(View view) {
        String texto = mensagemEdittext.getText().toString();
        Mensagem m = new Mensagem(texto, new java.util.Date(),fireUser.getEmail(),2);
        collMensagensReferences.add(m);
        mensagemEdittext.setText("");
    }

    //location
    public void enviarLocalizacao(View view) {
        String localizacao = getString(R.string.lat_long,latitude,longitude);
        Mensagem m = new Mensagem(localizacao, new java.util.Date(),fireUser.getEmail(), 1);
        collMensagensReferences.add(m);
        mensagemEdittext.setText("");
    }

    //image
    public void enviarImagem(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQ_COD_CAMERA);
        }else{
            Toast.makeText(ChatActivity.this, getString(R.string.cant_take_pic), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQ_COD_CAMERA:
                if(resultCode != RESULT_OK){
                    Toast.makeText(this, getString(R.string.cant_take_pic), Toast.LENGTH_SHORT).show();
                    return;
                }

                Bitmap picture = (Bitmap)data.getExtras().get("data");
                Date date = new Date();

                String mensagem = DateHelper.format(date).replace("/", "-") + ".jpg";
                Mensagem m = new Mensagem (mensagem, date, fireUser.getEmail(), 3);

                StorageReference pictureStorageReference = FirebaseStorage.getInstance()
                        .getReference(
                                String.format(
                                        Locale.getDefault(),
                                        "mensagens/%s/%s",
                                        m.getEmail().replace("@", ""),
                                        m.getTexto()));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                picture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bytes = baos.toByteArray();

                pictureStorageReference.putBytes(bytes);

                collMensagensReferences.add(m);

                break;
        }

    }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////

class ChatViewHolder extends RecyclerView.ViewHolder {
    public TextView dataNomeTextView;
    public TextView mensagemTextView;
    public ImageView profileImageView;
    public ImageView abrirMapaButton;
    public ImageView sendedImage;

    public ChatViewHolder(View raiz) {
        super(raiz);
        dataNomeTextView = raiz.findViewById(R.id.dataNomeTextView);
        mensagemTextView = raiz.findViewById(R.id.mensagemTextView);
        profileImageView = raiz.findViewById(R.id.profilePicImageView);
        abrirMapaButton = raiz.findViewById(R.id.abrirMapaButton);
        sendedImage = raiz.findViewById(R.id.sendedImage);
    }
}

class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder>{

    private Context context;
    private List <Mensagem> mensagens;
    private Map<String, Bitmap> fotos;

    public ChatAdapter(Context context, List<Mensagem> mensagens) {
        this.context = context;
        this.mensagens = mensagens;
        fotos = new HashMap<>();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return null;
        LayoutInflater inflater = LayoutInflater.from(context);
        View raiz = inflater.inflate(R.layout.list_item,parent,false);
        return new ChatViewHolder(raiz);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Mensagem m = mensagens.get(position);
        if (m.getTipoMsg() == 1){
            holder.dataNomeTextView.setText(
                    context.getString(R.string.data_nome,
                            DateHelper.format(m.getDate()),
                            m.getEmail()));
            holder.mensagemTextView.setText(m.getTexto());
            holder.abrirMapaButton.setOnClickListener(v -> {
                Uri uri = Uri.parse(m.getTexto().replace("Lat:","geo:").replace("Long:",","));
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                intent.setPackage("com.google.android.apps.maps");
                context.startActivity(intent);
            });
            holder.abrirMapaButton.setVisibility(View.VISIBLE);
            holder.mensagemTextView.setVisibility(View.VISIBLE);
            holder.sendedImage.setVisibility(View.INVISIBLE);
        }else if (m.getTipoMsg() == 2){
            holder.dataNomeTextView.setText(
                    context.getString(R.string.data_nome,
                            DateHelper.format(m.getDate()),
                            m.getEmail()));
            holder.mensagemTextView.setText(m.getTexto());
            holder.abrirMapaButton.setVisibility(View.INVISIBLE);
            holder.mensagemTextView.setVisibility(View.VISIBLE);
            holder.sendedImage.setVisibility(View.INVISIBLE);
        }else{
            holder.abrirMapaButton.setVisibility(View.INVISIBLE);
            holder.mensagemTextView.setVisibility(View.INVISIBLE);
            holder.sendedImage.setVisibility(View.VISIBLE);
            holder.dataNomeTextView.setText(
                    context.getString(R.string.data_nome,
                            DateHelper.format(m.getDate()),
                            m.getEmail()));
            StorageReference pictureStorageReference = FirebaseStorage.getInstance()
                    .getReference(
                            String.format(
                                    Locale.getDefault(),
                                    "mensagens/%s/%s",
                                    m.getEmail().replace("@", ""),
                                    m.getTexto()));

            pictureStorageReference.getDownloadUrl()
                    .addOnSuccessListener((result) -> {

                        Glide.with(context).
                                asBitmap().addListener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                holder.sendedImage.setImageBitmap(resource);



                                holder.sendedImage.setOnClickListener(v ->{
                                    Intent intent = new Intent(context, FullImageActivity.class);
                                    Drawable figura = ((AppCompatImageView) v).getDrawable();

                                    Bitmap bitmap = ((BitmapDrawable)figura).getBitmap();
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                    byte[] b = baos.toByteArray();

                                    intent.putExtra("figura", b);
                                    context.startActivity(intent);

                                });

                                return true;
                            }
                        }).
                                load(pictureStorageReference).
                                into(holder.sendedImage);

                    })
                    .addOnFailureListener((exception) -> {

                        holder.sendedImage.setImageResource(R.drawable.ic_person_black_50dp);

                    });

        }


        StorageReference pictureStorageReference = FirebaseStorage.getInstance().getReference(
                String.format(Locale.getDefault(),"image/%s/profilePic.jpg",
                        m.getEmail().replace("@",""))
        );
        if (fotos.containsKey(m.getEmail())){
            holder.profileImageView.setImageBitmap(fotos.get(m.getEmail()));
        }else {
            pictureStorageReference.getDownloadUrl().addOnSuccessListener(
                    (result) -> {
                        Glide.with(context).
                                asBitmap().addListener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                fotos.put(m.getEmail(),resource);
                                return true;
                            }
                        }).
                                load(pictureStorageReference).into(holder.profileImageView);
                    }
            )
                    .addOnFailureListener(
                            (exeption) -> {
                                holder.profileImageView.setImageResource(R.drawable.ic_person_black_50dp);
                            }
                    );
        }
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }
}


