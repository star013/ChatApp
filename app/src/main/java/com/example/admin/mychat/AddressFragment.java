package com.example.admin.mychat;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 通讯录部分
 */
public class AddressFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_address, container, false);
    }

}
