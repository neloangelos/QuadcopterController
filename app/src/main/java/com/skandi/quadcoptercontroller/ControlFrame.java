package com.skandi.quadcoptercontroller;

/**
 * Created by Skandi on 2015/6/1.
 */
public class ControlFrame {
    private static final byte FRAME_HEADER_P1 = 0x03;
    private static final byte FRAME_HEADER_P2 = 0x01;
    private byte[] frame;
    public ControlFrame(){
        frame = new byte[16];
        //frame header
        frame[0] = FRAME_HEADER_P1;
        frame[1] = FRAME_HEADER_P2;
        //ch7 is not used
        setValue(171,0); // ch1
        for(int i = 1; i < 4; i++ ){
            setValue(512,i); //ch2~ch4
        }
        setValue(171,4);  //ch5
        setValue(171,5);  //ch6
        frame[14] = (byte)0xff;
        frame[15] = (byte)0xff;
    }
    public boolean setValue(int value, int tag){
        if (value > 1024) return false;
        synchronized (this){
            byte p1 = (byte)(((tag & 0xff)<<2)|((value & 0x300)>>8));
            byte p2 = (byte)(value & 0xff);
            frame[tag*2+2] = p1;
            frame[tag*2+3] = p2;
        }
        return true;
    }
    public byte[] getBytes(){
        synchronized(this){
            return frame.clone();
        }
    }

}
