package com.phungnlg.hellodoctor;

/**
 * Created by Phil on 07/05/2017.
 */

public class Doctor {
    public String name;
    public String mobile;
    public String address;
    public String major;
    public String workplace;

    public Doctor() {
    }

    public Doctor(String n, String a, String m, String ma, String w) {
        this.name = n;
        this.address = a;
        this.mobile = m;
        this.major = ma;
        this.workplace = w;
    }
}
