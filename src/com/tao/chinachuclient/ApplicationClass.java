package com.tao.chinachuclient;

import Chinachu4j.Chinachu4j;
import android.app.Application;

public class ApplicationClass extends Application {
	
	private Chinachu4j chinachu;
	
	public void setChinachu(Chinachu4j chinachu){
		this.chinachu = chinachu;
	}
	public Chinachu4j getChinachu(){
		return chinachu;
	}
}
