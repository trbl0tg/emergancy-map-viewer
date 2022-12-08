package com.mycompany.app.controller;

import com.mycompany.app.JavaFxApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController  // Spring MVC Controller
@RequestMapping("/integration")
public class MyController {

    @Autowired
    private JavaFxApp fxApp;
    @PostMapping
    public String reportsLocationIntegration(@RequestBody ReportEnvelop body) {
        fxApp.setEnvelope(body);
        return "Success";
    }

//    @GetMapping
//    public void hello() {
//        System.out.println("draw!");
//    }
}
