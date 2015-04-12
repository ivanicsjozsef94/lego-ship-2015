package com.lego.minddroid.vegleges.BTConnection;

/**
 * Created by József on 2015.03.03..
 */
public class LCPMessage {

    public static byte DIRECT_COMMAND_REPLY = (byte) 0x00;
    public static byte DIRECT_COMMAND_NOREPLY = (byte) 0x80;
    public static byte PLAY_TONE = (byte) 0x03;
    //this controls the motor
    public static byte SET_OUTPUT_COMMAND = (byte) 0x04;
    public static byte OUTPUT_MODE_BYTE = (byte) 0x07;
    public static byte REGULATION_MODE_BYTE = (byte) 0x00;

    public static byte[] whichMotor = {
            (byte) 0x00,
            (byte) 0x01,
            (byte) 0x02
    };

    public static byte[] speedPercentages = {
            (byte) 0x00,
            (byte) 0x0A, //10%
            (byte) 0x14,
            (byte) 0x1E,
            (byte) 0x28,
            (byte) 0x32,
            (byte) 0x3C,
            (byte) 0x46,
            (byte) 0x50,
            (byte) 0x5A,
            (byte) 0x64,
    };

    public static byte[] FIRMWARE_VERSION = {0x6C, 0x4D, 0x49, 0x64};

   public static byte[] getBeepMessage() {
       byte[] message = new byte[8];

       message[0] = (byte) 0x06; // Use 6 bytes + 2 length
       message[1] = (byte) 0x00;

       message[2] = DIRECT_COMMAND_NOREPLY;
       message[3] = PLAY_TONE;
       // Frekvencia - 523 HZ.
       message[4] = (byte) 0x0B;
       message[5] = (byte) 0x02;

       //500 milliseconds - 1F4;
       message[6] = (byte) 0xF4;
       message[7] = (byte) 0x01;

       return message;
   }
    public static byte[] getProgramNameMessage() {
        byte[] message = new byte[11];

        message[0] = 0x0B;
        message[1] = 0x01;
        message[2] = 0x00;
        message[3] = 0x01;
        message[4] = 0x00;
        message[5] = (byte)0x9A;
        message[6] = 0x00;
        message[7] = 0x00;
        message[8] = 0x00;
        message[9] = 0x00;
        message[10] = 0x60;



        return message;
    }
    public static byte[] getBatteryInfo() {
        byte[] message = {
                (byte) 0x02,
                (byte) 0x00,
                DIRECT_COMMAND_REPLY,
                (byte) 0x0B
        };
        return message;
    }

    public static byte[] getMotor(int speed, int motor) {
        byte[] message = new byte[14];
        if(speed <= 10 && speed >= 0) {

            //command length 12 bytes + 2 length byte
            message[0] = (byte) 0x0C;
            message[1] = (byte) 0x00;

            //Direct command - No reply
            message[2] = (byte) DIRECT_COMMAND_REPLY;
            message[3] = SET_OUTPUT_COMMAND;
            message[4] = (byte) whichMotor[motor];
            message[5] = speedPercentages[speed];
            message[6] = OUTPUT_MODE_BYTE;
            message[7] = REGULATION_MODE_BYTE;
            message[8] = (byte) 0x00;
            message[9] = (byte) 0x20; //for
            //Tacho Limits
            message[10] = (byte) 0x00;
            message[11] = (byte) 0x00;
            message[12] = (byte) 0x00;
            message[13] = (byte) 0x00;

        }
        return message;
    }


}
