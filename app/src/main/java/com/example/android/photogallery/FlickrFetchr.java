package com.example.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by prate on 22-10-2017.
 */

public class FlickrFetchr {
    private static final String TAG="FlickrFetchr";
    private static final String API_KEY="12616cb7eee6415789e3927cbedcc81b";
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url=new URL(urlSpec);
        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            InputStream in=connection.getInputStream();
            if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+": with"+urlSpec);
            }
            int byteRead=0;
            byte[] buffer=new byte[1024];
            while ((byteRead=in.read(buffer))>0){
                out.write(buffer,0,byteRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }

    }

    public String getUrlString(String urlSpecs) throws IOException {
        return new String(getUrlBytes(urlSpecs));
    }

    public List<GalleryItem> fetchItems(){
        String url= Uri.parse("https://api.flickr.com/services/rest/").buildUpon().appendQueryParameter("method","flickr.photos.getRecent")
                    .appendQueryParameter("api_key",API_KEY).appendQueryParameter("format","json").appendQueryParameter("nojsoncallback","1")
                    .appendQueryParameter("extras","url_s").build().toString();
        List<GalleryItem> items=new ArrayList<>();

        try {
            String jsonString=getUrlString(url);
            Log.i(TAG,"Recived JSON="+jsonString);
            JSONObject jsonBody=new JSONObject(jsonString);
            parseItem(items,jsonBody);
        } catch (IOException e) {
            Log.e(TAG,"Failed to fetch json",e);
        } catch (JSONException e) {
            Log.e(TAG,"Failed to parse json",e);
        }

        return items;

    }

    public void parseItem(List<GalleryItem> items,JSONObject jsonBody) throws JSONException {
        JSONObject photosJsonObject=jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray=photosJsonObject.getJSONArray("photo");
        for(int i=0;i<photoJsonArray.length();i++){
            JSONObject photoJsonObject=photoJsonArray.getJSONObject(i);
            GalleryItem item=new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            if(!photoJsonObject.has("url_s")){
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);

        }
    }
}
