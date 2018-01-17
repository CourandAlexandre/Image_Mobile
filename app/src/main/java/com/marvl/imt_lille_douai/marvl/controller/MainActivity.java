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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.marvl.imt_lille_douai.marvl.BuildConfig;
import com.marvl.imt_lille_douai.marvl.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        String pathToPhoto = getRealPath(getApplicationContext(), photoUri);

        File pathToFile = new File(pathToPhoto);
        Bitmap photoBitmap = decodeFile(pathToFile); // err -> Maybe on path

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

    protected void startAnalyseActivity() {

        final Mat vocabulary;

        System.out.println("read vocabulary from file... ");
        Loader.load(opencv_core.class);
        String[] listURl = null;
        try {
            listURl = this.getAssets().list("yml"); //recup list image
        } catch (IOException e) {
            e.printStackTrace();
        }

        String photo = ToCache(this, "yml/" + listURl[0], listURl[0]).getAbsolutePath();
        System.out.println("testtest : " + photo);
        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage(photo, null, opencv_core.CV_STORAGE_READ); // change et met url cache du fichier
        System.out.println("storage" + storage);
        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new opencv_core.Mat(cvMat);
        System.out.println("vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        opencv_core.cvReleaseFileStorage(storage);


        //create SIFT feature point extracter
        final SIFT detector;
        // default parameters ""opencv2/features2d/features2d.hpp""
        detector = new SIFT(0, 3, 0.04, 10, 1.6);

        //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
        final FlannBasedMatcher matcher;
        matcher = new FlannBasedMatcher();

        //create BoF (or BoW) descriptor extractor
        final BOWImgDescriptorExtractor bowide;
        bowide = new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

        //Set the dictionary with the vocabulary we created in the first step
        bowide.setVocabulary(vocabulary);
        System.out.println("Vocab is set");

        int classNumber = 3;
        String[] class_names;
        class_names = new String[classNumber];

        // charge index.json, parse le, remplir class_name avec le json.

        class_names[0] = "Coca";
        class_names[1] = "Pepsi";
        class_names[2] = "Sprite";

        final CvSVM[] classifiers;
        classifiers = new CvSVM[classNumber];
        for (int i = 0; i < classNumber; i++) {
            //System.out.println("Ok. Creating class name from " + className);
            //open the file to write the resultant descriptor
            classifiers[i] = new CvSVM();
            System.out.println("class " + classifiers[i].get_support_vector_count());
            String fileTemp = ToCache(this, "classifier/" + class_names[i] + ".xml", class_names[i] + ".xml").getAbsolutePath();
            System.out.println(fileTemp);
            classifiers[i].load(fileTemp); // load xml dans classifier en cache url

        }
        System.out.println("class0 " + classifiers[0].get_support_vector_count());
        System.out.println("class1 " + classifiers[1].sizeof());
        System.out.println("class2 " + classifiers[2].sizeof());


        Mat response_hist = new Mat();
        KeyPoint keypoints = new KeyPoint();
        Mat inputDescriptors = new Mat();

        //System.out.println("path:" + im.getName());

        String photoTest = ToCache(this, "ImageBank/TestImage/Pepsi_13.jpg", "Pepsi_13.jpg").getAbsolutePath();

        //File im = new File( "ImageBank/TestImage/Coca_12.jpg");

        Mat imageTest = imread(photoTest, 1);
        detector.detectAndCompute(imageTest, Mat.EMPTY, keypoints, inputDescriptors);
        bowide.compute(imageTest, keypoints, response_hist);

        // Finding best match
        float minf = Float.MAX_VALUE;
        String bestMatch = null;

        long timePrediction = System.currentTimeMillis();
        // loop for all classes
        for (int i = 0; i < classNumber; i++) {
            // classifier prediction based on reconstructed histogram
            float res = classifiers[i].predict(response_hist, true);
            //System.out.println(class_names[i] + " is " + res);
            if (res < minf) {
                minf = res;
                bestMatch = class_names[i];
            }
        }
        timePrediction = System.currentTimeMillis() - timePrediction;
        System.out.println(photoTest + "  predicted as " + bestMatch + " in " + timePrediction + " ms");
        getIndexJsonServ();
    }

    protected void startWebsiteActivity() {


        //lance internet
        Uri uri = Uri.parse("http://www.google.com/#q=fish");

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);


        startActivity(intent);
    }

    protected String getRealPath(Context context, Uri uri) {
        Cursor cursor;

        String[] projection = {MediaStore.Images.Media.DATA};
        cursor = context.getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null
        );

        int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(dataIndex);
    }

    protected Bitmap decodeFile(File file) {
        Bitmap bitmap = null;

        try {
            FileInputStream inputStream = new FileInputStream(file); // System.err

            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
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

    public static File ToCache(Context context, String Path, String fileName) {
        InputStream input;
        FileOutputStream output;
        byte[] buffer;
        String filePath = context.getCacheDir() + "/" + fileName;
        File file = new File(filePath);
        AssetManager assetManager = context.getAssets();

        try {
            input = assetManager.open(Path);
            buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            output = new FileOutputStream(filePath);
            output.write(buffer);
            output.close();
            return file;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getIndexJsonServ() {
        String url = "http://www-rech.telecom-lille.fr/nonfreesift/index.json";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("Responseaaaaa : " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Error.Responseaaaa : " + error.toString());
                    }
                }
        );

        queue.add(getRequest);
    }
}