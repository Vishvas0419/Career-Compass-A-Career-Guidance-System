package com.example.cgs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.cgs.entities.Message;
import com.example.cgs.repositories.MessageRepository;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    /**
     * Store a message.
     *
     * @param message The message payload from the client.
     * @return The saved message object.
     */
    @PostMapping
    public ResponseEntity<String> saveMessage(@RequestBody Message message, RedirectAttributes redirectAttributes) {
        // Save the message
        messageRepository.save(message);

        // Add a success message
        redirectAttributes.addFlashAttribute("successMessage", "Message sent successfully!");

        // Redirect back to the form page
        return ResponseEntity.ok("message stored");
    }

    /**
     * Retrieve all messages.
     *
     * @return A list of all stored messages.
     */
    @GetMapping
    public ResponseEntity<List<Message>> getAllMessages() {
        List<Message> messages = messageRepository.findAll();
        return ResponseEntity.ok(messages);
    }
}