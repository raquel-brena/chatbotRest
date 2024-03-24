package com.ufrn.imd.chatbotrest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Message {

    private String message;
    private Date timestamp;
}


