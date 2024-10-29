package de.neuefische.todobackend.gpt;

import java.util.List;

public record ChatGptResponse(
        List<ChatGptChoice> choices
) {
}
