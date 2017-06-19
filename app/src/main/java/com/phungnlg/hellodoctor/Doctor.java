package com.phungnlg.hellodoctor;

/**
 * Created by Phil on 07/05/2017.
 */

public class Doctor {
    private String name;
    private String mobile;
    private String address;
    private String major;
    private String workplace;

    public Doctor() {
    }

    public Doctor(String n, String a, String m, String ma, String w) {
        this.name = n;
        this.address = a;
        this.mobile = m;
        this.major = ma;
        this.workplace = w;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }
}
