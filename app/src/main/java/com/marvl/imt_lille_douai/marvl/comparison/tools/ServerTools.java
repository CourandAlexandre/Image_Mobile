package com.marvl.imt_lille_douai.marvl.comparison.tools;


import android.content.Context;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.marvl.imt_lille_douai.marvl.comparison.image.ComparedImage;
import com.marvl.imt_lille_douai.marvl.comparison.variables.GlobalVariables;
import com.marvl.imt_lille_douai.marvl.controller.MainActivity;

import java.io.File;
import java.util.ArrayList;

import org.bytedeco.javacpp.opencv_ml;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerTools {

    ArrayList<File> classifierArray = new ArrayList<>();
    JSONObject json = null;

    public ServerTools(){
        // Default constructor -> non static
    }

    public ArrayList<File>  loadClassifier (Context context, Cache cache){
        getIndexJsonServ(context, cache);

        System.out.println(GlobalVariables.debugTag + "loadClassifier(), classifierArray : " + classifierArray.toString());

        return classifierArray;
    }

    public void loadClassifierInCache (Context context, Cache cache){
        getIndexJsonServ(context, cache);
    }

    public void getIndexJsonServ(Context context, Cache cache){
        String url = GlobalVariables.serverUrl +"/index.json";

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(GlobalVariables.debugTag + " VolleyResponse : " + response);
                        json = response;
                        //System.out.println(GlobalVariables.debugTag + " : ici : " + response.getJSONArray("vocabulary").toString());
                        getYmlServ(context,cache);

                        try {
                            for(int i=0; i<response.getJSONArray("brands").length(); i++) {
                                String urlXml = GlobalVariables.serverUrl + response.getJSONArray("brands").getJSONObject(i).getString("classifier");

                                getStringServ(context,response.getJSONArray("brands").getJSONObject(i).getString("classifier"), cache, response.getJSONArray("brands").length(), i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(GlobalVariables.debugTag + "Volley JSON error : " + error); // OR mTextView.setText("That didn't work!");
                    }
                }
        );

        queue.add(jsonRequest);
    }

    public void getStringServ(Context context, String xml, Cache cache, int size, int fichier){
        String url = GlobalVariables.serverUrl + "/classifiers/" + xml;

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //final DiskBasedCache diskCache = (DiskBasedCache) cache;
                        //File file = diskCache.getFileForKey(url);

                        File file = SystemTools.putFileIntoLocal(context, xml,response);
                        classifierArray.add(SystemTools.putFileIntoLocal(context, xml,response));

                        System.out.println(GlobalVariables.debugTag + " getStringServ : " + file.toString());

                        // TODO : Check for remove
                        if(size == fichier+1) {
                            /*findBestTruc(classifierArray, context);
                            System.out.print("AAAA : " + xml);
                            classifierArray = SystemTools.convertCacheToClassifierArray(context);
                            System.out.println("AAA : classifierArray " + classifierArray.toString());

                            opencv_ml.CvSVM[] classifiers = SiftTools.initClassifiersAndCacheThem(context, classifierArray);*/
                        }

                        System.out.println(GlobalVariables.debugTag + " xml file.length : " + file.length());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(GlobalVariables.debugTag +  "Volley string error : " + error); // OR mTextView.setText("That didn't work!");
                    }
                }
        );

        queue.add(stringRequest);
    }

    public void getYmlServ(Context context, Cache cache){

        String url = GlobalVariables.serverUrl + "/vocabulary.yml";

        System.out.println(GlobalVariables.debugTag + " url yml : " + url);

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        File file = SystemTools.putFileIntoLocal(context, "/vocabulary.yml",response);

                        System.out.println(GlobalVariables.debugTag + " getStringServ : " + file.toString());

                        System.out.println(GlobalVariables.debugTag + " xml file.length : " + file.length());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(GlobalVariables.debugTag +  "Volley string error : " + error); // OR mTextView.setText("That didn't work!");
                    }
                }
        );

        queue.add(stringRequest);
    }

    public void getImage(String name, ImageView mImageView, Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = GlobalVariables.serverUrl + "/train-images/" + name;
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        mImageView.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        
                    }
                });
        queue.add(request);
    }

    public JSONObject getJson() {
        return json;
    }
}
