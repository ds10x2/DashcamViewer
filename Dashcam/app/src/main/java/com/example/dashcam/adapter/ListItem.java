package com.example.dashcam.adapter;

public class ListItem {
    private String date;
    private String addressDepart;
    private String addressArrive;
    private String timeDepart;
    private String timeArrive;
    //private String distance;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddressDepart() {
        return addressDepart;
    }

    public void setAddressDepart(String addressDepart) {
        this.addressDepart = addressDepart;
    }

    public String getAddressArrive() {
        return addressArrive;
    }

    public void setAddressArrive(String addressArrive) {
        this.addressArrive = addressArrive;
    }

    public String getTimeDepart() {
        return timeDepart;
    }

    public void setTimeDepart(String timeDepart) {
        this.timeDepart = timeDepart;
    }

    public String getTimeArrive() {
        return timeArrive;
    }

    public void setTimeArrive(String timeArrive) {
        this.timeArrive = timeArrive;
    }



    public ListItem(String date, String addressDepart, String addressArrive, String timeDepart, String timeArrive){
        this.date = date;
        this.addressDepart = addressDepart;
        this.addressArrive = addressArrive;
        this.timeArrive = timeArrive;
        this.timeDepart = timeDepart;
    }
}
