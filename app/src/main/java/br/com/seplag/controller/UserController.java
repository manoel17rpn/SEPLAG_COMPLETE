package br.com.seplag.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import br.com.seplag.model.UserModel;
import br.com.seplag.view.MainActivity;

/**
 * Created by Manoel Neto on 29/04/2017.
 */

public class UserController {
    private static final String URL_POST = "http://192.168.112.105:8000/rest-api/user-methods/create";
    private static final String URL_GET = "http://192.168.112.105:8000/rest-api/user-methods/get/";
    RetryPolicy policy = new DefaultRetryPolicy(45000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    public void CreateUser(final Context context, final UserModel user, final VolleyCallback callback) {
        final ProgressDialog dialog = ProgressDialog.show(context, "Carregando", "Estamos realizando seu cadastro, por favor aguarde...", true);

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.POST, URL_POST, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    dialog.dismiss();

                    callback.onSuccess(response);
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.dismiss();
                    error.printStackTrace();

                    NetworkResponse response = error.networkResponse;
                    if(response != null && response.data != null){
                        callback.onFailed(response.statusCode, "");
                    }else{
                        String errorMessage=error.getClass().getSimpleName();
                        if(!TextUtils.isEmpty(errorMessage)){
                            callback.onFailed(0, errorMessage);
                        }
                    }
                }
            }
        ){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("user_name", user.getUser_name());
                params.put("user_phone", user.getUser_phone());
                params.put("user_neighborhood", user.getUser_neighborhood());
                params.put("user_street", user.getUser_street());
                params.put("user_score", Integer.toString(user.getUser_score()));
                params.put("user_office", user.getUser_office());

                return params;
            }
        };

        postRequest.setRetryPolicy(policy);
        queue.add(postRequest);
    }

    public UserModel LoginUser(final Context context, String UserNumber, final VolleyCallbackGet callback){
        final ProgressDialog dialog = ProgressDialog.show(context, "Carregando", "Realizando login, por favor aguarde...", true);
        final UserModel user = new UserModel();
        RequestQueue queue = Volley.newRequestQueue(context);

        // prepare the Request
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, URL_GET + UserNumber, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray array) {
                        JSONObject jsonObject;
                        JSONArray array1;

                        try{
                            if(array.getJSONArray(0).length() > 0){

                                array1 = new JSONArray(array.getJSONArray(0).toString());
                                jsonObject = new JSONObject(array1.getJSONObject(0).toString());

                                user.setUser_id(jsonObject.getInt("user_id"));
                                user.setUser_name(jsonObject.getString("user_name"));
                                user.setUser_phone(jsonObject.getString("user_phone"));
                                user.setUser_neighborhood(jsonObject.getString("user_neighborhood"));
                                user.setUser_score(jsonObject.getInt("user_score"));
                                user.setUser_office(jsonObject.getString("user_office"));

                                callback.onSucess(true);
                            }else if(array.getJSONArray(0).length() == 0){
                                Toast.makeText(context, "Número inválido, verifique seu número...", Toast.LENGTH_LONG).show();
                            }

                            dialog.dismiss();
                        }catch (JSONException e){
                                e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        callback.onFailed(false);

                        dialog.dismiss();
                    }
                }
        );

        getRequest.setRetryPolicy(policy);
        queue.add(getRequest);

        return user;
    }

    public interface VolleyCallback{
        void onSuccess(String result);
        void onFailed(int result, String menssager);
    }

    public interface VolleyCallbackGet{
        void onSucess(boolean result);
        void onFailed(boolean error);
    }
}