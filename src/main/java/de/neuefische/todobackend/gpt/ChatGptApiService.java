package de.neuefische.todobackend.gpt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ChatGptApiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ChatGptApiService(RestClient.Builder builder, @Value("${app.gpt.api.key}") String key, ObjectMapper objectMapper) {
        restClient = builder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .build();
        this.objectMapper = objectMapper;
    }

    public String spellCheck(String text) {
        ChatGptResponse body = restClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .body(new ChatGptRequest("gpt-4o-mini", List.of(new ChatGptMessage("Prüfe die folgende Nachricht auf Rechtschreibfehler. Gibt mir ein json Objekt mit der folgenden Struktur zurück: {original: string, corrected: string}. `" + text + "`", "user")), new ChatGptFormat("json_object")))
                .retrieve()
                .body(ChatGptResponse.class);

        GptSpellCheck spellCheck;
        try {
            spellCheck = objectMapper.readValue(body.choices().get(0).message().content(), GptSpellCheck.class);
        }
        catch (NullPointerException | JsonProcessingException e) {
            return text;
        }

        return spellCheck.corrected();
    }
}
