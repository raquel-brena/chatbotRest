package com.ufrn.imd.chatbotrest.controller;

import com.ufrn.imd.chatbotrest.model.Message;
import com.ufrn.imd.chatbotrest.model.Chatbot;

import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/chatbot")
public class ChatController {
    private final Chatbot bot = new Chatbot();
    @PostMapping
    public Message sendMessage (@RequestBody Message request) {
        String response = bot.answer(request.getMessage());
        return new Message(response, new Date());
    }
}
