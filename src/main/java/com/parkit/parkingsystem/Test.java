package com.parkit.parkingsystem;

import java.sql.Date;

public class Test{
    public static void main(String[] args) {
        Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        java.util.Date d = new java.util.Date(System.currentTimeMillis() - (2 * 60 * 60 * 1000));
        System.out.println(inTime);
        System.out.println(d);
    }
}
