package com.example.imagestoretos3.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.loader.content.CursorLoader;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Util {

    private static final String BUCKET_NAME = "your bucket name"; //Replace with your bucket name
    private static final String REGION = "your region";  // Replace with your bucket's region
    private static final String ACCESS_KEY = "Access key";  // Replace with your access key
    private static final String SECRET_KEY = "secret key";  // Replace with your secret key

    //for uploading image to S3 bucket
    public static URL generatePresignedUrl(String key) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        AmazonS3Client s3Client = new AmazonS3Client(awsCreds, Region.getRegion(REGION));

        // Set the pre-signed URL to expire after 10 Minutes.
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 10 * 60 * 1000;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(BUCKET_NAME, key)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);
        return s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    //for viewing image from S3 in android app
    public static URL generatePreSignedUrlForView(String key) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        AmazonS3Client s3Client = new AmazonS3Client(awsCreds, Region.getRegion(REGION));

        // Set the pre-signed URL to expire after 10 Minutes.
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 10 * 60 * 1000;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(BUCKET_NAME, key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    public static String getPath(Uri contentUri, Context context) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    //Using Volley library to upload image to S3 using preSigned Url
    public static void uploadImageToS3(Context context, Bitmap bitmap, String key) {
        URL presignedUrl = generatePresignedUrl(key);
        Log.e("preSignedUrl: ", String.valueOf(presignedUrl));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        final byte[] imageData = byteArrayOutputStream.toByteArray();

        StringRequest putRequest = new StringRequest(Request.Method.PUT, presignedUrl.toString(),
                response -> {
                    Log.e("ImageUploader", "Upload successful");
                    Log.e("Response-Success: ",""+response);
                },
                error -> Log.e("ImageUploader", "Upload failed", error)) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return imageData;
            }

            @Override
            public String getBodyContentType() {
                return "image/jpeg";
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return Response.success(new String(response.data), HttpHeaderParser.parseCacheHeaders(response));
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "image/jpeg");
                return headers;
            }
        };

        // Add the request to the RequestQueue.
        AppController.getInstance(context).addToRequestQueue(putRequest);
    }
}
