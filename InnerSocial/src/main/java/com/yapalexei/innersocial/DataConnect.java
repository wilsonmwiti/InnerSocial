package com.yapalexei.innersocial;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexeiyagolnikov on 8/27/13.
 */
public class DataConnect{
    private boolean connection;
    private String usr;
    private String psw;
    private String url;
    private String post;
    private String responseBody;
    public Map<String,String[]> hashContentTable = new HashMap<String,String[]>();


    public DataConnect(String url, String usr, String psw, String post) {
        this.url = url;
        this.usr = usr;
        this.psw = psw;
        this.post = post;
        responseBody = "false";
        connection = false;
    }

    public String getResults(){
        return responseBody;
    }

    public boolean isConnected(){
        return connection;
    }

    private String convertToSQLPostableFormat(String post){
        String convertedString = "INSERT INTO `yagoln5_android`.`content` " +
                "(`ID`, `display`, `UserName`, `Content`, `Date`)" +
                "VALUES (NULL, \'1\', \'" + this.usr +
                "\', \'" + this.post + "\', CURRENT_TIMESTAMP);";

        return convertedString;
    }

    public boolean connect(){
        HttpClient httpclient;
        HttpPost httpPost;
        InputStream is = null;
        HttpEntity entity = null;

        ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
        httpclient = new DefaultHttpClient();
        httpPost = new HttpPost(url);
        nameValuePairs = new ArrayList<BasicNameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("theuser", usr));
        nameValuePairs.add(new BasicNameValuePair("thepass", psw));
        if (post.length() > 0)
            nameValuePairs.add(new BasicNameValuePair("thepost", convertToSQLPostableFormat(post)));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e("LOGIN_PROCESS", "UnsupportedEncodingException - " + e.toString());
            return connection;
        }


        try{
            HttpResponse response = httpclient.execute(httpPost);
            entity = response.getEntity();
            is = ((HttpEntity) entity).getContent();
        } catch (ClientProtocolException e) {
            Log.e("LOGIN_PROCESS","ClientProtocolException - " + e.toString());
            return connection;
        } catch (IOException e) {
            Log.e("LOGIN_PROCESS", "IOException - " + e.toString());
            return connection;
        }



        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
            StringBuilder sb = new StringBuilder();
            sb.append(reader.readLine() + "\n"); // take th \n out
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            responseBody = sb.toString();
            connection = true;

        }catch(Exception e){
            Log.e("LOGIN_PROCESS", "Input Stream - Error converting result: " + e.toString());
            return connection;
        }

        return connection;
    }


}
