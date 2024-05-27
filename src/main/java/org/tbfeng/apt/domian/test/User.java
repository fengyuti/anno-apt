package org.tbfeng.apt.domian.test;

import org.tbfeng.apt.annotation.BuildProperty;

public class User {

    private String name;

    private String email;

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    @BuildProperty
    public void setEmail(String email) {
        this.email = email;
    }
}