package com.mycompany.app.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportEnvelopItem {
    private String userId;
    private Double lat;
    private Double lon;
    private String dangerLevel;
    private String localDateString;
}
