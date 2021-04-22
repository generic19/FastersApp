package com.basilalasadi.fasters.state;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import org.threeten.bp.ZonedDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.basilalasadi.fasters.bloc.CountdownBloc;
import com.basilalasadi.fasters.bloc.CountdownBloc.LoadTimingsEvent;
import com.basilalasadi.fasters.bloc.CountdownBloc.StateStreamConsumer;
import com.basilalasadi.fasters.controller.MainActivityController;
import com.basilalasadi.fasters.model.CountdownViewModel;
import com.basilalasadi.fasters.provider.TimeProvider;
import com.basilalasadi.fasters.view.MainActivity;


public class MainActivityState extends ActivityState<MainActivity> implements StateStreamConsumer {
	private static CountdownBloc bloc;
	
	private MainActivityController controller = null;
	private final Timer timer = new Timer();
	private TickTask tickTask;
	private CountdownViewModel viewModel;
	
	
	public MainActivityState(Class<MainActivity> _class) {
		super(_class);
	}
	
	public void bindController(MainActivityController controller) {
		this.controller = controller;
	}
	
	@Override
	protected void onCreateActivity(Bundle savedInstanceState) {
		Log.d("MainActivityState", "activity creation.");
		
		if (bloc == null) {
			bloc = new CountdownBloc(this);
		}
	}
	
	@Override
	protected void onDestroyActivity() {
		Log.d("MainActivityState", "activity destruction.");
	}
	
	@Override
	protected void onStartActivity() {
		Log.d("MainActivityState", "activity start.");
		
		if (viewModel == null || !viewModel.isDataAvailable()) {
			sendLoadTimingsEvent();
		}
		else {
			sendUpdateView();
			startTicker();
		}
	}
	
	@Override
	protected void onStopActivity() {
		Log.d("MainActivityState", "activity stop.");
		stopTicker();
	}
	
	@Override
	protected void onResumeActivity() {
		Log.d("MainActivityState", "activity resume.");
	}
	
	@Override
	protected void onPauseActivity() {
		Log.d("MainActivityState", "activity pause.");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d("MainActivityState", "activity save instance.");
	}
	
	void sendLoadTimingsEvent() {
		bloc.addEvent(new LoadTimingsEvent(controller.getCurrentContext()));
	}
	
	@Override
	public void onState(CountdownViewModel viewModel) {
		Log.d("MainActivityState", "new state " + viewModel.toString());
		
		this.viewModel = viewModel;
		sendUpdateView();
		
		if (viewModel.isDataAvailable()) {
			startTicker();
		}
		else {
			stopTicker();
		}
	}
	
	public CountdownViewModel getViewModel() {
		return viewModel;
	}
	
	private void startTicker() {
		long nextExecEpochSeconds = ZonedDateTime.now().plusSeconds(1).withNano(0).toEpochSecond();
		Date nextExec = new Date(nextExecEpochSeconds * 1000);
		
		if (tickTask != null) {
			tickTask.cancel();
		}
		
		tickTask = new TickTask(this);
		timer.scheduleAtFixedRate(tickTask, nextExec, 1000);
	}
	
	private void stopTicker() {
		if (tickTask != null) {
			tickTask.cancel();
		}
	}
	
	private void sendUpdateView() {
		Message msg = new Message();
		msg.what = MainActivityController.MSG_UPDATE_VIEW;
		controller.getHandler().sendMessage(msg);
	}
	
	private void sendUpdateCountdown() {
		Message msg = new Message();
		msg.what = MainActivityController.MSG_UPDATE_COUNTDOWN;
		controller.getHandler().sendMessage(msg);
	}
	
	
	private static class TickTask extends TimerTask {
		private final MainActivityState state;
		
		TickTask(MainActivityState state) {
			this.state = state;
		}
		
		@Override
		public void run() {
			try {
				if (state.getViewModel().isExpired()) {
					state.sendLoadTimingsEvent();
				}
				else {
					state.sendUpdateCountdown();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
