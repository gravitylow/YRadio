package net.gravitydevelopment.yradio;

public class StationRunnable extends Thread {

    private Station station;

    public StationRunnable(Station station) {
        this.station = station;
    }

    @Override
    public void run() {
        station.parse();
        station.play();
        while(true) {

        }
    }
}
