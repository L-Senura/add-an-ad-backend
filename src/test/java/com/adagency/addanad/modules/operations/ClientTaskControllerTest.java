package com.adagency.addanad.modules.operations;

import com.adagency.addanad.modules.client.ClientRepo;
import com.adagency.addanad.modules.operations.dto.ClientTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientTaskControllerTest {

    @Mock
    private TaskRepo taskRepo;

    @Mock
    private ClientRepo clientRepo;

    @InjectMocks
    private ClientTaskController clientTaskController;

    private TaskDB unassignedTask;
    private TaskDB assignedTask;

    @BeforeEach
    void setUp() {
        unassignedTask = new TaskDB();
        unassignedTask.setId(100L);
        unassignedTask.setClientId(5L);
        unassignedTask.setClientName("Apex Apparel");
        unassignedTask.setTaskTitle("Original Brief Title");
        unassignedTask.setTaskDetails("Original Brief Details");
        unassignedTask.setTaskCategory("Graphic Design");
        unassignedTask.setPriority("MEDIUM");
        unassignedTask.setStatus("PENDING_COORDINATION");
        unassignedTask.setEmployeeId(null);
        unassignedTask.setEmployeeName(null);

        assignedTask = new TaskDB();
        assignedTask.setId(200L);
        assignedTask.setClientId(5L);
        assignedTask.setClientName("Apex Apparel");
        assignedTask.setTaskTitle("Assigned Brief Title");
        assignedTask.setTaskDetails("Assigned Brief Details");
        assignedTask.setStatus("ASSIGNED");
        assignedTask.setEmployeeId(2L);
        assignedTask.setEmployeeName("Sarah Jenkins");
    }

    @Test
    void updateTask_Success_WhenUnassigned() {
        when(taskRepo.findById(100L)).thenReturn(Optional.of(unassignedTask));
        when(taskRepo.save(any(TaskDB.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientTaskRequest request = new ClientTaskRequest();
        request.setClientId(5L);
        request.setTaskTitle("Updated Title For Graphic Banner");
        request.setTaskDetails("Updated specifications with revised Pantone colors");
        request.setTaskCategory("Video Production");
        request.setPriority("HIGH");
        request.setClientDeadline(LocalDate.of(2026, 11, 15));

        ResponseEntity<?> response = clientTaskController.updateTask(100L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof TaskDB);
        TaskDB updated = (TaskDB) response.getBody();
        assertEquals("Updated Title For Graphic Banner", updated.getTaskTitle());
        assertEquals("Updated specifications with revised Pantone colors", updated.getTaskDetails());
        assertEquals("Video Production", updated.getTaskCategory());
        assertEquals("HIGH", updated.getPriority());
        assertEquals(LocalDate.of(2026, 11, 15), updated.getClientDeadline());
        verify(taskRepo, times(1)).save(unassignedTask);
    }

    @Test
    void updateTask_Fails_WhenAssignedToEmployee() {
        when(taskRepo.findById(200L)).thenReturn(Optional.of(assignedTask));

        ClientTaskRequest request = new ClientTaskRequest();
        request.setClientId(5L);
        request.setTaskTitle("Trying to change assigned task");

        ResponseEntity<?> response = clientTaskController.updateTask(200L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("already been assigned to an employee"));
        verify(taskRepo, never()).save(any());
    }

    @Test
    void updateTask_Fails_WhenCompletedOrCancelled() {
        unassignedTask.setStatus("Completed");
        when(taskRepo.findById(100L)).thenReturn(Optional.of(unassignedTask));

        ClientTaskRequest request = new ClientTaskRequest();
        request.setClientId(5L);
        request.setTaskTitle("Trying to change completed task");

        ResponseEntity<?> response = clientTaskController.updateTask(100L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("already been completed"));
        verify(taskRepo, never()).save(any());
    }

    @Test
    void updateTask_Fails_WhenUnauthorizedClient() {
        when(taskRepo.findById(100L)).thenReturn(Optional.of(unassignedTask));

        ClientTaskRequest request = new ClientTaskRequest();
        request.setClientId(999L); // Different client
        request.setTaskTitle("Hacker title update");

        ResponseEntity<?> response = clientTaskController.updateTask(100L, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(taskRepo, never()).save(any());
    }

    @Test
    void updateTask_Fails_WhenTaskNotFound() {
        when(taskRepo.findById(999L)).thenReturn(Optional.empty());

        ClientTaskRequest request = new ClientTaskRequest();
        ResponseEntity<?> response = clientTaskController.updateTask(999L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(taskRepo, never()).save(any());
    }
}