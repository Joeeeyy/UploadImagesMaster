package jjoey.com.uploadimagesmaster;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by JosephJoey on 12/2/2017.
 */

public interface RestInterface {

    @Multipart
    @POST("/images/uploads")
    Call<Response> upLoadImage(@Part MultipartBody.Part image);

}
