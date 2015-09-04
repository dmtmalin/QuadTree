package modelsControllers;

/**
 * Created by dmt on 31.08.2015.
 */
public class Spherical extends Coordinate2D {
    public Spherical(double lat, double lon) {
        super(lat, lon);
    }
    public double getLat() {
        return super.getCoordinate1();
    }
    public double getLon() {
        return super.getCoordinate2();
    }
    public Cartesian toCartesian() {
        //equatorial radius
        double a = 6378136.6;
        //polar radius
        double c = 6356751.9;

        double lat1 = 0;
        double lon1 = 0;

        double lat2 = super.getCoordinate1();
        double lon2 = super.getCoordinate2();

        double phi = (lat1 + lat2) / 2;
        phi = Math.PI / 2 - (phi * Math.PI / 180);
        double lon_diff = lon2 - lon1;
        double lat_diff = lat2 - lat1;

        double temp = Math.pow((Math.sin(phi) / a), 2) + Math.pow((Math.cos(phi) / c), 2);
        double r = Math.pow((1/temp), 0.5);

        double lon_r = Math.cos(phi) * r;
        double x = lon_r * lon_diff * Math.PI / 180;
        double y = r * lat_diff * Math.PI / 180;
        return new Cartesian(x, y);
    }

}
