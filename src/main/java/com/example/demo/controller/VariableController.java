package com.example.demo.controller;

import com.example.demo.model.Variable;
import com.example.demo.service.interfaces.IVariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class VariableController {

    private final IVariableService IVariableService;

    @Autowired
    public VariableController(IVariableService IVariableService) {
        this.IVariableService = IVariableService;
    }

    @RequestMapping("/fillingVariables")
    public String variableList(Model model) {
        model.addAttribute("variables", IVariableService.readAll());
        return "fillingVariables";
    }

    @GetMapping(value = "/variable")
    public ResponseEntity<List<Variable>> read() {
        final List<Variable> variables = IVariableService.readAll();

        return variables != null && !variables.isEmpty()
                ? new ResponseEntity<>(variables, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/variable{id}")
    public ResponseEntity<Variable> read(@PathVariable(name = "id") int id) {
        final Variable variable = IVariableService.read(id);

        return variable != null
                ? new ResponseEntity<>(variable, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/variable")
    public ResponseEntity<?> create(@RequestBody Variable variable) {
        IVariableService.create(variable);
        System.out.println("\nДобавлена переменная:" + "\nid: " + variable.getId() + "\nname: " + variable.getName() + "\ntype: " + variable.getType());

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
