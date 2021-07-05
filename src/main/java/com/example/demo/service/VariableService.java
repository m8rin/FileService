package com.example.demo.service;

import com.example.demo.model.Variable;
import com.example.demo.service.interfaces.IVariableService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Component("IVariableService")
public class VariableService implements IVariableService {
    // Хранилище переменных
    private static final Map<Integer, Variable> VARIABLE_REPOSITORY_MAP = new HashMap<>();

    // Переменная для генерации ID переменной
    private static final AtomicInteger VARIABLE_ID_HOLDER = new AtomicInteger();

    @Override
    public void create(Variable variable) {
        final int variableId = VARIABLE_ID_HOLDER.incrementAndGet();
        variable.setId(variableId);
        VARIABLE_REPOSITORY_MAP.put(variableId, variable);
    }

    @Override
    public List<Variable> readAll() {
        return new ArrayList<>(VARIABLE_REPOSITORY_MAP.values());
    }

    @Override
    public Variable read(int id) {
        return VARIABLE_REPOSITORY_MAP.get(id);
    }

    @Override
    public boolean delete(int id) {
        return VARIABLE_REPOSITORY_MAP.remove(id) != null;
    }

}
