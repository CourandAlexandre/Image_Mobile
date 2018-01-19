package com.marvl.imt_lille_douai.marvl.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.marvl.imt_lille_douai.marvl.BuildConfig;
import com.marvl.imt_lille_douai.marvl.R;
import com.marvl.imt_lille_douai.marvl.comparison.tools.GlobalTools;
import com.marvl.imt_lille_douai.marvl.comparison.tools.ServerTools;
import com.marvl.imt_lille_douai.marvl.comparison.tools.SiftTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.bytedeco.javacpp.opencv_features2d.drawMatches;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;
import static org.bytedeco.javacpp.opencv_highgui.namedWindow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String TAG = MainActivity.class.getName();

    protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
    final int captureActivityResult = 100;
    final int libraryActivityResult = 200;
    final int analyseActivityResult = 300;

    final int photoRequestActivityResult = 400;
    final int resultCodeActivityResult = 500;

    Button captureButton;
    Button libraryButton;
    Button analyseButton;
    Button websiteButton;

    ImageView photoView;

    String photoTakenPath;
    Uri photoTakenUri;

    private static final String SHARED_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        captureButton = (Button) findViewById(R.id.captureButton);
        captureButton.setOnClickListener(this);

        libraryButton = (Button) findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(this);

        analyseButton = (Button) findViewById(R.id.analyseButton);
        analyseButton.setOnClickListener(this);

        photoView = (ImageView) findViewById(R.id.imageAnalysed);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission wasn't allowed");
            captureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                captureButton.setEnabled(true); // If user doesn't have the permission the button is hidden
                Log.i(TAG, "Permission wasn't allowed but now is granted");
            }
        }
    }

    public void onClick(View view) {
        if (view == findViewById(R.id.captureButton)) {
            startCaptureActivity();
        } else if (view == findViewById(R.id.libraryButton)) {
            startLibraryActivity();
        } else if (view == findViewById(R.id.analyseButton)) {
            startAnalyseActivity();
        } else if (view == findViewById(R.id.websiteButton)) {
            startWebsiteActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                switch (requestCode) {
                    case captureActivityResult:
                        photoView.setImageURI(photoTakenUri);

                        break;

                    case libraryActivityResult:
                        //processPhotoLibrary(intent);

                        //Log.i(TAG,intent.toString());

                        Uri photoUri = intent.getData();
                        photoView.setImageURI(photoUri);

                        Log.i(TAG, photoUri.toString());
                        break;

                    case analyseActivityResult:

                        break;
                }
                break;
        }
    }

    protected void processPhotoLibrary(Intent intent) {
        Uri photoUri = intent.getData();
        String pathToPhoto = GlobalTools.getRealPath(getApplicationContext(), photoUri);

        File pathToFile = new File(pathToPhoto);
        Bitmap photoBitmap = GlobalTools.decodeFile(pathToFile); // err -> Maybe on path

        photoView.setImageBitmap(photoBitmap);

        Log.i(TAG, pathToPhoto);
    }

    protected void startCaptureActivity() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null; // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                //Uri photoUri = FileProvider.getUriForFile(this,SHARED_PROVIDER_AUTHORITY,photoFile);
                Uri photoUri = FileProvider.getUriForFile(this.getApplicationContext(), "com.marvl.imt_lille_douai.marvl.fileprovider", photoFile);

                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                startActivityForResult(intent, captureActivityResult);
            }
        }
    }

    protected void startLibraryActivity() {
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, libraryActivityResult);
    }

    protected void startWebsiteActivity() {
        Uri uri = Uri.parse("http://www.google.com/#q=fish"); //lance internet

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        startActivity(intent);
    }

    //ArrayList<String> classifier = new ArrayList<>();

    protected void startAnalyseActivity() {

        ArrayList<String> classifierArray = ServerTools.loadClassifier();
        final CvSVM[] classifiers = SiftTools.initClassifiersAndCacheThem(this, classifierArray) ;

        System.out.println("class0 " + classifiers[0].get_support_vector_count());
        System.out.println("class1 " + classifiers[1].sizeof());
        System.out.println("class2 " + classifiers[2].sizeof());

        SiftTools.doComparison(this,classifierArray,classifiers);

        /*getIndexJsonServ(new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                try {
                    System.out.println("JeSuisUnTest");
                    for(int i=0;i<json.getJSONArray("brands").length(); i++) {
                        System.out.println("JeSuisUnTest2");
                       // classifier.add(json.getJSONArray("brands").getJSONObject(i).getString("classifier"));
                        System.out.println("aaaaaaa : " + json.getJSONArray("brands").getJSONObject(i).getString("classifier"));
                        addToClassifier(json.getJSONArray("brands").getJSONObject(i).getString("classifier"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });*/

        // charge index.json, parse le, remplir class_name avec le json.

    }

    public void addToClassifier(String string) {
//this.classifier.add(string);
    }

    // Create an image file name
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file : path for use with ACTION_VIEW intents
        photoTakenPath = image.getAbsolutePath();
        photoTakenUri = Uri.fromFile(image);

        //galleryAddPic(image);

        //folder stuff
        /*File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
        imagesFolder.mkdirs();

        File image = new File(Environment.getExternalStorageDirectory(),"fname_" +
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        Uri uriSavedImage = Uri.fromFile(image);*/

        return image;
    }

    private void galleryAddPic(File f) {
        Log.i(TAG, "TRUUUUC : " + f.getAbsolutePath());
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(photoTakenUri);
        this.sendBroadcast(mediaScanIntent);
    }
}