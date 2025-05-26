package com.pandav.pdvserver.pdvauth;

public class PdvAuthRequest {
    public String message;
    public Severity messageSeverity;
    public String userMessage;
    public String signingMessage;
    public String sigmaBoolean;
    public String replyTo;
	
    public enum Severity {
        INFORMATION, WARNING, ERROR
    }
}