package com.example.quynh.virtualrunproject.functionscreen.adminscreens;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.quynh.virtualrunproject.R;
import com.example.quynh.virtualrunproject.customGUI.AdminRacesAdapter;
import com.example.quynh.virtualrunproject.custominterface.OnButtonClickRecyclerViewAdapter;
import com.example.quynh.virtualrunproject.dao.RacesListDAO;
import com.example.quynh.virtualrunproject.entity.Race;
import com.example.quynh.virtualrunproject.functionscreen.hosting.RaceResultScreen;
import com.example.quynh.virtualrunproject.functionscreen.race.RaceDetailScreen;
import com.google.gson.Gson;

import java.util.Iterator;
import java.util.List;

public class AllRaceScreen extends AppCompatActivity implements TextView.OnEditorActionListener,
        SwipeRefreshLayout.OnRefreshListener{

    private ImageView backBtn;
    private RecyclerView recyclerView;
    private List<Race> races;
    private AdminRacesAdapter adapter;
    private TextView nameSearched;
    private ImageView imgSearch;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_race_screen);

        setupView();
        setupAction();
        setupRaceList();
    }

    private void search(String name){
        Iterator<Race> iter = races.iterator();

        while (iter.hasNext()) {
            Race race = iter.next();

            if (!race.getName().toLowerCase().contains(name.toLowerCase())){
                iter.remove();
                adapter.notifyDataSetChanged();
            }
        }

        if(!races.isEmpty()){
            recyclerView.setVisibility(View.VISIBLE);
            noData.setVisibility(View.GONE);
        }else{
            recyclerView.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
        }
    }

    private void setupRaceList() {
        Intent intent = getIntent();
        final Gson gson = new Gson();
        RacesListDAO dao = gson.fromJson(intent.getStringExtra("races"), RacesListDAO.class);
        races = dao.getRaces();
        adapter = new AdminRacesAdapter(races, this);
        adapter.setOnButtonClickRecyclerViewAdapter(new OnButtonClickRecyclerViewAdapter() {
            @Override
            public void OnButtonClick(int position) {
                Intent intent1 = new Intent(AllRaceScreen.this, RaceDetailScreen.class);
                intent1.putExtra("raceString", gson.toJson(races.get(position)));
                startActivity(intent1);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(AllRaceScreen.this));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        if(!races.isEmpty()){
            recyclerView.setVisibility(View.VISIBLE);
            noData.setVisibility(View.GONE);
        }else{
            recyclerView.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
        }

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setupAction() {
        swipeRefreshLayout.setOnRefreshListener(this);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager) AllRaceScreen.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(AllRaceScreen.this.getCurrentFocus().getWindowToken(), 0);
                search(nameSearched.getText().toString());
            }
        });
        nameSearched.setOnEditorActionListener(this);
    }

    private void setupView() {
        TextView title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("Đường Chạy");
        backBtn = (ImageView) findViewById(R.id.back_btn);
        backBtn.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        nameSearched = (TextView) findViewById(R.id.name_searched);
        imgSearch = (ImageView) findViewById(R.id.img_search);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        noData = (LinearLayout) findViewById(R.id.no_data);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            search(nameSearched.getText().toString());
            return true;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        setupRaceList();
    }
}
