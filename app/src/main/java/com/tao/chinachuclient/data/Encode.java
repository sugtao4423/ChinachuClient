package com.tao.chinachuclient.data;

public class Encode{

    private String type;
    private String containerFormat;
    private String videoCodec;
    private String audioCodec;
    private String videoBitrate;
    private String audioBitrate;
    private String videoSize;
    private String frame;

    public Encode(String type, String containerFormat, String videoCodec, String audioCodec,
                  String videoBitrate, String audioBitrate, String videoSize, String frame){
        this.type = type;
        this.containerFormat = containerFormat;
        this.videoCodec = videoCodec;
        this.audioCodec = audioCodec;
        this.videoBitrate = videoBitrate;
        this.audioBitrate = audioBitrate;
        this.videoSize = videoSize;
        this.frame = frame;
    }

    public String getType(){
        return type;
    }

    public String getContainerFormat(){
        return containerFormat;
    }

    public String getVideoCodec(){
        return videoCodec;
    }

    public String getAudioCodec(){
        return audioCodec;
    }

    public String getVideoBitrate(){
        return videoBitrate;
    }

    public String getAudioBitrate(){
        return audioBitrate;
    }

    public String getVideoSize(){
        return videoSize;
    }

    public String getFrame(){
        return frame;
    }
}
