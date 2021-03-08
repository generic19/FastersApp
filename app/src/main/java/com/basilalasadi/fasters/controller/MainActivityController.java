package com.basilalasadi.fasters.controller;

import com.basilalasadi.fasters.model.CountdownViewModel;


public interface MainActivityController {
	void setViewModel(CountdownViewModel viewModel);
	void updateViewData();
	void updateCountdown();
}
