package com.example.imagestoretos3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.imagestoretos3.Utils.Util;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    Button btnUpload;
    ImageView imgGallery;
    private final int GALLERY_REQUEST = 200;

    // Permissions for devices below Android 13 (Tiramisu)
    String[] storagePermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Permissions for devices running Android 13 (Tiramisu) and above
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    String[] storagePermissions_33 = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES
    };

    String filePath = "no_pic";
    Bitmap finalBitmap = null;
    String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        btnUpload.setOnClickListener(view -> {
            openGallery();
        });

    }

    @AfterPermissionGranted(GALLERY_REQUEST)
    private void openGallery() {
        if (hasGalleryPermission()) {
            // Have permission, do the thing!
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Start the Intent
            startActivityForResult(galleryIntent, GALLERY_REQUEST);
        } else {
            String[] perms;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms = storagePermissions_33;
            } else {
                perms = storagePermissions;
            }


            EasyPermissions.requestPermissions(this, "This app require permission for accessing gallery.", GALLERY_REQUEST, perms);
        }
    }

    public void initializeViews() {
        btnUpload = findViewById(R.id.btnUpload);
        imgGallery = findViewById(R.id.img_gallery);
    }

    private String[] permissionsForGallery() {
        String[] per;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            per = storagePermissions_33;
        } else {
            per = storagePermissions;
        }
        return per;
    }

    private Boolean hasGalleryPermission() {
        String[] permissions = permissionsForGallery();
        if (permissions != null) {
            return EasyPermissions.hasPermissions(this, permissions);
        }
        return null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == GALLERY_REQUEST) {
                Log.e("=====From Gallery", "=========");
                Uri contentUri = data.getData();
                filePath = Util.getPath(contentUri, this);
                try {

                    if (Build.VERSION.SDK_INT < 28) {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(),
                                contentUri);
                        finalBitmap = bitmap;

                        Glide.with(getApplicationContext())
                                .load(new File(filePath)) // Uri of the picture
                                .placeholder(R.drawable.ic_launcher_round)
                                .into(imgGallery);

                    } else {
                        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), contentUri);
                        Bitmap bitmap = ImageDecoder.decodeBitmap(source);

                        finalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                        Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(finalBitmap) // Uri of the picture
                                .placeholder(R.drawable.ic_launcher_round)
                                .into(imgGallery);

                    }

                    key = "ProfilePicSample.png";

                    //Upload image to S3
                    Util.uploadImageToS3(this, finalBitmap, key);

                } catch (Exception e) {
                    Log.e("Exception: ", "=========" + e.getMessage());
                    e.printStackTrace();
                }

            }
        }

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.e("onPermissionsGranted:", requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.e("onPermissionsDenied:", requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.e("onRationaleAccepted", "*************");
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.e("onRationaleDenied", "*************");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}