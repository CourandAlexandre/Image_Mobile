package com.marvl.imt_lille_douai.marvl.comparison.tools;


import android.content.Context;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.marvl.imt_lille_douai.marvl.comparison.image.ComparedImage;
import com.marvl.imt_lille_douai.marvl.controller.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.bytedeco.javacpp.opencv_features2d.drawMatches;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;
import static org.bytedeco.javacpp.opencv_highgui.namedWindow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;

import org.bytedeco.javacpp.opencv_ml;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerTools {

    final String TAG = MainActivity.class.getName();

    ArrayList<File> classifierArray = new ArrayList<>();

    public ServerTools(){

    }

    public ArrayList<File>  loadClassifier (Context context, RequestQueue requestWithCache){



        getIndexJsonServ(context, requestWithCache);

        // TODO : remove and change with server request
        return classifierArray;
    }

    public void getIndexJsonServ(Context context, RequestQueue requestWithCache){

        String url = "http://www-rech.telecom-lille.fr/nonfreesift/index.json";

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("aaaaa json : " + response);
                        try {
                            for(int i=0;i<response.getJSONArray("brands").length(); i++) {
                                String urlXml = "http://www-rech.telecom-lille.fr/nonfreesift/" + response.getJSONArray("brands").getJSONObject(i).getString("classifier");

                                getStringServ(context,response.getJSONArray("brands").getJSONObject(i).getString("classifier"), requestWithCache, response.getJSONArray("brands").length(), i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // mTextView.setText("That didn't work!");
                        Log.d(TAG,"@@REST That didn't work!");
                    }
                }
        );

        queue.add(jsonRequest);
    }

    public void getStringServ(Context context, String xml, RequestQueue requestWithCache, int size, int fichier){

        String url = "http://www-rech.telecom-lille.fr/nonfreesift/classifiers/" + xml;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        final DiskBasedCache diskCache = (DiskBasedCache) requestWithCache.getCache();
                        File file = diskCache.getFileForKey(url);

                        classifierArray.add(GlobalTools.toCacheServ(context,file));

                        if(size == fichier+1) {
                            findBestTruc(classifierArray, context);
                        }

                        System.out.println("aaaa xml : " + file.length());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // mTextView.setText("That didn't work!");
                        Log.d(TAG,"@@REST That didn't work!");
                    }
                }
        );

        requestWithCache.add(stringRequest);
    }

    public void findBestTruc(ArrayList<File> classifierArray, Context context){
        final opencv_ml.CvSVM[] classifiers = SiftTools.initClassifiersAndCacheThem(context, classifierArray) ;
        System.out.println("class0 " + classifiers[0].get_support_vector_count());
        System.out.println("class1 " + classifiers[1].sizeof());
        System.out.println("class2 " + classifiers[2].sizeof());

        ComparedImage comparedImage = SiftTools.doComparison(context,classifierArray,classifiers);

        System.out.println(comparedImage.getImageName() + "  predicted as " + comparedImage.getBestMatchImage() + " in " + comparedImage.getTimePrediction() + " ms");

    }

}
