package test.tvdb.dev.com.tvdb_test;

import android.graphics.Bitmap;
import com.omertron.thetvdbapi.model.Episode;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MyTVSeries implements Serializable
{
    private String title,description,id,firstAired;
    private ArrayList<String> episodes,actors;
    private byte[] image;
    private Bitmap poster;
    private List<Episode> episodeList;
    private ArrayList<Season> seasons;

    public MyTVSeries(String title, String description, Bitmap poster, ArrayList<String> episodes,String id, String firstAired, ArrayList<String> actors,List<Episode> episodeList)
    {
        //TODO PARAMETRI PER EPISODI RIDONDANTI, DA FIXARE
        this.title = title;
        this.description = description;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        poster.compress(Bitmap.CompressFormat.PNG, 0, bos);
        image=bos.toByteArray();
        this.poster = poster;
        this.episodes = episodes;
        this.id=id;
        this.actors=actors;
        this.firstAired = firstAired;
        this.episodeList = episodeList;
        if(this.episodeList != null) {
            manageSeasons();
            //Log.v("Emil","Seasons size: "+seasons.size());
        }
        //else Log.v("Emil", "EpisodeList null");
    }

    //STILL TO IMPROVE. THIS CONSTRUCTOR IS CALLED BY DATABASE CLASS
    public MyTVSeries(String title, String description, ArrayList<String> episodes, String id, String firstAired, byte[] image, ArrayList<Season> seasons, ArrayList<String> actors){
        this.title = title;
        this.description = description;
        this.image = image;
        this.episodes = episodes;
        this.id = id;
        this.firstAired = firstAired;
        //this.episodeList = episodeList;
        this.seasons = seasons;
        this.actors = actors;
    }

    public void setEpisodes(ArrayList<String> episodes) { this.episodes=episodes; }

    public void setEpisodes(List<Episode> episodes)
    {
        ArrayList<String> tmp=new ArrayList<>();
        for(int i=0;i<episodes.size();i++)
            tmp.add(episodes.get(i).getEpisodeName());
        this.episodes=tmp;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public byte[] getPoster() {
        return image;
    }

    public Bitmap getBitmapPoster() { return poster; }

    public byte[] getBitmapArray()
    {
        return image;
    }

    public ArrayList<String> getEpisodes() {
        return episodes;
    }

    public String getID() {return id;}

    public String getFirstAired() {return firstAired;}

    public ArrayList<String> getActors()
    {
        return actors;
    }

    public int getTotSeasons(){ return seasons.size(); }

    public int getLastSeasonNumber(){
        if(seasons.size()>0)
            return seasons.get(seasons.size()-1).getSeasonNumber();
        return 0;
    }

    public Season getLastSeason(){
        if(seasons.size()>0)
            return seasons.get(seasons.size() - 1);
        return null;
    }

    public int getFirstSeasonNumber(){
        if(seasons.size()>0)
            return seasons.get(0).getSeasonNumber();
        return -1;
    }

    public Season getSeason(int seasonNumber){
        //return seasons.get(seasonNumber);

        //for a more strict control
        int i = 0;
        try
        {
            while (seasons.get(i).getSeasonNumber() != seasonNumber)
                i++;
        }
        catch(IndexOutOfBoundsException exc)
        {
            if(seasonNumber==0&&seasons.get(0).getSeasonNumber()==1)            //in case of minxed seasonNumber value
                return seasons.get(0);                                          //(for example passing 0 when seasons start from 1)
        }
        if(seasons.get(i).getSeasonNumber()==seasonNumber)
            return seasons.get(i);
        else return null;
    }

    public ArrayList<Season> getSeasons(){ return seasons; }

    private void manageSeasons(){
        seasons = new ArrayList<Season>();
        boolean noSeason0 = true;  //true if no Season 0 and false if there is Season 0
        for(int i=0, j=0; i<episodeList.size(); i++){
           Episode e = episodeList.get(i);
           if(e.getSeasonNumber() == j){
               if(seasons.size() <= j && seasons.indexOf(new Season(e.getSeasonId(), j))==-1) {   //se non è ancora stata creata la stagione
                   seasons.add(new Season(e.getSeasonId(), j));
               }
               if(j==0)
                   noSeason0 = false;
               if(!noSeason0)
                   seasons.get(j).addEpisode(e);
               else seasons.get(j-1).addEpisode(e); //i.e season 1 goes in array[0]
           }
           else {
               j++;
               i--;
           }
        }
    }

    @Override
    public boolean equals(Object o) {
        return (this.id.equals(((MyTVSeries) o).getID()));

    }
}
