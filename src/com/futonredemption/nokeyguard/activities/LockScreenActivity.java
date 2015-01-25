package com.futonredemption.nokeyguard.activities;

import org.beryl.intents.IntentHelper;

import com.futonredemption.nokeyguard.Constants;
import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.Preferences;
import com.futonredemption.nokeyguard.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LockScreenActivity extends Activity {

	public ImageView BackgroundGlowImageView;
	public ImageButton ToggleLockButton;
	public TextView StatusTextView;
	public Button PreferencesButton;
	public TextView WarningTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setWindowMode();
		getViews();
	}

	private void setWindowMode() {
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	private void getViews() {
		BackgroundGlowImageView = (ImageView)findViewById(R.id.BackgroundGlowImageView);
		ToggleLockButton = (ImageButton)findViewById(R.id.ToggleLockButton);
		StatusTextView = (TextView)findViewById(R.id.StatusTextView);
		ToggleLockButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toggleLockScreenState();
			}
		}
		);
		
		PreferencesButton = (Button)findViewById(R.id.PreferencesButton);
		PreferencesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showPreferencesActivity();
			}
		}
		);
		
		WarningTextView = (TextView)findViewById(R.id.WarningTextView);
	}

	public final LockStatusReceiver LockState = new LockStatusReceiver();
	
	public class LockStatusReceiver extends BroadcastReceiver {

		public int Mode = Constants.MODE_Enabled;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Mode = intent.getIntExtra("mode", Constants.MODE_Enabled);
			final boolean isLockActive = intent.getBooleanExtra("isActive", true);
			
			StringBuilder sb = new StringBuilder();
			
			if(isLockActive) {
				BackgroundGlowImageView.setImageResource(R.drawable.bg_glow_white);
				ToggleLockButton.setImageResource(R.drawable.active_lock);
				sb.append("Lock Screen is Active");
			} else {
				BackgroundGlowImageView.setImageResource(R.drawable.bg_glow_blue);
				ToggleLockButton.setImageResource(R.drawable.inactive_lock);
				sb.append("Lock Screen is Not Active");
			}
			if(Mode == Constants.MODE_ConditionalToggle) {
				sb.append(", conditionally toggled.");
			}
			
			StatusTextView.setText(sb.toString());
		}
		
	}

	@Override
	public void onResume() {
		super.onResume();
		
		toggleShowWarning();

		registerReceiver(LockState, Intents.broadcastLockStateIntentFilter());
		startService(Intents.getStatus(this));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(LockState);
	}
	
	public void toggleLockScreenState() {
		if(LockState.Mode == Constants.MODE_Enabled) {
			startService(Intents.disableKeyguard(LockScreenActivity.this));
		} else {
			startService(Intents.enableKeyguard(LockScreenActivity.this));
		}
	}
	
	public void showPreferencesActivity() {
		runActivity(Intents.showPreferencesActivity(LockScreenActivity.this));
	}
	
	public void showSecuritySettingsActivity() {
		runActivity(Intents.securitySettings());
	}
	
	private void runActivity(Intent intent) {
		if(IntentHelper.canHandleIntent(this, intent)) {
			startActivity(intent);
		} else {
			Toast.makeText(this, R.string.option_not_supported, Toast.LENGTH_LONG).show();
		}
	}
	
	private void toggleShowWarning() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean hideNotification = preferences.getBoolean(Preferences.General.HideNotification, false);
		if(hideNotification) {
			WarningTextView.setVisibility(View.VISIBLE);
		} else  {
			WarningTextView.setVisibility(View.GONE);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.MenuItemPreferences: {
			showPreferencesActivity();
		} break;
		case R.id.MenuItemSecuritySettings: {
			showSecuritySettingsActivity();
		} break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}
}
