package com.thuMediaLab.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

public class DumbActivity extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// 修改返回的View来修改tab显示的tab
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_dumb, null);
		return view;
	}

}
