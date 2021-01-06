package com.kwanwaipang.rosandroid.ui.fragments.main;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.kwanwaipang.rosandroid.R;
import com.kwanwaipang.rosandroid.utility.Utils;
import com.kwanwaipang.rosandroid.viewmodel.MainViewModel;
import com.liphy.navigation.network.LiphyCloudManager;
import com.parse.ParseException;

import java.util.ArrayList;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.1
 * @created on 10.01.2020
 * @updated on 27.07.2020
 * @modified by Nils Rottmann
 * @updated on 05.11.2020
 * @modified by Nico Studt
 */
public class MainFragment extends Fragment implements OnBackPressedListener {
//此处应该是主界面,

    public static final String TAG = MainFragment.class.getSimpleName();

    ConfigTabsPagerAdapter pagerAdapter;//可以通过此处新增fragment
    LockableViewPager viewPager;
    TabLayout tabLayout;

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    MainViewModel mViewModel;


    public static MainFragment newInstance() {
        return new MainFragment();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager = view.findViewById(R.id.configViewpager);
        tabLayout = view.findViewById(R.id.tabs);
        toolbar = view.findViewById(R.id.toolbar);
        drawerLayout = view.findViewById(R.id.drawer_layout);

        drawerLayout.setScrimColor(getResources().getColor(R.color.drawerFadeColor));


        // load liphy cloud data
        LiphyCloudManager manager = LiphyCloudManager.getInstance();
        ArrayList<String> filters = new ArrayList<>();
        try {
            manager.fetchLightBeaconsFromCloud();
            manager.fetchBuildingsFromCloud(filters);
        } catch (ParseException e) {
            e.printStackTrace();

        }





        // Connect toolbar to application
        if(getActivity() instanceof AppCompatActivity){

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);

            // Setup home indicator to open drawer layout
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);

            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // Setup tabs for navigation
        pagerAdapter = new ConfigTabsPagerAdapter(this.getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1,false);

        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {
                Utils.hideSoftKeyboard(view);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        if(this.getArguments() != null) {
            mViewModel.createFirstConfig(this.getArguments().getString("configName"));
        }

        mViewModel.getConfigTitle().observe(getViewLifecycleOwner(), this::setTitle);
    }

    private void setTitle(String newTitle) {
        if (newTitle.equals(toolbar.getTitle().toString())) {
            return;
        }

        toolbar.setTitle(newTitle);
    }

    public boolean onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (viewPager.getCurrentItem() != 1) {
            viewPager.setCurrentItem(1,true);
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home){
                drawerLayout.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
