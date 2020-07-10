package com.example.recview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chootdev.recycleclick.RecycleClick;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button bParseBelta, bParseSputnik, bParseMlyn;
    TextView tvUrlInfo;

    BottomNavigationView bottomNavigationView;

    int pageNumber = 1;

    String beltaAllNewsUrl = "https://www.belta.by/all_news/page/" + pageNumber;
    String beltaUrl = "https://www.belta.by";

    String sputnikUrl = "https://sputnik.by";
    String sputnikAllNewsUrl = "https://sputnik.by/archive/";

    String mlynUrl = "";
    String mlynAllNewsUrl = "https://www.mlyn.by/novosti/page/" + pageNumber;

    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount, lastVisibleItem;

    List<News> news = new ArrayList<>();

    DataAdapter adapter;

    public ArrayList<String> titleList = new ArrayList<>();
    public ArrayList<String> textList = new ArrayList<>();
    public ArrayList<String> timeList = new ArrayList<>();
    public ArrayList<String> linkList = new ArrayList<>();

    public ArrayList<String> sputnikNextPageLink = new ArrayList<>();
    public ArrayList<String> beltaNextPageLink = new ArrayList<>();
    public ArrayList<String> mlynNextPageLink = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvUrlInfo = findViewById(R.id.tvUrlInfo);

        RecyclerView recyclerView = findViewById(R.id.list);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        adapter = new DataAdapter(this, news);
        recyclerView.setAdapter(adapter);

        RecycleClick.addTo(recyclerView).setOnItemClickListener(new RecycleClick.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int i, View view) {

                Intent browserIntent = new
                        Intent(Intent.ACTION_VIEW, Uri.parse(tvUrlInfo.getText() + linkList.get(i)));
                        startActivity(browserIntent);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                    lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();

                    if (loading) {
                        if ( (visibleItemCount + pastVisiblesItems) == totalItemCount) {
                            loading = false;
                            if (tvUrlInfo.getText().equals(beltaUrl)){
                                ParseNextPageBeltaTask pnpbt = new ParseNextPageBeltaTask();
                                pnpbt.execute();
                            } else if (tvUrlInfo.getText().equals(sputnikUrl)){
                                ParseNextPageSputnikTask pnpst = new ParseNextPageSputnikTask();
                                pnpst.execute();
                            } else if (tvUrlInfo.getText().equals(mlynUrl)){
                                ParseNextPageMlynTask pnpmt = new ParseNextPageMlynTask();
                                pnpmt.execute();
                            }
                            adapter.notifyDataSetChanged();
                            loading = true;
                        }
                    }
                }
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_parse_belta:
                                onParseBelta();
                                item.setChecked(true);
                                break;
                            case R.id.action_parse_sputnik:
                                onParseSputnik();
                                item.setChecked(true);
                                break;
                            case R.id.action_parse_mlyn:
                                onParseMlyn();
                                item.setChecked(true);
                                break;
                        }
                        return false;
                    }
                });
    }

    class ParseBeltaTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Document doc;
            try {
                doc = Jsoup.connect(beltaAllNewsUrl).get();

                Elements title = doc.select("span.lenta_item_title");
                Elements text = doc.select("span.lenta_textsmall");
                Elements time = doc.select("div.date");
                Elements link = doc.select("div.news_item.lenta_item a:not(.date_rubric)");
                Elements nextPageLink = doc.select("a.p_next");

                titleList.clear();
                textList.clear();
                timeList.clear();
                linkList.clear();
                beltaNextPageLink.clear();

                for (Element titles : title){
                    titleList.add(titles.text());
                }

                for (Element texts : text){
                    if(texts.text() != null) {
                        textList.add(texts.text());
                    } else textList.add(" ");
                }

                for (Element times : time){
                    timeList.add(times.text());
                }

                for (Element links : link){
                    linkList.add(links.attr("href"));
                }

                for (Element nextPageLinks : nextPageLink){
                    beltaNextPageLink.add(nextPageLinks.attr("href"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            adapter.notifyDataSetChanged();
            for(int i = 0, j = 0, k = 0; i < titleList.size() && j < textList.size() && k < timeList.size(); i++, j++, k++){
                news.add(new News(timeList.get(k).substring(0, 6),titleList.get(i), textList.get(j)));
            }
        }
    }

    class ParseNextPageBeltaTask extends AsyncTask<Void, Void, Void>{
        Document doc;

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                doc = Jsoup.connect(beltaUrl + beltaNextPageLink.get(0)).get();

                Elements title = doc.select("span.lenta_item_title");
                Elements text = doc.select("span.lenta_textsmall");
                Elements time = doc.select("div.date");
                Elements link = doc.select("div.news_item.lenta_item a:not(.date_rubric)");
                Elements nextPageLink = doc.select("a.p_next");

                titleList.clear();
                textList.clear();
                timeList.clear();
                //linkList.clear();
                beltaNextPageLink.clear();

                for (Element titles : title){
                    titleList.add(titles.text());
                }

                for (Element texts : text){
                    if(texts.text() != null) {
                        textList.add(texts.text());
                    } else textList.add(" ");
                }

                for (Element times : time){
                    timeList.add(times.text());
                }

                for (Element links : link){
                    linkList.add(links.attr("href"));
                }

                for (Element nextPageLinks : nextPageLink){
                    beltaNextPageLink.add(nextPageLinks.attr("href"));
                }

            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            adapter.notifyDataSetChanged();
            for(int i = 0, j = 0, k = 0; i < titleList.size() && j < textList.size() && k < timeList.size(); i++, j++, k++){
                news.add(new News(timeList.get(k).substring(0, 6),titleList.get(i), textList.get(j)));
            }
        }
    }

    class ParseSputnikTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Document doc;
            try {
                doc = Jsoup.connect(sputnikAllNewsUrl).get();

                Elements title_link = doc.select("h2.b-plainlist__title a");
                Elements text = doc.select("div.b-plainlist__announce p");
                Elements time = doc.select("span.b-plainlist__date");
                Elements nextPageLink = doc.select("div.b-more a");

                titleList.clear();
                textList.clear();
                timeList.clear();
                linkList.clear();
                sputnikNextPageLink.clear();

                for (Element titles : title_link){
                    titleList.add(titles.text());
                }

                for (Element texts : text){
                    textList.add(texts.text());
                }

                for (Element times : time){
                    timeList.add(times.text());
                }

                for (Element links : title_link){
                    linkList.add(links.attr("href"));
                }

                for (Element nextPageLinks : nextPageLink){
                    sputnikNextPageLink.add(nextPageLinks.attr("href"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            adapter.notifyDataSetChanged();
            for(int i = 0, j = 0, k = 0; i < titleList.size() && j < textList.size() && k < timeList.size(); i++, j++, k++){
                news.add(new News(timeList.get(k).substring(0, 6),titleList.get(i), textList.get(j)));
            }
        }
    }

    class ParseNextPageSputnikTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Document doc;
            try {
                doc = Jsoup.connect(sputnikUrl + sputnikNextPageLink.get(0)).get();

                Elements title_link = doc.select("h2.b-plainlist__title a");
                Elements text = doc.select("div.b-plainlist__announce p");
                Elements time = doc.select("span.b-plainlist__date");
                Elements nextPageLink = doc.select("div.b-more a");

                titleList.clear();
                textList.clear();
                timeList.clear();
                //linkList.clear();
                sputnikNextPageLink.clear();

                for (Element titles : title_link){
                    titleList.add(titles.text());
                }

                for (Element texts : text){
                    textList.add(texts.text());
                }

                for (Element times : time){
                    timeList.add(times.text());
                }

                for (Element links : title_link){
                    linkList.add(links.attr("href"));
                }

                for (Element nextPageLinks : nextPageLink){
                    sputnikNextPageLink.add(nextPageLinks.attr("href"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            adapter.notifyDataSetChanged();
            for(int i = 0, j = 0, k = 0; i < titleList.size() && j < textList.size() && k < timeList.size(); i++, j++, k++){
                news.add(new News(timeList.get(k).substring(0, 6),titleList.get(i), textList.get(j)));
            }
        }
    }

    class ParseMlynTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Document doc;
            try {
                doc = Jsoup.connect(mlynAllNewsUrl).get();

                Elements title = doc.select("div.comment__body i");
                Elements text = doc.select("div.comment__body p");
                Elements time = doc.select("div.comment__body em");
                Elements link = doc.select("div.comment");
                Elements nextPageLink = doc.select("a.next");

                titleList.clear();
                textList.clear();
                timeList.clear();
                linkList.clear();
                mlynNextPageLink.clear();

                for (Element titles : title){
                    titleList.add(titles.text());
                }

                for (Element texts : text){
                    textList.add(texts.text());
                }

                for (Element times : time){
                    timeList.add(times.text());
                }

                for (Element links : link){
                    linkList.add(links.attr("onclick").substring(15, (links.attr("onclick").length() - 1)));
                }

                for (Element nextPageLinks : nextPageLink){
                    mlynNextPageLink.add(nextPageLinks.attr("href"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            adapter.notifyDataSetChanged();
            for(int i = 0, j = 0, k = 0; i < titleList.size() && j < textList.size() && k < timeList.size(); i++, j++, k++){
                news.add(new News(timeList.get(k).substring(0, 6),titleList.get(i), textList.get(j)));
            }
        }
    }

    class ParseNextPageMlynTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Document doc;
            try {
                doc = Jsoup.connect(mlynNextPageLink.get(0)).get();

                Elements title = doc.select("div.comment__body i");
                Elements text = doc.select("div.comment__body p");
                Elements time = doc.select("div.comment__body em");
                Elements link = doc.select("div.comment");
                Elements nextPageLink = doc.select("a.next");

                titleList.clear();
                textList.clear();
                timeList.clear();
                //linkList.clear();
                mlynNextPageLink.clear();

                for (Element titles : title){
                    titleList.add(titles.text());
                }

                for (Element texts : text){
                    textList.add(texts.text());
                }

                for (Element times : time){
                    timeList.add(times.text());
                }

                for (Element links : link){
                    linkList.add(links.attr("onclick").substring(15, (links.attr("onclick").length() - 1)));
                }

                for (Element nextPageLinks : nextPageLink){
                    mlynNextPageLink.add(nextPageLinks.attr("href"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            adapter.notifyDataSetChanged();
            for(int i = 0, j = 0, k = 0; i < titleList.size() && j < textList.size() && k < timeList.size(); i++, j++, k++){
                news.add(new News(timeList.get(k).substring(0, 6),titleList.get(i), textList.get(j)));
            }
        }
    }

    public void onParseBelta() {
        beltaAllNewsUrl = "https://www.belta.by/all_news/page/" + pageNumber;
        news.clear();
        adapter.notifyDataSetChanged();
        tvUrlInfo.setText(beltaUrl);
        ParseBeltaTask pbt = new ParseBeltaTask();
        pbt.execute();
    }

    public void onParseSputnik(){
        news.clear();
        adapter.notifyDataSetChanged();
        tvUrlInfo.setText(sputnikUrl);
        ParseSputnikTask pst = new ParseSputnikTask();
        pst.execute();
    }

    public void onParseMlyn(){
        mlynAllNewsUrl = "https://www.mlyn.by/novosti/page/" + pageNumber;
        tvUrlInfo.setText(mlynUrl);
        news.clear();
        adapter.notifyDataSetChanged();
        ParseMlynTask pmt = new ParseMlynTask();
        pmt.execute();
    }
}

