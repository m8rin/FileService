package com.example.demo.service.interfaces;

import com.example.demo.model.Variable;

import java.util.List;

public interface IVariableService {
    //Возвращает список всех имеющихся блюд
    List<Variable> readAll();

    //Возвращает переменную по его ID
    Variable read(int id);

    // Создает новую переменную
    void create(Variable variable);

    boolean delete(int id);
}
