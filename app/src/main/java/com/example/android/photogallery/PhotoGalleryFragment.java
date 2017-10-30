package com.example.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by prate on 22-10-2017.
 */

public class PhotoGalleryFragment extends Fragment {

    public static final String TAG="PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems=new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemTask().execute();
        Handler responseHandler=new Handler();
        mThumbnailDownloader=new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setmThumbnailDownloadListner(new ThumbnailDownloader.ThumbnailDownloadListner<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable=new BitmapDrawable(getResources(),thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        //This method returns the Looper associated with this thread. If this thread not been started or for any reason isAlive() returns false,
        //this method will return null. If this thread has been started, this method will block until the looper has been initialized.
        mThumbnailDownloader.getLooper();
        Log.i(TAG,"Background thread started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        mThumbnailDownloader.clearQueue();
        Log.i(TAG,"Background thread destroyed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mPhotoRecyclerView=(RecyclerView) v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        setupAdapter();
        return v;
    }
    private void setupAdapter(){
        //iaAdded to check whether fragment has been attached to activity or not
        //we call this bcz we are using asynctask which is a background thread and fragment can deattach
        //while processing n backgrond so we check,normally its not required
        if(isAdded()){
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }
    private class PhotoHolder extends RecyclerView.ViewHolder{
        //private TextView mTitleTextView;
        private ImageView imageView;
        public PhotoHolder(View itemView) {
            super(itemView);
            //mTitleTextView=(TextView) itemView;
            imageView=(ImageView) itemView.findViewById(R.id.item_image_view);
        }
        //for picasso and no need of any handler or looper
       /* public void bindGalleryItem(GalleryItem item){
           Picasso.with(getActivity()).load(item.getUrl()).into(imageView);
        }*/
        public void bindDrawable(Drawable drawable){
            imageView.setImageDrawable(drawable);
        }
    }
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        private List<GalleryItem> mGalleryItems;
        public PhotoAdapter(List<GalleryItem> galleryItems){
            mGalleryItems=galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //TextView textView=new TextView(getActivity());
            //return new PhotoHolder(textView);
            LayoutInflater inflater=LayoutInflater.from(getActivity());
            View view=inflater.inflate(R.layout.list_item_gallery,parent,false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem item=mGalleryItems.get(position);
            mThumbnailDownloader.queueThumbnail(holder,item.getUrl());



        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }


    private class FetchItemTask extends AsyncTask<Void,Void,List<GalleryItem>>{

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
           /* try {
                String result=new FlickrFetchr().getUrlString("https://www.bignerdranch.com");
                Log.i(TAG,"Fetched content of url"+result);
            } catch (IOException e) {
                Log.e(TAG,"Failed to fetch url",e);
            }
            */
           return new FlickrFetchr().fetchItems();
        }
        //this automatically  runs once doinBackground is done,all gui changes should be handled here
        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems=galleryItems;
            setupAdapter();
        }
    }
}
