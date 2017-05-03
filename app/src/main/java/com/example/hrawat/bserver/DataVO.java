package com.example.hrawat.bserver;

import java.io.Serializable;

/**
 * Created by hrawat on 26-04-2017.
 */

public class DataVO implements Serializable {

    private Object object;

    public DataVO(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
