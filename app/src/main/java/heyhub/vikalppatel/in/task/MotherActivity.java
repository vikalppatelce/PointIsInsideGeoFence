package heyhub.vikalppatel.in.task;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MotherActivity extends AppCompatActivity {

    private ArrayList<Geopoint> vertices = new ArrayList<>();

    private AppCompatEditText mLatitudeEt;
    private AppCompatEditText mLongitudeEt;
    private AppCompatEditText mAccuracyEt;

    private AppCompatTextView mResultsTv;
    private AppCompatTextView mResultsBriefTv;

    private AppCompatButton mComputeButton;

    private double boundMinLat;
    private double boundMaxLat;
    private double boundMinLon;
    private double boundMaxLon;

    private double minDistance;
    private double minTempDistance;
    private int indexOfMinVertice;

    private double secondMinDistance;
    private ArrayList<Geopoint> distanceWithVertice = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_mother);
            initUi();
            setUiListener();

            String mJSONStringFromFile = readFile("hub.json");
            getDataFromJSONFile(mJSONStringFromFile);

            //For Test Results : Test
            /*double mLat[] = new double[]{50.840473, 50.842458, 50.843317, 44.197736, 50.854067};
            double mLon[] = new double[]{-0.146755, -0.150285, -0.144960, 1.183339, -0.163824};
            double accuracy[] = new double[]{30.0, 5, 0, 1000000, 100};

            for (int i = 0; i < mLat.length; i++) {
                boolean isLocationWithinArea = isLocationWithinArea(mLat[i], mLon[i], accuracy[i]);

                Log.e("isLocationWithinArea", "lat: " + mLat[i] + ", lon: " + mLon[i] + " & accuracy: " + accuracy[i] + " :: " + isLocationWithinArea);
            }*/


        } catch (Exception e) {
        }
    }

    /**
     * Initialize UI Elements from Layout
     */
    private void initUi() {
        try {
            mLatitudeEt = findViewById(R.id.activityMotherLatitudeEt);
            mLongitudeEt = findViewById(R.id.activityMotherLongitudeEt);
            mAccuracyEt = findViewById(R.id.activityMotherAccuracyEt);
            mComputeButton = findViewById(R.id.activityMotherComputeButton);

            mResultsBriefTv = findViewById(R.id.activityMotherResultsBriefTv);
            mResultsTv = findViewById(R.id.activityMotherResultsTv);
        } catch (Exception e) {
        }
    }

    /**
     * Parse JSON From File : To collect GeoMetric Points for Fencing
     */
    private void getDataFromJSONFile(String mJSONStringFromFile) {
        try {
            JSONObject mJSONObj = new JSONObject(mJSONStringFromFile);
            JSONArray mJSONGeoFenceArr = mJSONObj.getJSONObject("results").getJSONArray("geometry").getJSONArray(0);

            for (int i = 0; i < mJSONGeoFenceArr.length(); i++) {
                vertices.add(new Geopoint(Double.parseDouble(mJSONGeoFenceArr.getJSONObject(i).getString("lat")),
                        Double.parseDouble(mJSONGeoFenceArr.getJSONObject(i).getString("lon"))));
            }

            JSONObject mJSONBounds = mJSONObj.getJSONObject("results").getJSONObject("bounds");
            boundMinLat = Double.parseDouble(mJSONBounds.getString("minlat"));
            boundMinLon = Double.parseDouble(mJSONBounds.getString("minlon"));
            boundMaxLat = Double.parseDouble(mJSONBounds.getString("maxlat"));
            boundMaxLon = Double.parseDouble(mJSONBounds.getString("maxlon"));
        } catch (Exception e) {
        }
    }


    /**
     * UI : Listener
     */
    private void setUiListener() {
        try {
            mComputeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        //Validation Check
                        if (!TextUtils.isEmpty(mLatitudeEt.getText().toString())
                                && !TextUtils.isEmpty(mLongitudeEt.getText().toString()) && !TextUtils.isEmpty(mAccuracyEt.getText().toString())) {

                            distanceWithVertice.clear();

                            boolean isLocationWithinArea = isLocationWithinArea(Double.parseDouble(mLatitudeEt.getText().toString()),
                                    Double.parseDouble(mLongitudeEt.getText().toString()), Double.parseDouble(mAccuracyEt.getText().toString()));


                            boolean isPointInGeoFence = isPointInPolygon(new Geopoint(Double.parseDouble(mLatitudeEt.getText().toString()),
                                    Double.parseDouble(mLongitudeEt.getText().toString())), vertices);

                            if (isPointInGeoFence) {
                                isLocationWithinArea = isPointInGeoFence;
                            }

                            mResultsTv.setText("isLocationWithinArea :: " + String.valueOf(isLocationWithinArea));

                            mResultsBriefTv.setText("lat: " + mLatitudeEt.getText().toString() + "," +
                                    " lon: " + mLongitudeEt.getText().toString() + " & accuracy: " + mAccuracyEt.getText().toString()
                                    + " :: " + isLocationWithinArea + "\n\n"
                                    + "NearBy Point : " + vertices.get(indexOfMinVertice).getLatitude() + ", " + vertices.get(indexOfMinVertice).getLongitude()
                                    + " which is at " + minDistance + "m.");

                        } else {
                            if (TextUtils.isEmpty(mLatitudeEt.getText().toString())) {
                                mLatitudeEt.setError("Please enter latitude");
                                return;
                            }

                            if (TextUtils.isEmpty(mLongitudeEt.getText().toString())) {
                                mLongitudeEt.setError("Please enter longitude");
                                return;
                            }

                            if (TextUtils.isEmpty(mAccuracyEt.getText().toString())) {
                                mAccuracyEt.setError("Please enter accuracy");
                                return;
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    /**
     * @param latitude  : Latitude of Given Point
     * @param longitude : Longitude of Given Point
     * @param accuracy  : Distance in meter which can be allowed for geo fencing
     * @return
     */
    private boolean isLocationWithinArea(Double latitude, Double longitude, Double accuracy) {
        try {
            if (vertices != null && vertices.size() > 0) {
                for (int i = 0; i < vertices.size(); i++) {
                    minTempDistance = distanceBetweenVertices(latitude, longitude, vertices.get(i).getLatitude(), vertices.get(i).getLongitude());
                    if (i == 0) {
                        minDistance = minTempDistance;
                    }

                    distanceWithVertice.add(new Geopoint(minTempDistance, Double.parseDouble(String.valueOf(i))));
                    /*Log.e("minTempDistance:" + i, "" + minTempDistance);*/
                    if (minTempDistance < minDistance) {
                        indexOfMinVertice = i;
                        minDistance = minTempDistance;
                    }
                }
            }

            if (distanceWithVertice != null && distanceWithVertice.size() > 0) {
                Collections.sort(distanceWithVertice, new GeopointSort());
            }


            try {
                Geopoint mTempGeoPoint = getMinimumDistanceIntersectionOfPoint();
                Double mTemp = distanceBetweenVertices(latitude, longitude, mTempGeoPoint.getLatitude(), mTempGeoPoint.getLongitude());

                Log.e("mTempGeoPoint:", "" + mTempGeoPoint.getLatitude() + ", " + mTempGeoPoint.getLongitude()
                        + " -> Distance : " + mTemp + "m");

                if (mTemp > 0) {
                    minDistance = mTemp;
                }
            } catch (Exception e) {
                Log.e("MiniIntersection", e.toString());
            }

            /*Log.e("minDistance: ", "" + minDistance + "m");
            Log.e("indexOfMinVertices:", "" + vertices.get(indexOfMinVertice).getLatitude() + ", " + vertices.get(indexOfMinVertice).getLongitude());*/


            //Whether NearBy Vertext in on left or right side
            if (boundMaxLat > vertices.get(indexOfMinVertice).getLatitude()
                    && boundMaxLon > vertices.get(indexOfMinVertice).getLongitude()) {//Nearby Vertex is on left side
                if (latitude > vertices.get(indexOfMinVertice).getLatitude()
                        && longitude > vertices.get(indexOfMinVertice).getLongitude()) {//inside
                    return true;
                } else {
                    if (accuracy - minDistance >= 0) {
                        return true;
                    }
                }
            } else if (boundMinLat < vertices.get(indexOfMinVertice).getLatitude()
                    && boundMinLon < vertices.get(indexOfMinVertice).getLongitude()) {//Nearby Vertex is on right side
                if (vertices.get(indexOfMinVertice).getLatitude() < latitude
                        && vertices.get(indexOfMinVertice).getLongitude() < longitude) {//inside
                    return true;
                } else {
                    if (accuracy - minDistance >= 0) {
                        return true;
                    }
                }
            }


            /*if (((boundMaxLat > latitude) && (boundMinLat < latitude)) &&
                    (boundMaxLon > longitude) && (boundMinLon < longitude)
                    ) {//Point is inside
                return true;
            } else {//Point is outside
                if ((latitude > boundMaxLat) && (longitude > boundMaxLon)) {// on Left side
                    if (accuracy - minDistance > 0) {
                        return true;
                    }
                } else if ((latitude > boundMaxLat) && (longitude > boundMaxLon)) {// on Right side
                    if (accuracy - minDistance > 0) {
                        return true;
                    }
                }
            }*/
        } catch (Exception e) {
        }

        return false;
    }


    /**
     * Method : To Find distance between two latlong
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return distance in meters
     */
    private double distanceBetweenVertices(double lat1, double lon1, double lat2, double lon2) {
        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344 * 1000;//in to meters
        return Math.round(dist);
    }


    private boolean isPointInPolygon(Geopoint tap, ArrayList<Geopoint> vertices) {
        int intersectCount = 0;
        for (int j = 0; j < vertices.size() - 1; j++) {
            if (rayCastIntersect(tap, vertices.get(j), vertices.get(j + 1))) {
                intersectCount++;
            }
        }

        return (intersectCount % 2) == 1; // odd = inside, even = outside;
    }

    /**
     * Using Ray Intersect Algorithm
     * @param tap
     * @param vertA
     * @param vertB
     * @return
     */
    private boolean rayCastIntersect(Geopoint tap, Geopoint vertA, Geopoint vertB) {

        double aY = vertA.getLatitude();
        double bY = vertB.getLatitude();
        double aX = vertA.getLongitude();
        double bX = vertB.getLongitude();
        double pY = tap.getLatitude();
        double pX = tap.getLongitude();

        if ((aY > pY && bY > pY) || (aY < pY && bY < pY) || (aX < pX && bX < pX)) {
            return false; // a and b can't both be above or below pt.y, and a or b must be east of pt.x
        }

        double m = (aY - bY) / (aX - bX);               // Rise over run
        double bee = (-aX) * m + aY;                // y = mx + b
        double x = (pY - bee) / m;                  // algebra is neat!

        return x > pX;
    }


    /**
     * Get the GeoPoint on GeoFence from where our point P is at minimum distance
     * by getting the two nearest point.
     * @return
     */
    private Geopoint getMinimumDistanceIntersectionOfPoint() {
        Double mPercentageU = 0.0;
        try {
            Double x1 = vertices.get((int) Math.round(distanceWithVertice.get(0).getLongitude())).getLatitude();
            Double y1 = vertices.get((int) Math.round(distanceWithVertice.get(0).getLongitude())).getLongitude();
            Double x2 = vertices.get((int) Math.round(distanceWithVertice.get(1).getLongitude())).getLatitude();
            Double y2 = vertices.get((int) Math.round(distanceWithVertice.get(1).getLongitude())).getLongitude();
            Double x3 = Double.parseDouble(mLatitudeEt.getText().toString());
            Double y3 = Double.parseDouble(mLongitudeEt.getText().toString());

            if (distanceWithVertice != null && distanceWithVertice.size() > 1) {
                mPercentageU = ((x3 - x1) * (x2 - x1) + (y3 - y1) * (y2 - y1)) / (((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));

                if (mPercentageU > 0) {
                    Double mLat = x1 + mPercentageU * (x2 - x1);
                    Double mLon = y1 + mPercentageU * (y2 - y1);
                    return new Geopoint(mLat, mLon);
                }
            }
        } catch (Exception e) {
            Log.e("getMidPoint", e.toString());
        }
        return null;
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    double deg2rad(double deg) {
        return (deg * 3.14 / 180);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    double rad2deg(double rad) {
        return (rad * 180 / 3.14);
    }

    private String readFile(String s) {
        BufferedReader r;
        StringBuilder str = new StringBuilder();
        try {
            r = new BufferedReader(new InputStreamReader(this.getAssets().open(s)));
            String line;
            while ((line = r.readLine()) != null) {
                str.append(line);
            }
        } catch (IOException e) { // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return str.toString();
    }

    public class GeopointSort implements Comparator<Geopoint> {
        @Override
        public int compare(Geopoint Obj1, Geopoint Obj2) {
            try {
                if (Obj1.getLatitude() > Obj2.getLatitude()) {
                    return 1;
                } else {
                    return -1;
                }
            } catch (Exception e) {
                return -1;
            }
        }
    }
}
