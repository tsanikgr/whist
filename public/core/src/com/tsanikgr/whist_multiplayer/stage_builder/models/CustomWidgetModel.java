package com.tsanikgr.whist_multiplayer.stage_builder.models;

import java.util.HashMap;
import java.util.Map;

public class CustomWidgetModel extends BaseActorModel {
    private final Map<String, String> attributeMap = new HashMap<>();
    private String klass;

    public void addAttribute(String key, String value) {
        this.attributeMap.put(key, value);
    }

    public String getAttribute(String key) {
        return this.attributeMap.get(key);
    }

    public String getKlass() {
        return klass;
    }

    public void setKlass(String klass) {
        this.klass = klass;
    }

    public Map<String, String> getAttributeMap() {
        return this.attributeMap;
    }
}
