package modelsControllers;

/**
 * Created by dmt on 31.08.2015.
 */
public class Coordinate2D {
    private double coordinate1;
    private double coordinate2;

    protected Coordinate2D(double coordinate1, double coordinate2) {
        this.coordinate1 = coordinate1;
        this.coordinate2 = coordinate2;
    }
    protected double getCoordinate1() {
        return coordinate1;
    }
    protected double getCoordinate2() {
        return coordinate2;
    }
}
