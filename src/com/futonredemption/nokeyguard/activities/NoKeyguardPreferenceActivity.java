package com.futonredemption.nokeyguard.activities;

import org.beryl.app.AndroidVersion;

import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.R;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class NoKeyguardPreferenceActivity extends PreferenceActivity {

	private boolean isChangingConfiguration = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showUpAffordanceOnActionBar();
		addPreferencesFromResource(R.xml.pref_main);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		isChangingConfiguration = true;
		return super.onRetainNonConfigurationInstance();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(! isChangingConfiguration) {
			this.startService(Intents.refreshWidgets(this));
			finish();
		}
	}
	
	protected void showUpAffordanceOnActionBar() {
		if(AndroidVersion.isHoneycombOrHigher()) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home: {
			goHome();
		} break;
		
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}
	
	protected void goHome() {
		Intent intent = new Intent(this, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
	}
}
