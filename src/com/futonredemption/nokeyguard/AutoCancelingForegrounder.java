package com.futonredemption.nokeyguard;

import java.util.Timer;
import java.util.TimerTask;

import org.beryl.app.ServiceForegrounder;
import org.beryl.diagnostics.Logger;

import android.app.PendingIntent;
import android.app.Service;

public class AutoCancelingForegrounder {

	private final Service service;
	private final ServiceForegrounder foregrounder;
	private Timer pendingStopForegroundTimer = null;
	
	private TimerTask pendingStopForeground = null;
	
	public AutoCancelingForegrounder(Service service) {
		this.service = service;
		this.foregrounder = new ServiceForegrounder(this.service, Constants.NOTIFICATION_ForegroundService);
	}
	
	public boolean isForegrounded() {
		boolean isForegrounded = false;
		
		synchronized(foregrounder) {
			isForegrounded = foregrounder.isForegrounded();
		}
		
		return isForegrounded;
	}
	
	public void startForeground(int resIconId, int title, int description, int tickerText, PendingIntent onClickIntent) {
		synchronized (foregrounder) {
			Logger.e("START foregrounding");
			foregrounder.startForeground(resIconId, title, description, tickerText, onClickIntent);
		}
	}
	
	public void stopForeground() {
		synchronized(foregrounder) {
			Logger.e("stop foregrounding");
			foregrounder.stopForeground();
		}
	}
	
	public void beginChangeAction(final int resIconId, final int title, final int description, final int tickerText, final PendingIntent onClickIntent) {
		queueTask(new TimerTask() {		
				@Override
				public void run() {
					synchronized(foregrounder) {
						foregrounder.startForeground(resIconId, title, description, tickerText, onClickIntent);
						cancelCurrentTask();
					}
				}
		});
	}
	
	private void queueTask(TimerTask task) {
		synchronized(foregrounder) {
			cancelCurrentTask();
			pendingStopForegroundTimer = new Timer();
			pendingStopForeground = task;
			pendingStopForegroundTimer.schedule(pendingStopForeground, Constants.INTERVAL_ForegroundTimeout);
		}
	}

	public void beginRemoveForeground() {
		queueTask(new TimerTask() {		
			@Override
			public void run() {
				synchronized(foregrounder) {
					foregrounder.stopForeground();
					cancelCurrentTask();
				}
			}
		});
	}
	
	public void cancelCurrentTask() {
		synchronized(foregrounder) {
			if(pendingStopForegroundTimer != null) {
				try {
					pendingStopForegroundTimer.cancel();
					pendingStopForegroundTimer.purge();
					
					if(pendingStopForeground != null) {
						pendingStopForeground = null;
					}
				} catch(Exception e) {
				} finally {
					pendingStopForegroundTimer = null;
				}
			}
		}
	}
}
