package org.tbfeng.apt.domian.test;

import org.tbfeng.apt.annotation.BuildProperty;

public class Company {

    private String name;

    private String email ;

    public String getName() {
        return name;
    }

    @BuildProperty
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}