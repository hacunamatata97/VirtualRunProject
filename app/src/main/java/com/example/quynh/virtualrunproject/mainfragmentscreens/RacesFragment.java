package com.example.quynh.virtualrunproject.mainfragmentscreens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.example.quynh.virtualrunproject.R;
import com.example.quynh.virtualrunproject.customGUI.RacesAdapter;
import com.example.quynh.virtualrunproject.custominterface.OnButtonClickRecyclerViewAdapter;
import com.example.quynh.virtualrunproject.custominterface.OnReceiveResponse;
import com.example.quynh.virtualrunproject.dao.RacesListDAO;
import com.example.quynh.virtualrunproject.entity.Race;
import com.example.quynh.virtualrunproject.functionscreen.adminscreens.EndedRacesScreen;
import com.example.quynh.virtualrunproject.functionscreen.race.RaceDetailScreen;
import com.example.quynh.virtualrunproject.functionscreen.useraccountandprofile.GetVerifyCodeScreen;
import com.example.quynh.virtualrunproject.helper.RemoveAccentHandler;
import com.example.quynh.virtualrunproject.services.RaceServices;
import com.google.gson.Gson;
import com.thekhaeng.pushdownanim.PushDownAnim;

import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by quynh on 12/26/2018.
 */

public class RacesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, TextView.OnEditorActionListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.races_fragment_layout, container, false);
        return view;
    }

    private EditText fromDistance, toDistance;
    private Button filterBtn;
    private RecyclerView recyclerView;
    private List<Race> races;
    private RacesAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText nameSearched;
    private ImageView imgSearch;
    private LinearLayout noData;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView(view);
        setupAction();
        getOngoingRaces();
    }

    private void setupView(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        fromDistance = (EditText) view.findViewById(R.id.distance_from);
        toDistance = (EditText) view.findViewById(R.id.distance_to);
        filterBtn = (Button) view.findViewById(R.id.filter_btn);
        nameSearched = (EditText) view.findViewById(R.id.name_searched);
        imgSearch = (ImageView) view.findViewById(R.id.img_search);
        noData = (LinearLayout) view.findViewById(R.id.no_data);

        recyclerView = (RecyclerView) view.findViewById(R.id.racesList);
        settingAdapter();
        PushDownAnim.setPushDownAnimTo(filterBtn);
    }

    private void setupAction() {
        swipeRefreshLayout.setOnRefreshListener(this);
        nameSearched.setOnEditorActionListener(this);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //searchRaces(RemoveAccentHandler.removeAccent(nameSearched.getText().toString()));
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                Race race1 = new Race();
                race1.setName((nameSearched.getText().toString()));
                searchRaces(race1);
            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Close android keyboard
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                if (fromDistance.getText().toString().equalsIgnoreCase("")) {
                    fromDistance.setError("Thông tin bắt buộc");
                } else if (toDistance.getText().toString().equalsIgnoreCase("")) {
                    toDistance.setError("Thông tin bắt buộc");
                } else if (Integer.valueOf(toDistance.getText().toString()) < Integer.valueOf(fromDistance.getText().toString())) {
                    Toast.makeText(getActivity(), "Khoảng cách cần nhập hợp lý", Toast.LENGTH_LONG).show();
                } else {
                    double from = Double.valueOf(fromDistance.getText().toString());
                    double to = Double.valueOf(toDistance.getText().toString());
                    getRacesWithDistanceRange(from, to);
                }
            }
        });
    }

    private void searchRaces(Race name) {
        if (!name.getName().equalsIgnoreCase("")) {
            RaceServices.searchRacesWithName(name, getActivity(), new OnReceiveResponse() {
                @Override
                public void onReceive(JSONObject response) {
                    Gson gson = new Gson();
                    races.clear();
                    RacesListDAO racesListDAO = gson.fromJson(response.toString(), RacesListDAO.class);
                    if (!racesListDAO.getRaces().isEmpty()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        noData.setVisibility(View.GONE);
                        for (Race race : racesListDAO.getRaces()) {
                            races.add(race);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        noData.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void getRacesWithDistanceRange(double from, double to) {
        RaceServices.getRacesWithDistanceRange(from, to, getActivity(), new OnReceiveResponse() {
            @Override
            public void onReceive(JSONObject response) {
                Gson gson = new Gson();
                Log.d("RacesFragment", "onResponse: " + response);
                races.clear();
                RacesListDAO racesListDAO = gson.fromJson(response.toString(), RacesListDAO.class);
                if (!racesListDAO.getRaces().isEmpty()) {
                    recyclerView.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);
                    for (Race race : racesListDAO.getRaces()) {
                        races.add(race);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    recyclerView.setVisibility(View.GONE);
                    noData.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void settingAdapter() {
        races = new ArrayList<>();
        adapter = new RacesAdapter(races, getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        adapter.setOnButtonClickRecyclerViewAdapter(new OnButtonClickRecyclerViewAdapter() {
            @Override
            public void OnButtonClick(int position) {
                Intent intent = new Intent(getActivity(), RaceDetailScreen.class);
                Gson gson = new Gson();
                intent.putExtra("raceString", gson.toJson(races.get(position)));
                getActivity().startActivityForResult(intent, 2);
            }
        });
    }

    private void getOngoingRaces() {
        RaceServices.getOngoingRaces(getActivity(), new OnReceiveResponse() {
            @Override
            public void onReceive(JSONObject response) {
                Gson gson = new Gson();
                Log.d("RacesFragment", "onResponse: " + response);
                RacesListDAO racesListDAO = gson.fromJson(response.toString(), RacesListDAO.class);
                if (!racesListDAO.getRaces().isEmpty()) {
                    recyclerView.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);
                    for (Race race : racesListDAO.getRaces()) {
                        races.add(race);
                    }
                    adapter.notifyDataSetChanged();
                }else {
                    recyclerView.setVisibility(View.GONE);
                    noData.setVisibility(View.VISIBLE);
                }
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        settingAdapter();
        getOngoingRaces();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            Race race1 = new Race();
            race1.setName((nameSearched.getText().toString()));
            searchRaces(race1);
            return true;
        }
        return false;
    }
}
