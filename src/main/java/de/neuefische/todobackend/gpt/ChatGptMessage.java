package de.neuefische.todobackend.gpt;

public record ChatGptMessage(
        String content,
        String role
) {
}
