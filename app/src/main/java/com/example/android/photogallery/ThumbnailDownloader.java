package com.example.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by prate on 23-10-2017.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD=0;

    private boolean mHasQuit=false;
    private Handler mRequestHandler;
    private ConcurrentHashMap<T,String> mRequestMap=new ConcurrentHashMap<>();
    //This is noting but thread safe version of HashMap
    private Handler mResponseHandler;
    private ThumbnailDownloadListner<T> mThumbnailDownloadListner;

    public interface ThumbnailDownloadListner<T>{
        void onThumbnailDownloaded(T target,Bitmap thumbnail);
    }
    public void setmThumbnailDownloadListner(ThumbnailDownloadListner<T> listner){
        mThumbnailDownloadListner=listner;
    }


    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler=responseHandler;
    }
    //This is called when looper checks the queue for the first time
    @Override
    protected void onLooperPrepared() {
        Log.i(TAG,"onlooperprep");
        mRequestHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Log.i(TAG,"2");
                if(msg.what==MESSAGE_DOWNLOAD){
                    T target=(T) msg.obj;
                    Log.i(TAG,"got a request from url"+mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    private void handleRequest(final T target) {
        final String url=mRequestMap.get(target);
        if (url==null){
            return;
        }
        try {
            byte[] bitmapBytes=new FlickrFetchr().getUrlBytes(url);
             final Bitmap bitmap= BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG,"Bitmap created");
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target)!=url|| mHasQuit){
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListner.onThumbnailDownloaded(target,bitmap);
                }
            });
        } catch (IOException e) {
           Log.e(TAG,"Error downloding image");
        }

    }

    @Override
    public boolean quit() {
        mHasQuit=true;
        return super.quit();
    }
    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }
    public void queueThumbnail(T target,String url){
        Log.i(TAG,"Got a url:"+url);
        if(url==null){
            mRequestMap.remove(target);
        }else {
            Log.i(TAG,MESSAGE_DOWNLOAD+"");
            mRequestMap.put(target,url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();
            //sendToTarget()-Sends this Message to the Handler specified by getTarget().
            //mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target) Returns a new Message
            Log.i(TAG,"1");

        }
    }
}
