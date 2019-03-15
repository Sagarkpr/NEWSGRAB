package com.grabnews.app;

import android.app.SearchManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.grabnews.app.Adapter.NewsAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.grabnews.app.DatabaseModel.NewsSchema;
import com.grabnews.app.Interface.API;
import com.grabnews.app.News.News;
import com.grabnews.app.News.NewsData;
import com.grabnews.app.News.Source;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,NewsAdapter.OnItemClickListener {

    private String TAG = "GRABNEWS";
    private NewsAdapter adapter;
    private List<NewsData> articles = new ArrayList<>();
    public static final String API_KEY = "0d0d9cbef4954e1fa599d5e198b7bbce";
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView topHeadline;
    private RecyclerView recyclerView;
    private RelativeLayout errorLayout;
    private ImageView errorImage;
    private TextView errorTitle, errorMessage;
    private Button btnRetry;
    private RecyclerView.LayoutManager layoutManager;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        topHeadline = findViewById(R.id.topheadlines);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        realm = Realm.getDefaultInstance();
        onLoadingSwipeRefresh("");

        errorLayout = findViewById(R.id.errorLayout);
        errorImage = findViewById(R.id.errorImage);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        btnRetry = findViewById(R.id.btnRetry);
    }

    public void LoadJson(final String keyword) {
        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);

        API apiInterface = ApiResponse.getApiClient().create(API.class);

        String country = Utils.getCountry();
        String language = Utils.getLanguage();

        Call<News> call;

        if(keyword.length() > 0) {
            call = apiInterface.getNewsSearch(keyword, language, "publishedAt", API_KEY);
        } else {
            call = apiInterface.getNews(country, API_KEY);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful() && response.body().getArticle() != null) {

                    if(!articles.isEmpty()) {
                        articles.clear();
                    }

                    articles=response.body().getArticle();
                    writeDB();
                    initAdapter();

                } else {
                    if (!realm.isEmpty() && !realm.isClosed()) {
                        readDB();
                        return;
                    }

                    topHeadline.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode = "unknown error";
                            break;
                    }

                    showErrorMessage(R.drawable.no_result,"No Result", "Please Try Again\n" + errorCode);
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {

                Log.d(TAG,"Network failure " + realm.isEmpty() + realm.isClosed());
                if (!realm.isEmpty() && !realm.isClosed()) {

                    Log.d(TAG,"Reading From DB");
                    readDB();
                    return;
                }

                topHeadline.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                showErrorMessage(
                        R.drawable.oops,
                        "Oops..",
                        "Network failure, Please Try Again\n"+
                                t.toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public void initAdapter() {
        adapter = new NewsAdapter(articles,MainActivity.this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        initListener();
        topHeadline.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    public void readDB() {
            if (!articles.isEmpty()) {
                articles.clear();
            }
            RealmResults<NewsSchema> result1 =
                    realm.where(NewsSchema.class).findAll();

            for (NewsSchema data : result1) {
                Source src=new Source();
                src.setId(data.getId());
                src.setName(data.getName());

                NewsData data1 = new NewsData();
                data1.setUrl(data.getUrl());
                data1.setAuthor(data.getAuthor());
                data1.setSource(src);
                data1.setImageUrl(data.getImageUrl());
                data1.setDescription(data.getDescription());
                data1.setContent(data.getContent());
                data1.setTitle(data.getTitle());
                data1.setPbDate(data.getPbDate());
                articles.add(data1);
            }
            initAdapter();
    }

    public void writeDB() {
        if(articles.isEmpty()) {
            return ;
        }

        if(articles.isEmpty()) {
            return;
        }

        if(!realm.isEmpty()) {
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
        }

        realm.beginTransaction();
        for(NewsData nsdata : articles) {
            NewsSchema data = realm.createObject(NewsSchema.class);

            data.setId(nsdata.getSource().getId());
            data.setName(nsdata.getSource().getId());
            data.setUrl(nsdata.getUrl());
            data.setAuthor(nsdata.getAuthor());
            data.setImageUrl(nsdata.getImageUrl());
            data.setDescription(nsdata.getDescription());
            data.setContent(nsdata.getContent());
            data.setTitle(nsdata.getTitle());
            data.setPbDate(nsdata.getPbDate());
            realm.insert(data);
        }
        realm.commitTransaction();

        if(!realm.isEmpty()) {
            Log.d(TAG,"not empty");
        }
        else {
            Log.d(TAG,"empty");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Latest News...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2){
                    onLoadingSwipeRefresh(query);
                }
                else {
                    Toast.makeText(MainActivity.this, "Type more than two letters!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false, false);

        return true;
    }

    private void showErrorMessage(int imageView, String title, String message) {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
        }

        errorImage.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadingSwipeRefresh("");
            }
        });
    }

    public void onItemClick(View view, int position) {
        ImageView imageView = view.findViewById(R.id.img);

        Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class) ;
        NewsData article = articles.get(position);
        intent.putExtra("url", article.getUrl());
        intent.putExtra("title", article.getTitle());
        intent.putExtra("img",  article.getImageUrl());
        intent.putExtra("date",  article.getPbDate());
        intent.putExtra("source",  article.getSource().getName());
        intent.putExtra("author",  article.getAuthor());

        Pair<View, String> pair = Pair.create((View)imageView, ViewCompat.getTransitionName(imageView));
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                MainActivity.this,
                pair
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(intent, optionsCompat.toBundle());
        }else {
            startActivity(intent);
        }
    }

    private void initListener() {
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onRefresh() {
        LoadJson("");
    }

    private void onLoadingSwipeRefresh(final String keyword){
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        LoadJson(keyword);
                    }
                }
        );

    }

}