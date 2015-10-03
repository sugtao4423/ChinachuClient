package com.tao.chinachuclient;

import Chinachu4j.Chinachu4j;
import android.app.Application;

public class ApplicationClass extends Application{

	private Chinachu4j chinachu;
	private boolean streaming, encStreaming;
	private Object tmp;

	public void setChinachu(Chinachu4j chinachu){
		this.chinachu = chinachu;
	}

	public Chinachu4j getChinachu(){
		return chinachu;
	}

	public void setStreaming(boolean streaming){
		this.streaming = streaming;
	}

	public boolean getStreaming(){
		return streaming;
	}

	public void setEncStreaming(boolean encStreaming){
		this.encStreaming = encStreaming;
	}

	public boolean getEncStreaming(){
		return encStreaming;
	}

	public void setTmp(Object tmp){
		this.tmp = tmp;
	}

	public Object getTmp(){
		return tmp;
	}
}
