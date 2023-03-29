package com.example.eventappproject.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventappproject.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class PickLocationFragment extends SupportMapFragment implements OnMapReadyCallback {

    Context parent;

    public PickLocationFragment(Context parent) { this.parent = parent; }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        System.out.println(" I AM HERE");
    }
}