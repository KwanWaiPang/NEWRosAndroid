package com.kwanwaipang.rosandroid.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.kwanwaipang.rosandroid.ui.fragments.Utils;
import com.kwanwaipang.rosandroid.R;

/**
 * Simple AboutFragment showing about info for the app.
 *
 * Created by Kenneth Spear on 3/15/16.
 */
public class AboutFragment extends Fragment {

    /**
     * Default Constructor.
     */
    public AboutFragment() {}

    /**
     * Called when the activity is created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_about, null);

        WebView webView = (WebView) view.findViewById(R.id.abouttxt);
        webView.loadData(Utils.readText(getActivity(), R.raw.about), "text/html", null);

        return view;
    }
}

