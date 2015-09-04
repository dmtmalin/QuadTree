package controllers;

import android.location.Location;

import modelsControllers.QuadTree;

/**
 * Created by dmt on 31.08.2015.
 */
public class QuadController {
    private int depth;
    private int accuracy;
    private boolean isChange;

    private QuadTree lastQuadTree;

    public QuadController(int depth, int accuracy) {
        this.depth = depth;
        isChange = false;
        setAccuracy(accuracy);
    }

    public void update(Location location) {
        String index = getIndexQuad();
        lastQuadTree = new QuadTree(location.getLatitude(), location.getLongitude(), depth);
        String newIndex = getIndexQuad();
        isChange = !newIndex.equals(index);
    }

    public boolean changed() {
        return isChange;
    }

    public String getIndexPoint() {
        String index = "";
        if (lastQuadTree != null) {
            index = lastQuadTree.getPointIndex();
        }
        return index;
    }

    public String getIndexQuad() {
        String index = "";
        if (lastQuadTree != null) {
            index = lastQuadTree.getQuadIndex(accuracy);
        }
        return index;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy > depth ? depth : depth - accuracy;
    }

}
