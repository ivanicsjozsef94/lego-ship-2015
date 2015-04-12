package com.lego.minddroid.vegleges;

import com.lego.minddroid.vegleges.Location.CompassTracker;

/**
 * Created by József on 2015.03.23..
 */
public class AutomaticDriving {

    static int counter = 0; //If it was 5 times incremented, then recalculate the direction
    static double[] coordinateX;
    static double[] coordinateY;
    static double tempCoordinateX, tempCoordinateY;
    static double finalCoordinateX, finalCoordinateY;
    static boolean tempCoordinatesExecuted;
    static boolean finalCoordinatedExecuted;

    public AutomaticDriving(double tempCoordinateX, double tempCoordinateY,
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
            angle = calculatedAngle(tempCoordinateX,tempCoordinateY,averageCoordinateX,averageCoordinateY);
        } else if(!finalCoordinatedExecuted) {
            angle = calculatedAngle(finalCoordinateX,finalCoordinateY,averageCoordinateX,averageCoordinateY);
        }
        double result = degree-angle;


    }

    private double calculateDirectionLength(double calculateCoordinateX, double calculateCoordinateY,
        double currentCoordinateX, double currentCoordinateY) {
        //|-AB→| = √[(x2 - x1)2+(y2 - y1)2]
        double tagOne = (calculateCoordinateX-currentCoordinateX);
        double tagTwo = (calculateCoordinateY-currentCoordinateY);
        double finalScore = Math.sqrt((Math.pow(tagOne,2)+Math.pow(tagTwo,2)));
        return finalScore;
    }

    private double auxiliaryDirectionVector() {
        //|-AB→| = √[(x2 - x1)2]
        double tagOne = 10;
        double finalScore = Math.sqrt(Math.pow(tagOne,2));
        return finalScore;
    }

    public double calculatedAngle(double finalCoordinateX, double finalCoordinateY,
        double currentCoordinateX, double currentCoordinateY) {
        //a=arccos((a*b)/(|a|*|b|))
        double szamlalo = currentCoordinateX*finalCoordinateX + currentCoordinateY*finalCoordinateY;
        double nevezo = (
            calculateDirectionLength
                    (finalCoordinateX,finalCoordinateY,currentCoordinateX,currentCoordinateY)/
            auxiliaryDirectionVector());
        double angle = Math.acos((szamlalo/nevezo));
        return angle;
    }


    public boolean manualControlling(int top, int left, int down, int right) {
        return true;
    }
}
