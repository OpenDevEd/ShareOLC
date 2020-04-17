package com.android.shareolc.model;

public class SatelliteModel {
    public SatelliteModel(int totalSatellites, int useInSatellites) {
        this.totalSatellites = totalSatellites;
        this.useInSatellites = useInSatellites;
    }

    public int getTotalSatellites() {
        return totalSatellites;
    }

    public void setTotalSatellites(int totalSatellites) {
        this.totalSatellites = totalSatellites;
    }

    public int getUseInSatellites() {
        return useInSatellites;
    }

    public void setUseInSatellites(int useInSatellites) {
        this.useInSatellites = useInSatellites;
    }

    private int totalSatellites = 0;
    private int useInSatellites = 0;
}
