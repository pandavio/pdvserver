package com.pandav.pdvserver.pdvauth;

public class SigmaProp {
    public static SigmaProp createFromAddress(String address) {
        return new SigmaProp();
    }

    public byte[] toBytes() {
        return new byte[]{0x01, 0x02}; // dummy data
    }
}
