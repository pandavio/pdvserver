package com.pandav.pdvserver.pdvaccess;

public class PdvAccessResponse {
    public String address;
    public String message;
    public Severity messageSeverity;

    public enum Severity {
        INFORMATION, WARNING, ERROR
    }
}