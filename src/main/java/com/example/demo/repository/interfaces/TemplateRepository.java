package com.example.demo.repository.interfaces;

import com.example.demo.model.Template;
import org.springframework.data.repository.CrudRepository;

// Расширяя Spring CrudRepository, будем реализовывать некоторые методы для нашего репозитория данных.
// Таким образом экономим много шаблонного кода.
public interface TemplateRepository extends CrudRepository<Template, Integer> {
}
