package com.tao.chinachuclient.data;

public class Server{

	private String chinachuAddress;
	private String username;
	private String password;
	private boolean streaming;
	private boolean encStreaming;
	private Encode encode;
	private String channelIds;
	private String channelNames;

	public Server(String chinachuAddress, String username, String password, boolean streaming, boolean encStreaming,
			Encode encode, String channelIds, String channelNames){
		this.chinachuAddress = chinachuAddress;
		this.username = username;
		this.password = password;
		this.streaming = streaming;
		this.encStreaming = encStreaming;
		this.encode = encode;
		this.channelIds = channelIds;
		this.channelNames = channelNames;
	}

	public String getChinachuAddress(){
		return chinachuAddress;
	}

	public String getUsername(){
		return username;
	}

	public String getPassword(){
		return password;
	}

	public boolean getStreaming(){
		return streaming;
	}

	public boolean getEncStreaming(){
		return encStreaming;
	}

	public Encode getEncode(){
		return encode;
	}

	public String getChannelIds(){
		return channelIds;
	}

	public String getChannelNames(){
		return channelNames;
	}
}
