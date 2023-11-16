package com.jahitin.service;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class ADTSubscription implements Serializable {
    @XmlElement
    private String user_id;
    @XmlElement
    private String status;

    public ADTSubscription(int user_id, String status) {
        this.user_id = String.valueOf(user_id);
        this.status = status;
    }
}
