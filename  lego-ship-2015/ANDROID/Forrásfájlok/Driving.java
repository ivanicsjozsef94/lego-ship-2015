package com.lego.minddroid.vegleges;

import android.util.Log;

import com.lego.minddroid.vegleges.Location.CompassTracker;

/**
 * Created by József on 2015.04.09..
 */
public class Driving {

    static int counter = 0; //If it was 5 times incremented, then recalculate the direction
    static double[] coordinateX;
    static double[] coordinateY;
    static double tempCoordinateX, tempCoordinateY;
    static double finalCoordinateX, finalCoordinateY;
    static boolean tempCoordinatesExecuted;
    static boolean finalCoordinatedExecuted;

    public Driving (double tempCoordinateX, double tempCoordinateY,
                            double finalCoordinateX, double finalCoordinateY) {
        coordinateX = new double[] {0,0,0,0,0};
        coordinateY = new double[] {0,0,0,0,0};

        this.tempCoordinateX = tempCoordinateX;
        this.tempCoordinateY = tempCoordinateY;
        this.finalCoordinateX= finalCoordinateX;
        this.finalCoordinateY= finalCoordinateY;

        tempCoordinatesExecuted = false;
        finalCoordinatedExecuted= false;

    }

    public void changeCoordinates(double tempCoordinateX, double tempCoordinateY,
                                  double finalCoordinateX, double finalCoordinateY) {
        this.tempCoordinateX = tempCoordinateX;
        this.tempCoordinateY = tempCoordinateY;
        this.finalCoordinateX= finalCoordinateX;
        this.finalCoordinateY= finalCoordinateY;

    }

    //Ha nem létezik még az adat vagy nem valódi az adat, akkor kivételt dob!
    public boolean automatedControlling(Double coordinateX, Double coordinateY)
            throws IllegalArgumentException{
        if((coordinateX >= -90.0 && coordinateX <= 90.0) && (coordinateY >= -180.0 && coordinateY <= 180.0)) {
            if(coordinateX == null || coordinateY == null) { throw new IllegalArgumentException();}
            else {
                if(counter < 5) { //Ha kisebb mint 5, akkor még csak adatot gyűjtünk!
                    this.coordinateX[counter] = coordinateX;
                    this.coordinateY[counter] = coordinateY;
                    counter++;
                } else { //Adatok újrakalibrálása.
                    counter = 0;
                    recalibrating();
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
        return true;
    }

    public void recalibrating() {
        double averageCoordinateX = 0.0;
        double averageCoordinateY = 0.0;
        float degree = 0;

        for(int i=0; i<coordinateX.length; i++) {
            averageCoordinateX += coordinateX[i];
            averageCoordinateY += coordinateY[i];
        }
        averageCoordinateX /= coordinateX.length;
        averageCoordinateY /= coordinateY.length;
        degree = CompassTracker.getCurrentDegree();
        double angle = 0;

        if(!tempCoordinatesExecuted) {
            angle = RotateDegree(tempCoordinateX,tempCoordinateY,averageCoordinateX,averageCoordinateY);
        } else if(!finalCoordinatedExecuted) {
            angle = RotateDegree(finalCoordinateX,finalCoordinateY,averageCoordinateX,averageCoordinateY);
        }

        Log.i("Driving: ", ""+angle);
        double difference = Math.abs((angle-degree));
        double difference2= Math.abs(360-degree+angle);

        if(degree>=angle && difference>=180) {
            //Jobbra fordulj, még hozzá difference2 fokkal!
            Log.i("Turning with: ", ""+difference2);
        } else if(degree>=angle && difference<180){
            //Balra fordulj difference fokkal!
        } else if(degree<angle && difference>=180) {
            //Balra fordulj difference2 fokkal!
        } else if(degree<angle && difference<180) {
            //Jobbra fordulj difference fokkal!
        }


    }

    //Két pont közötti távolság
    public static double distance(double x1, double y1, double x2, double y2)
    {
        //distance (A, B) = R * arccos (sin(latA) * sin(latB) + cos(latA) * cos(latB) * cos(lonA-lonB))
        double R = 6372.795477598;
        x1 = x1 / 180 * Math.PI;
        x2 = x2 / 180 * Math.PI;
        y1 = y1 / 180 * Math.PI;
        y2 = y2 / 180 * Math.PI;
        double distance = (R * Math.acos(Math.sin(x1) * Math.sin(x2) + Math.cos(x1) *
                Math.cos(x2) * Math.cos(y1 - y2)));
        return distance;
    }
    //Ez fogja kiszámítani, hogy mennyit kell elfordulnia, milyen irányban kell elfordulnia,
    public static double RotateDegree(double x1, double y1, double x2, double y2)
    {
        //Δφ = ln( tan( latB / 2 + π / 4 ) / tan( latA / 2 + π / 4) )
        //Δlon = abs( lonA - lonB )
        //bearing :  θ = atan2( Δlon ,  Δφ )
        x1 = x1 / 180 * Math.PI;
        x2 = x2 / 180 * Math.PI;
        y1 = y1 / 180 * Math.PI;
        y2 = y2 / 180 * Math.PI;
        double deltaFi = Math.log(Math.tan((x2 / 2 + Math.PI/ 4)) / Math.tan((x1 / 2 + Math.PI / 4)));
        double deltaLon = Math.abs(y1 - y2);
        if (deltaLon > Math.PI) deltaLon = deltaLon - 180;
        //if (deltaLon >= 180) deltaLon = deltaLon - 180;
        double angl = Math.atan2(deltaLon, deltaFi);
        angl = angl * 180 / Math.PI;
        if (y2 < y1) angl = 360 - angl;

        return angl;
    }

    public boolean manualControlling(int top, int left, int down, int right) {
        return true;
    }
}
