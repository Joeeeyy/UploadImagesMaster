package jjoey.com.uploadimagesmaster;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String URL = "http://192.168.0.4:9098/"; // replace with your device ip
    private static final int REQ_CODE = 9097;

    private ImageView imageView;
    private Button btn_uploadImage, btn_getImage;

    private String imgUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        btn_uploadImage = findViewById(R.id.btn_uploadImage);
        btn_getImage = findViewById(R.id.btn_getImage);

        btn_uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        btn_getImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Picasso.with(MainActivity.this)
//                        .load(imgUrl)
//                        .into(imageView);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(imgUrl));
                startActivity(intent);

            }
        });

    }

    private void openGallery() {
        Intent galIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galIntent.setType("image/jpeg");
        try {
            startActivityForResult(galIntent, REQ_CODE);
        } catch (ActivityNotFoundException Aex) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                int buffSize = 1024;
                byte[] buff = new byte[buffSize];

                int len = 0;
                while ((len = inputStream.read(buff)) != -1) {
                    baos.write(buff, 0, len);
                }

                byte[] upLoadBytes = baos.toByteArray();
                upLoadImageBytes(upLoadBytes);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void upLoadImageBytes(byte[] upLoadBytes) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface anInterface = retrofit.create(RestInterface.class);

        RequestBody imageFileBody = RequestBody.create(MediaType.parse("image/jpeg"), upLoadBytes);

        MultipartBody.Part partBody = MultipartBody.Part.createFormData("image", "user_avatar.jpg", imageFileBody);

        Call<Response> call = anInterface.upLoadImage(partBody);
        call.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if (response.isSuccessful()){
                    Log.d(TAG, "Response body is:\t" + response.body().toString());

                    Response responseBody = response.body();
                    imgUrl = URL + responseBody.getPath();

                    Log.d(TAG, "Get Image URL is:\t" + imgUrl);

                    Snackbar.make(findViewById(android.R.id.content), responseBody.getMessage(), Snackbar.LENGTH_LONG).show();

                } else {
                    ResponseBody errorResponse = response.errorBody();

                    Gson gson = new Gson();

                    Response errResp = gson.fromJson(errorResponse.toString(), Response.class);
                    Snackbar.make(findViewById(android.R.id.content), errResp.getMessage(), Snackbar.LENGTH_LONG).show();

                }

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.d(TAG, "Response error message is:\t" + t.getMessage().toString());
            }
        });

    }
}
