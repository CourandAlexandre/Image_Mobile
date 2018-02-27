package com.marvl.imt_lille_douai.marvl.controller;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
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

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.marvl.imt_lille_douai.marvl.BuildConfig;
import com.marvl.imt_lille_douai.marvl.R;
import com.marvl.imt_lille_douai.marvl.comparison.image.ComparedImage;
import com.marvl.imt_lille_douai.marvl.comparison.image.Img;
import com.marvl.imt_lille_douai.marvl.comparison.tools.GlobalTools;
import com.marvl.imt_lille_douai.marvl.comparison.tools.ServerTools;
import com.marvl.imt_lille_douai.marvl.comparison.tools.SiftTools;
import com.marvl.imt_lille_douai.marvl.comparison.tools.SystemTools;
import com.marvl.imt_lille_douai.marvl.comparison.variables.AndroidVariables;
import com.marvl.imt_lille_douai.marvl.comparison.variables.GlobalVariables;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.bytedeco.javacpp.opencv_ml;
import org.bytedeco.javacpp.opencv_ml.CvSVM;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String TAG = MainActivity.class.getName();

    Button captureButton;
    Button libraryButton;
    Button analyseButton;
    Button websiteButton;

    ImageView photoView;

    Img img = new Img("ImageBank/TestImage/Pepsi_13.jpg");

    ServerTools serverTools = new ServerTools();

    RequestQueue requestWithCache;
    private Cache cache;

    ArrayList<File> classifierArray ;
    CvSVM[] classifiers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        setupButtons();

        photoView = (ImageView) findViewById(R.id.imageAnalysed);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission wasn't allowed");
            System.out.println(GlobalVariables.debugTag + " onCreate Permission wasn't allowed" );

            captureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        // Clear cache before start
        SystemTools.clearCache(this);

        prepareAnalyseActivity();

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
                    case AndroidVariables.captureActivityResult:

                        /** resize **/

                        beginCrop(img.getImageUri());

                        /** resize **/

                        System.out.println(GlobalVariables.debugTag + " inCaptureActivityResult ");
                        photoView.setImageURI(img.getImageUri());

                        Bitmap bitmap = ((BitmapDrawable)photoView.getDrawable()).getBitmap();
                        File recupImg = SystemTools.convertBitmapToFile(this,bitmap,img.getImageName());

                        img.setImagePath(recupImg.getAbsolutePath());

                        break;

                    case AndroidVariables.libraryActivityResult:
                        //processPhotoLibrary(intent);

                        img.setImageUri(intent.getData());
                        photoView.setImageURI(img.getImageUri());

                        System.out.println(GlobalVariables.debugTag + " LibraryActivityResult img : " + img.toString());
                        break;

                    case AndroidVariables.analyseActivityResult:

                        break;
                    case Crop.REQUEST_PICK:
                        beginCrop(intent.getData());
                        break;
                    case Crop.REQUEST_CROP:
                        handleCrop(resultCode, intent);
                        break;
                }
                break;
            case Activity.RESULT_CANCELED :
                System.out.println(GlobalVariables.debugTag + " onActivityResult : ResultCanceled" );
                break ;
        }
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
                img.setImageUri(FileProvider.getUriForFile(this.getApplicationContext(), "com.marvl.imt_lille_douai.marvl.fileprovider", photoFile));

                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, img.getImageUri());

                startActivityForResult(intent, AndroidVariables.captureActivityResult);
            }
        }
    }

    protected void startLibraryActivity() {
        Intent intent = new Intent();

        intent.setType("img/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, AndroidVariables.libraryActivityResult);
    }

    protected void startWebsiteActivity() {
        Uri uri = Uri.parse("http://www.google.com/#q=fish"); //lance internet

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        startActivity(intent);

        /* V1
        String bestSimilitudePath= SimilitudeTools.getMostSimilitudeImageComparedToDataBank(photoTakenPath,dataBank);
        Log.i("ahah",bestSimilitudePath);
        setContentView(R.layout.analyse_layout);
        websiteButton = (Button) findViewById(R.id.websiteButton);
        websiteButton.setOnClickListener(this);
         */
    }

    protected void prepareAnalyseActivity(){
        // Instantiate the cache
        cache = new DiskBasedCache(getCacheDir(), GlobalVariables.maxCacheSizeInBytes);

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestWithCache = new RequestQueue(cache, network);
        requestWithCache.start(); // Start the queue

        serverTools.loadClassifierInCache(this, cache);
    }

    // TODO : renvoi Android vers bouton de la marque du bestMatchImage
    protected void startAnalyseActivity()  {
        // Put the taken photo into cache to be faster if img has been set
        /*if( !img.getImageUri().equals(null) ){
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String imagePathExternal = storageDir + "/" + img.getImageName();

            System.out.println(GlobalVariables.debugTag + " imagePathFrom external dir : " + imagePathExternal);

            File copyTakenPhotoToCache = SystemTools.toCache(this,imagePathExternal,img.getImageName());

            img.setImagePath(copyTakenPhotoToCache.getAbsolutePath());

            System.out.println(GlobalVariables.debugTag + " Img path : " + img.getImagePath() );
        }*/

        classifierArray = SystemTools.convertCacheToClassifierArray(this);
        System.out.println(GlobalVariables.debugTag + " classifierArray : " + classifierArray.toString());

        opencv_ml.CvSVM[] classifiers = SiftTools.initClassifiersAndCacheThem(this, classifierArray);

        ComparedImage comparedImage = SiftTools.doComparison(this, classifierArray, classifiers, img.getImagePath()); // Default ImageBank/TestImage/Pepsi_13.jpg

        System.out.println(GlobalVariables.debugTag + comparedImage.toString());

        // Remove tested img from cache after analyse
        SystemTools.clearFileFromCache(this, img.getImageName());
    }

    // Create an img file name
    private File createImageFile() throws IOException {
        System.out.println(GlobalVariables.debugTag + " inCreateImageFile");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String imagePath = storageDir + "/" + imageFileName + ".jpg";

        File image = new File(imagePath);
        image.getParentFile().mkdirs();
        image.createNewFile();
        image.canRead();

        System.out.println(GlobalVariables.debugTag + " newFile : " + image.toString());

        //File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file : path for use with ACTION_VIEW intents
        img.setImageUri(Uri.fromFile(image));

        //galleryAddPic(img);

        //folder stuff
        /*File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
        imagesFolder.mkdirs();

        File img = new File(Environment.getExternalStorageDirectory(),"fname_" +
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        Uri uriSavedImage = Uri.fromFile(img);*/

        return image;
    }

    // TODO
    private void galleryAddPic(File f) {
        Log.i(TAG, "TRUUUUC : " + f.getAbsolutePath());
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(img.getImageUri());
        this.sendBroadcast(mediaScanIntent);
    }

    protected void processPhotoLibrary(Intent intent) {
        Uri photoUri = intent.getData();
        String pathToPhoto = GlobalTools.getRealPath(getApplicationContext(), photoUri);

        File pathToFile = new File(pathToPhoto);
        Bitmap photoBitmap = GlobalTools.decodeFile(pathToFile); // err -> Maybe on path

        photoView.setImageBitmap(photoBitmap);

        Log.i(TAG, pathToPhoto);
    }

    protected void setupButtons(){
        captureButton = (Button) findViewById(R.id.captureButton);
        captureButton.setOnClickListener(this);

        libraryButton = (Button) findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(this);

        analyseButton = (Button) findViewById(R.id.analyseButton);
        analyseButton.setOnClickListener(this);
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), img.getImageName()));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            photoView.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            //Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}