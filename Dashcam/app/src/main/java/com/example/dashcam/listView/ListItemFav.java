package com.example.dashcam.listView;

public class ListItemFav {
    private String videoTitle;
    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public ListItemFav (String videoTitle, String tableName){
        this.tableName = tableName;
        this.videoTitle = videoTitle;
    }
}
