package com.open.camel.amq.retry;

import javax.persistence.Entity;
import java.io.Serializable;

@Entity
public class BaseMessage implements Serializable {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
