package com.onlinestore.jdoulke.onlinestorefx.entities;

import java.util.HashMap;
import java.util.Map;

public class GenericEntry {
    private final Map<String, String> data;

    public GenericEntry() {
        this.data = new HashMap<>();
    }


    public void addData(String column, String value) {
        data.put(column, value);
    }


    public String getData(String column) {
        return data.getOrDefault(column, "");
    }


    public Map<String, String> getAllData() {
        return data;
    }
}
