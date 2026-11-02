package edu.pucp.gtics.lab7_20232283.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"", "/", "/inicio"})
    public String inicio() {
        return "index";
    }
}
