package com.basilalasadi.fasters.controller;

import android.content.Context;
import android.os.Handler;


public interface MainActivityController {
	int MSG_UPDATE_VIEW = 1;
	int MSG_UPDATE_COUNTDOWN = 2;
	
	Context getCurrentContext();
	Handler getHandler();
}
