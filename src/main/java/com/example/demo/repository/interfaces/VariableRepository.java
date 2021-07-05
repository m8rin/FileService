package com.example.demo.repository.interfaces;

import com.example.demo.model.Variable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariableRepository extends CrudRepository<Variable, Integer> {
}
