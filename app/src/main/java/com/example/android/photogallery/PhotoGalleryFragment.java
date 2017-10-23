package com.example.android.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d("TEST","beforefetch");
        new FetchItemTask().execute();
        Log.d("TEST","afterfetch");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mPhotoRecyclerView=(RecyclerView) v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        Log.d("TEST","beforeadap");
        //Dummy fill
        for(int i=0;i<10;i++){
            GalleryItem ite=new GalleryItem();
            ite.setCaption("DEMO"+i);
            mItems.add(ite);
        }

        //Dummy fill
        setupAdapter();
        Log.d("TEST","afteradap");
        return v;
    }
    private void setupAdapter(){
        //iaAdded to check whether fragment has been attached to activity or not
        //we call this bcz we are using asynctask which is a background thread and fragment can deattach
        //while processing n backgrond so we check,normally its not required
        Log.d("TEST","beforeisadd");
        if(isAdded()){
            Log.d("TEST","afterisadd");
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }
    private class PhotoHolder extends RecyclerView.ViewHolder{
        private TextView mTitleTextView;
        public PhotoHolder(View itemView) {
            super(itemView);
            mTitleTextView=(TextView) itemView;
        }
        public void bindGalleryItem(GalleryItem item){
            mTitleTextView.setText(item.toString());
        }
    }
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        private List<GalleryItem> mGalleryItems;
        public PhotoAdapter(List<GalleryItem> galleryItems){
            mGalleryItems=galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView=new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem item=mGalleryItems.get(position);
            holder.bindGalleryItem(item);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }


    private class FetchItemTask extends AsyncTask<Void,Void,List<GalleryItem>>{

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            Log.d("TEST","indoin");
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
            Log.d("TEST","inonpost");
            setupAdapter();
        }
    }
}
