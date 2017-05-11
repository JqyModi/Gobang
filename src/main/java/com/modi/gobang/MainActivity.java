package com.modi.gobang;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.squareup.leakcanary.RefWatcher;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//MyAPP.refWatcher.watch(this);
		RefWatcher refWatcher = MyAPP.getRefWatcher(this);
		refWatcher.watch(this);
	}
}
