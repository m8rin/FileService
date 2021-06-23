package com.example.demo.model;

import javax.persistence.*;

@Entity
@Table(name = "templates")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String filename;

    public Template() {
    }

    public Template(Integer id, String filename) {
        this.id = id;
        this.filename = filename;
    }

    @Override
    public String toString() {
        String sb = "Template{" + "id=" + id +
                ", filename='" + filename + '\'' +
                '}';
        return sb;
    }

    public long getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
