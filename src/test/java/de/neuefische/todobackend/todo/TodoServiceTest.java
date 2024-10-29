package de.neuefische.todobackend.todo;

import de.neuefische.todobackend.gpt.ChatGptApiService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TodoServiceTest {

    TodoRepository todoRepository = mock(TodoRepository.class);
    IdService idService = mock(IdService.class);
    ChatGptApiService chatGptApiService = mock(ChatGptApiService.class);
    TodoService todoService = new TodoService(todoRepository, idService, chatGptApiService);

    @Test
    void findAllTodos() {
        //GIVEN
        Todo t1 = new Todo("1", "d1", TodoStatus.OPEN);
        Todo t2 = new Todo("2", "d2", TodoStatus.OPEN);
        Todo t3 = new Todo("3", "d3", TodoStatus.OPEN);
        List<Todo> todos = List.of(t1, t2, t3);

        when(todoRepository.findAll()).thenReturn(todos);

        //WHEN
        List<Todo> actual = todoService.findAllTodos();

        //THEN

        verify(todoRepository).findAll();
        assertEquals(todos, actual);
    }

    @Test
    void addTodo() {
        //GIVEN
        NewTodo newTodo = new NewTodo("Test-Description", TodoStatus.OPEN);
        Todo todoToSave = new Todo("Test-Id", "Corrected-Description", TodoStatus.OPEN);

        when(idService.randomId()).thenReturn("Test-Id");
        when(todoRepository.save(todoToSave)).thenReturn(todoToSave);
        when(chatGptApiService.spellCheck("Test-Description")).thenReturn("Corrected-Description");

        //WHEN

        Todo actual = todoService.addTodo(newTodo);

        //THEN
        verify(idService).randomId();
        verify(todoRepository).save(todoToSave);
        verify(chatGptApiService).spellCheck("Test-Description");
        assertEquals(todoToSave, actual);
    }

    @Test
    void updateTodo() {
        //GIVEN
        String id = "123";
        UpdateTodo todoToUpdate = new UpdateTodo("test-description", TodoStatus.IN_PROGRESS);

        Todo updatedTodo = new Todo("123", "Corrected-description", TodoStatus.IN_PROGRESS);

        when(todoRepository.save(updatedTodo)).thenReturn(updatedTodo);
        when(chatGptApiService.spellCheck("test-description")).thenReturn("Corrected-description");


        //WHEN

        Todo actual = todoService.updateTodo(todoToUpdate, id);

        //THEN
        verify(chatGptApiService).spellCheck("test-description");
        verify(todoRepository).save(updatedTodo);
        assertEquals(updatedTodo, actual);
    }

    @Test
    void getTodoByIdTest_whenValidId_ThenReturnTodo() {
        //GIVEN
        String id = "1";
        Todo todo = new Todo("1", "test-description", TodoStatus.OPEN);

        when(todoRepository.findById(id)).thenReturn(Optional.of(todo));

        //WHEN

        Todo actual = todoService.findTodoById(id);

        //THEN
        verify(todoRepository).findById(id);
        assertEquals(todo, actual);
    }

    @Test
    void getTodoByIdTest_whenInvalidId_ThenThrowException() {
        //GIVEN
        String id = "1";

        when(todoRepository.findById(id)).thenReturn(Optional.empty());

        //WHEN

        assertThrows(NoSuchElementException.class, () -> todoService.findTodoById(id));

        //THEN
        verify(todoRepository).findById(id);
    }

    @Test
    void deleteTodo() {
        //GIVEN
        String id = "1";
        doNothing().when(todoRepository).deleteById(id);

        //WHEN

        todoService.deleteTodo(id);

        //THEN
        verify(todoRepository).deleteById(id);
    }
}
