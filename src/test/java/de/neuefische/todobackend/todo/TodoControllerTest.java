package de.neuefische.todobackend.todo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
class TodoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    MockRestServiceServer mockRestServiceServer;

    @Test
    void getAllTodos() throws Exception {
        //GIVEN

        //WHEN
        mockMvc.perform(MockMvcRequestBuilders.get("/api/todo"))

                //THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                            []
                        """));

    }

    @Test
    @DirtiesContext
    void postTodo() throws Exception {
        //GIVEN
        mockRestServiceServer.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andExpect(MockRestRequestMatchers.content().json("""
                        {
                            "model": "gpt-4o-mini",
                            "messages": [
                                {
                                    "content": "Prüfe die folgende Nachricht auf Rechtschreibfehler. Gibt mir ein json Objekt mit der folgenden Struktur zurück: {original: string, corrected: string}. `test-description`",
                                    "role": "user"
                                }
                            ],
                            "response_format": {
                                "type": "json_object"
                            }
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                            "id": "chatcmpl-test-id",
                            "object": "chat.completion",
                            "created": 1730105949,
                            "model": "gpt-4o-mini-2024-07-18",
                            "choices": [
                                {
                                    "index": 0,
                                    "message": {
                                        "role": "assistant",
                                        "content": "{\\"original\\": \\"test-description\\", \\"corrected\\": \\"test-Description\\"}",
                                        "refusal": null
                                    },
                                    "logprobs": null,
                                    "finish_reason": "stop"
                                }
                            ],
                            "usage": {
                                "prompt_tokens": 24,
                                "completion_tokens": 7,
                                "total_tokens": 31,
                                "prompt_tokens_details": {
                                    "cached_tokens": 0
                                },
                                "completion_tokens_details": {
                                    "reasoning_tokens": 0
                                }
                            },
                            "system_fingerprint": "fingerprint"
                        }
                        """, MediaType.APPLICATION_JSON));

        //WHEN
        mockMvc.perform(post("/api/todo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "description": "test-description",
                                        "status": "OPEN"
                                    }
                                """)
                )
                //THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                            {
                                "description": "test-Description",
                                "status": "OPEN"
                            }
                        """))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @DirtiesContext
    void putTodo() throws Exception {
        //GIVEN
        mockRestServiceServer.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andExpect(MockRestRequestMatchers.content().json("""
                        {
                            "model": "gpt-4o-mini",
                            "messages": [
                                {
                                    "content": "Prüfe die folgende Nachricht auf Rechtschreibfehler. Gibt mir ein json Objekt mit der folgenden Struktur zurück: {original: string, corrected: string}. `test-description-2`",
                                    "role": "user"
                                }
                            ],
                            "response_format": {
                                "type": "json_object"
                            }
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                            "id": "chatcmpl-test-id",
                            "object": "chat.completion",
                            "created": 1730105949,
                            "model": "gpt-4o-mini-2024-07-18",
                            "choices": [
                                {
                                    "index": 0,
                                    "message": {
                                        "role": "assistant",
                                        "content": "{\\"original\\": \\"test-description-2\\", \\"corrected\\": \\"test-Description-3\\"}",
                                        "refusal": null
                                    },
                                    "logprobs": null,
                                    "finish_reason": "stop"
                                }
                            ],
                            "usage": {
                                "prompt_tokens": 24,
                                "completion_tokens": 7,
                                "total_tokens": 31,
                                "prompt_tokens_details": {
                                    "cached_tokens": 0
                                },
                                "completion_tokens_details": {
                                    "reasoning_tokens": 0
                                }
                            },
                            "system_fingerprint": "fingerprint"
                        }
                        """, MediaType.APPLICATION_JSON));

        Todo existingTodo = new Todo("1", "test-description", TodoStatus.OPEN);

        todoRepository.save(existingTodo);

        //WHEN
        mockMvc.perform(put("/api/todo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "description": "test-description-2",
                                        "status": "IN_PROGRESS"
                                    }
                                """))
                //THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                            {
                                "id": "1",
                                "description": "test-Description-3",
                                "status": "IN_PROGRESS"
                            }
                        """));
    }

    @Test
    @DirtiesContext
    void getById() throws Exception {
        //GIVEN
        Todo existingTodo = new Todo("1", "test-description", TodoStatus.OPEN);
        todoRepository.save(existingTodo);

        //WHEN
        mockMvc.perform(get("/api/todo/1"))
                //THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                            {
                                "id": "1",
                                "description": "test-description",
                                "status": "OPEN"
                            }
                        """));

    }

    @Test
    @DirtiesContext
    void getByIdTest_whenInvalidId_thenStatus404() throws Exception {
        //GIVEN
        //WHEN
        mockMvc.perform(get("/api/todo/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                          "message": "Todo with id: 1 not found!"
                        }
                        """));

    }


    @Test
    @DirtiesContext
    void deleteTodoById() throws Exception {
        //GIVEN
        Todo existingTodo = new Todo("1", "test-description", TodoStatus.OPEN);
        todoRepository.save(existingTodo);

        //WHEN
        mockMvc.perform(delete("/api/todo/1"))
                //THEN
                .andExpect(status().isOk());
    }
}
