package com.example.webrtckw.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class MainController {
    @RequestMapping("/chat/room/{roomId}")
    public ModelAndView chat() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("chat");
        return mv;
    }

    @GetMapping("/chatrooms")
    public String chatRooms(Model model) {
        return "chatrooms";
    }

}
