package com.example.demo.controller;

import com.example.demo.model.Template;
import com.example.demo.service.interfaces.ITemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class TemplateController {
    @Autowired
    private ITemplateService templateService;

    @GetMapping("/showTemplates")
    public String findTemplates(Model model) {

        var templates = (List<Template>) templateService.findAll();

        model.addAttribute("templates",templates);

        return "showTemplates";
    }
}
