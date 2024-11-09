package org.example.telega2.Controllers;


import org.springframework.web.bind.annotation.*;





@RestController
public class BotController {
    public static String botToken="7915945364:AAGKFPwVqd7_rky1TODlGBDefxUDRuLR5Xs";

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
