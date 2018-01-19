package com.marvl.imt_lille_douai.marvl.comparison.tools;


import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.marvl.imt_lille_douai.marvl.controller.MainActivity;

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

import org.json.JSONObject;

public class ServerTools {

    final String TAG = MainActivity.class.getName();

    public static ArrayList<String>  loadClassifier (){
        ArrayList<String> classifierArray = new ArrayList<>();

        // TODO : remove and change with server request
        classifierArray.add("Coca.xml");
        classifierArray.add("Pepsi.xml");
        classifierArray.add("Sprite.xml");

        return classifierArray;
    }

    public static void getIndexJsonServ(Context context, VolleyCallback callback) {
        String url = "http://www-rech.telecom-lille.fr/nonfreesift/index.json";

        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("aaaa : " + response.toString());
                        callback.onSuccess(response);

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

    public JSONObject getIndexJsonServ2(Context context) {
        String url = "http://www-rech.telecom-lille.fr/nonfreesift/index.json";

        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), future, future);
        queue.add(request);
        JSONObject response=null;

        try {
            System.out.println("ici");
            response = future.get(3, TimeUnit.SECONDS);
            System.out.println("ici");
            return response;

        } catch (InterruptedException e) {
            // exception handling
        } catch (ExecutionException e) {
            // exception handling
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return response;
    }

    public void getXmlServ(Context context, String url){
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG,"@@REST"+"Response is: "+ response.substring(12000,12500));
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

        queue.add(stringRequest);
    }

}
