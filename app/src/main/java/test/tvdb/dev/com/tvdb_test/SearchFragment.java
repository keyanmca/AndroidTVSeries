package test.tvdb.dev.com.tvdb_test;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.TvDbException;
import com.omertron.thetvdbapi.model.Banner;
import com.omertron.thetvdbapi.model.Banners;
import com.omertron.thetvdbapi.model.Episode;
import com.omertron.thetvdbapi.model.Series;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {

    private ImageView poster;
    private TextView desc,episodeLabel;
    private ListView list;
    private ProgressBar bar;
    private EditText editText;
    private Button search;
    private TheTVDBApi tvDB;
    private ArrayList<MyTVSeries> myTvSeries;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.search,container,false);
        /*poster=(ImageView)rootView.findViewById(R.id.poster);
        desc=(TextView)rootView.findViewById(R.id.description);
        list=(ListView)rootView.findViewById(R.id.listView);*/
        bar=(ProgressBar)rootView.findViewById(R.id.progressBar);
        editText=(EditText)rootView.findViewById(R.id.search_box);
        search=(Button)rootView.findViewById(R.id.search_button);
        //episodeLabel=(TextView)rootView.findViewById(R.id.episode_label);
        myTvSeries=read();
        if(myTvSeries==null)
            myTvSeries=new ArrayList<>();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadSeriesData().execute(editText.getText().toString());
            }
        });
        bar.setVisibility(View.VISIBLE);
        bar.setIndeterminate(true);
        new LoginTVDB().execute();
        return rootView;
    }

    private class LoginTVDB extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            tvDB=new TheTVDBApi("2C8BD989F33B0C84");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            bar.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(),"You can now search a TV Series",Toast.LENGTH_SHORT).show();
        }
    }

    private class DownloadSeriesData extends AsyncTask<String,Void,Bitmap[]>  //TODO implement serial AsyncTasks for each series
    {
        private String description;
        private List<Episode>[] episodes;
        private Banners banner;
        private List<Series> results,backup;

        @Override
        protected void onPreExecute() {
            /*poster.setVisibility(View.INVISIBLE);
            desc.setVisibility(View.INVISIBLE);
            list.setVisibility(View.INVISIBLE);
            episodeLabel.setVisibility(View.INVISIBLE);*/
            bar.setVisibility(View.VISIBLE);
            bar.setIndeterminate(true);
        }

        @Override
        protected Bitmap[] doInBackground(String[] params)
        {
            try {
                results=tvDB.searchSeries(params[0],"en");
                backup=new ArrayList<>();
                backup.addAll(results);  //needed in case of null posters
                episodes=new List[results.size()];
                Bitmap[] poster=new Bitmap[results.size()];
                try
                {
                    for(int i=0,j=0;i<episodes.length;i++,j++)
                    {
                        episodes[i]=tvDB.getAllEpisodes(results.get(i).getId(),"en");
                        banner=tvDB.getBanners(results.get(i).getId());
                        try
                        {
                            List<Banner> banners=banner.getSeasonList();
                            InputStream in=new java.net.URL(banners.get(0).getUrl()).openStream();
                            poster[i]=BitmapFactory.decodeStream(in);
                        }
                        catch(IndexOutOfBoundsException exc)
                        {
                            System.out.println("No poster has been found");
                            poster[i]=null; //no poster has been found
                            backup.remove(j);
                            j--;
                        }
                    }

                }
                catch (MalformedURLException ex)
                {
                    ex.printStackTrace();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
                /*boolean checkPresence = false;
                for (int i = 0; i < myTvSeries.size(); i++)
                    if (myTvSeries.get(i).getDescription().equals(description))
                        checkPresence = true;
                if (!checkPresence) {
                    ArrayList<String> _episodes = new ArrayList<>();
                    for (int i = 0; i < episodes.size(); i++)
                        _episodes.add(episodes.get(i).getEpisodeName());
                    MyTVSeries _myTVSeries = new MyTVSeries(results.get(0).getSeriesName(), description, poster, _episodes, results.get(0).getId());
                    myTvSeries.add(_myTVSeries);
                    write(myTvSeries);
                }*/
                Bitmap[] validPosters=new Bitmap[backup.size()];
                for(int i=0,j=0;i<poster.length;i++)
                    if(poster[i]!=null)
                    {
                        validPosters[j]=poster[i];
                        j++;
                    }
                return validPosters;
            }
            catch(TvDbException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmap) {
            /*poster.setVisibility(View.VISIBLE);
            desc.setVisibility(View.VISIBLE);
            list.setVisibility(View.VISIBLE);
            episodeLabel.setVisibility(View.VISIBLE);*/
            bar.setVisibility(View.INVISIBLE);
            bar.setIndeterminate(false);
            /*poster.setImageBitmap(bitmap);
            desc.setText(description);
            final ArrayList<String> _episodes=new ArrayList<>();
            for(int i=0;i<episodes.size();i++)
                _episodes.add(episodes.get(i).getEpisodeName());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,_episodes);
            list.setAdapter(adapter);
            ListViewHelper.getListViewSize(list);*/
            System.out.println("Valid series found : "+backup.size());
            GridView gridView=(GridView)rootView.findViewById(R.id.gridView);
            GridViewAdapter customGridAdapter = new GridViewAdapter(getActivity(),R.layout.grid_cell,backup,episodes,bitmap,myTvSeries);
            gridView.setAdapter(customGridAdapter);
        }
    }

    private ArrayList<MyTVSeries> read()
    {
        try
        {
            FileInputStream fis=getActivity().openFileInput("TV_Series.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<MyTVSeries> object=(ArrayList<MyTVSeries>)ois.readObject();
            return object;
        }
        catch(FileNotFoundException exc)
        {
            return null;
        }
        catch(IOException exc)
        {
            return null;
        }
        catch(ClassNotFoundException exc)
        {
            return null;
        }
    }

    private void write(ArrayList<MyTVSeries> tvSeries)
    {
        FileOutputStream fos;
        try
        {
            fos=getActivity().openFileOutput("TV_Series.dat",Context.MODE_PRIVATE);
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            oos.writeObject(tvSeries);
            oos.close();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
