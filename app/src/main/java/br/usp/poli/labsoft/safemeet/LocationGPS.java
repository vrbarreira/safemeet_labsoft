package br.usp.poli.labsoft.safemeet;

public class LocationGPS {

    private float latitude, longitude;

    public LocationGPS(float latitude, float longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "Latitude: " + this.latitude + "\nLongitude: " + this.longitude;
    }
}
