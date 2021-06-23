package com.example.demo.service;

import com.example.demo.model.Template;
import com.example.demo.repository.interfaces.TemplateRepository;
import com.example.demo.service.interfaces.ITemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("ITemplateService")
public class TemplateService implements ITemplateService {

    //используем репозиторий для извлечения данных из бд
    @Autowired
    private TemplateRepository repository;

    @Override
    public List<Template> findAll() {
        return (List<Template>) repository.findAll();
    }
}
