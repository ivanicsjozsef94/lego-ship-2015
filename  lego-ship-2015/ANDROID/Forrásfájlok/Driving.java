package com.lego.minddroid.vegleges;

import android.media.MediaPlayer;
import android.util.Log;

import com.lego.minddroid.vegleges.BTConnection.BTCommunicator;
import com.lego.minddroid.vegleges.BTConnection.LCPMessage;
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
    static double lastDistance = 0;
    static boolean hasStarted = false;//Elindította-e már valaha?

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

    public static void changeCoordinates(double tempCoordinateX1, double tempCoordinateY1,
                                  double finalCoordinateX1, double finalCoordinateY1) {
        tempCoordinateX = tempCoordinateX1;
        tempCoordinateY = tempCoordinateY1;
        finalCoordinateX= finalCoordinateX1;
        finalCoordinateY= finalCoordinateY1;

    }

    //Ha nem létezik még az adat vagy nem valódi az adat, akkor kivételt dob!
    public boolean automatedControlling(Double coordinateX, Double coordinateY)
            throws IllegalArgumentException{
        //Manual controlling
        if(MainActivity.manualUp != 0 || MainActivity.manualLeft != 0 || MainActivity.manualRight != 0) {
            manualControlling();
        } else {
            if ((coordinateX >= -90.0 && coordinateX <= 90.0) && (coordinateY >= -180.0 && coordinateY <= 180.0)) {
                if (coordinateX == null || coordinateX == 0 || coordinateY == null || coordinateY == 0) {
                    throw new IllegalArgumentException();
                } else {
                    if (counter < 5) { //Ha kisebb mint 5, akkor még csak adatot gyűjtünk!
                        this.coordinateX[counter] = coordinateX;
                        this.coordinateY[counter] = coordinateY;
                        counter++;
                    } else { //Adatok újrakalibrálása.
                        counter = 0;
                        if (!tempCoordinatesExecuted && !finalCoordinatedExecuted) {
                            recalibrating();
                            tempCoordinatesExecuted = getTarget(distanceValue);
                            MainActivity.loggingString("Driving: Temp coordinates has reached!");
                            MainActivity.reached.setText("To temp!");
                            hasStarted = true; //Amikor eléri a temp koordinátát, csak a kövi ciklusban induljon a zene!
                        } else if (tempCoordinatesExecuted && !finalCoordinatedExecuted) {
                            recalibrating();
                            finalCoordinatedExecuted = getTarget(distanceValue);
                            MainActivity.reached.setText("To final!");
                            hasStarted = false;
                            MainActivity.loggingString("Driving: Final coordinated has reached!");
                        } else if (tempCoordinatesExecuted && finalCoordinatedExecuted) {
                            MainActivity.loggingString("Driving: Stopping automation driving...");
                            BTCommunicator.getInstance().write(LCPMessage.getMotor(0, 0));
                            BTCommunicator.getInstance().write(LCPMessage.getMotor(0, 1));
                            BTCommunicator.getInstance().write(LCPMessage.getMotor(0, 2));
                            MainActivity.reached.setText("Reached!");
                            MainActivity.changeMotorPercentagesTextViews(0 + "%", 0 + "%", 0 + "%");
                        }
                        Log.d("Driving: ", "" + tempCoordinatesExecuted + "\t" + finalCoordinatedExecuted);
                        if (!hasStarted && tempCoordinatesExecuted && distanceValue <= 40) {
                            hasStarted = true;
                            MainActivity.mediaPlayer.start();
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        return true;
    }

    public void manualControlling() {
        int[] speeds = {10,10,10};
        speeds[0] = MainActivity.manualLeft;
        speeds[1] = MainActivity.manualUp;
        speeds[2] = MainActivity.manualRight;

        BTCommunicator.getInstance().write(LCPMessage.getMotor(speeds[0],0));
        BTCommunicator.getInstance().write(LCPMessage.getMotor(speeds[1],1));
        BTCommunicator.getInstance().write(LCPMessage.getMotor(speeds[2],2));

        MainActivity.changeMotorPercentagesTextViews(
                Integer.toString(speeds[0]*10)+"%",
                Integer.toString(speeds[1]*10)+"%",
                Integer.toString(speeds[2]*10)+"%");
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
            angle = RotateDegree(averageCoordinateX,averageCoordinateY, tempCoordinateX,tempCoordinateY);
            distanceValue = distance(averageCoordinateX,averageCoordinateY, tempCoordinateX,tempCoordinateY);
        } else if(!finalCoordinatedExecuted) {
            angle = RotateDegree(averageCoordinateX,averageCoordinateY, finalCoordinateX,finalCoordinateY);
            distanceValue = distance(averageCoordinateX,averageCoordinateY, finalCoordinateX,finalCoordinateY);
        }
        Log.i("Loginfo: ", "angle " + angle + " degree: " + Math.abs(degree));
        degree = Math.abs(degree);
        int speed[] = {10,10,10};
        if(angle > degree) {
            double diff = angle-degree; //Mindig jobbra fordul!
            double diff2= 360-angle+degree;
            if(diff <= diff2) {
                //Jobbra fordulj!
                Log.i("Loginfo:", "Diff: " + diff + " Diff2:" + diff2 + " jobbra!");
                speed[0] = 10;
                speed[1] = 10;
                if(diff < 30) {
                    speed[2] = 9;
                } else if(diff < 60) {
                    speed[2] = 7;
                } else if(diff < 90) {
                    speed[2] = 5;
                } else if(diff < 120) {
                    speed[2] = 3;
                } else if(diff < 150) {
                    speed[2] = 2;
                } else if(diff < 180) {
                    speed[2] = 0;
                }
            } else {
                //balra fordulj!
                Log.i("Loginfo:", "Diff: " + diff + " Diff2:" + diff2 + " balra!");
                speed[1] = 10;
                speed[2] = 10;
                if(diff2 < 30) {
                    speed[0] = 9;
                } else if(diff2 < 60) {
                    speed[0] = 7;
                } else if(diff2 < 90) {
                    speed[0] = 5;
                } else if(diff2 < 120) {
                    speed[0] = 3;
                } else if(diff2 < 150) {
                    speed[0] = 2;
                } else if(diff2 < 180) {
                    speed[0] = 0;
                }
            }
        } else if(angle < degree){
            double diff2 = degree-angle;
            double diff = 360-degree+angle;
            if(diff <= diff2) {
                //Jobbra fordulj!
                Log.i("Loginfo:", "Diff: " + diff + " Diff2:" + diff2 + " jobbra!");
                speed[0] = 10;
                speed[1] = 10;
                if(diff < 30) {
                    speed[2] = 9;
                } else if(diff < 60) {
                    speed[2] = 7;
                } else if(diff < 90) {
                    speed[2] = 5;
                } else if(diff < 120) {
                    speed[2] = 3;
                } else if(diff < 150) {
                    speed[2] = 2;
                } else if(diff < 180) {
                    speed[2] = 0;
                }
            } else {
                //balra fordulj!
                Log.i("Loginfo:", "Diff: " + diff + " Diff2:" + diff2 + " balra!");
                speed[1] = 10;
                speed[2] = 10;
                if(diff2 < 30) {
                    speed[0] = 9;
                } else if(diff2 < 60) {
                    speed[0] = 7;
                } else if(diff2 < 90) {
                    speed[0] = 5;
                } else if(diff2 < 120) {
                    speed[0] = 3;
                } else if(diff2 < 150) {
                    speed[0] = 2;
                } else if(diff2 < 180) {
                    speed[0] = 0;
                }
            }
        }
        BTCommunicator.getInstance().write(LCPMessage.getMotor(speed[0],0));
        BTCommunicator.getInstance().write(LCPMessage.getMotor(speed[1],1));
        BTCommunicator.getInstance().write(LCPMessage.getMotor(speed[2],2));

        MainActivity.changeMotorPercentagesTextViews(
                Integer.toString(speed[0]*10)+"%",
                Integer.toString(speed[1]*10)+"%",
                Integer.toString(speed[2]*10)+"%");

    }
    private static long distanceValue = 0;
    public static long getDistance() {
        return distanceValue;
    }

    private static double speed = 0;
    public static double getSpeed() {
        return speed;
    }
    public static int speed (double lastDistance, double currentDistance) {
        double change = lastDistance-currentDistance;
        return ((int)((change*3.6)/4)); // 4 mp, és nem méterben, hanem km-ben kérjük!
    }
    //Két pont közötti távolság
    public static long distance(double x1, double y1, double x2, double y2)
    {
        //distance (A, B) = R * arccos (sin(latA) * sin(latB) + cos(latA) * cos(latB) * cos(lonA-lonB))
        double R = 63727954.77598;
        x1 = x1 / 180 * Math.PI;
        x2 = x2 / 180 * Math.PI;
        y1 = y1 / 180 * Math.PI;
        y2 = y2 / 180 * Math.PI;
        double distance = (R * Math.acos(Math.sin(x1) * Math.sin(x2) + Math.cos(x1) *
                Math.cos(x2) * Math.cos(y1 - y2)));
        speed = speed(lastDistance, distance);
        lastDistance = distance;
        return (long)Math.abs(distance);
    }
    //Ez fogja kiszámítani, hogy mennyit kell elfordulnia, milyen irányban kell elfordulnia,
    public static double RotateDegree(double x1, double y1, double x2, double y2)
    {
        //Δφ = ln( tan( latB / 2 + π / 4 ) / tan( latA / 2 + π / 4) )
        //Δlon = abs( lonA - lonB )
        //bearing :  θ = atan2( Δlon ,  Δφ )
        Log.i("Log: ", "X1: " + x1 + "Y1: " + y1 + "X2: " + x2 + "Y2: " + y2);
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

    public static boolean getTarget(double distance) {
        if (distance <= 5.0) {
            return true;
        }
        return false;
    }
}
