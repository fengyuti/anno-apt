package org.tbfeng.apt.test;


import org.tbfeng.apt.domian.test.Company;

/**
 * @author linjf48556
 * @Description
 * @date 2024-05-24
 **/
public class Test {

    public static void main(String[] args) {
        Company example = new Company();
        example.setName("张三");
        System.out.println(example);
    }
}
