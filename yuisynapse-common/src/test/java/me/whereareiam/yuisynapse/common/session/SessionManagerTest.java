package me.whereareiam.yuisynapse.common.session;

import me.whereareiam.yuisynapse.common.context.ConversationContext;
import me.whereareiam.yuisynapse.common.provider.DefaultProviderRegistry;
import me.whereareiam.yuisynapse.common.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionManagerTest {
    
    @Mock
    private DefaultProviderRegistry providerRegistry;
    
    private SessionManager sessionManager;
    
    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager(providerRegistry);
    }
    
    @Test
    void shouldCreateSessionWithUniqueId() {
        // Given
        ConversationContext context1 = new ConversationContext("temp-id-1");
        ConversationContext context2 = new ConversationContext("temp-id-2");
        
        // When
        String sessionId1 = sessionManager.createSession(context1);
        String sessionId2 = sessionManager.createSession(context2);
        
        // Then
        assertThat(sessionId1).isNotNull();
        assertThat(sessionId2).isNotNull();
        assertThat(sessionId1).isNotEqualTo(sessionId2);
        assertThat(context1.getSessionId()).isEqualTo(sessionId1);
        assertThat(context2.getSessionId()).isEqualTo(sessionId2);
    }
    
    @Test
    void shouldRetrieveExistingSession() {
        // Given
        ConversationContext context = new ConversationContext("temp-id");
        context.addUser(TestUtils.mockFluctlight());
        String sessionId = sessionManager.createSession(context);
        
        // When
        DefaultSynapseSession retrievedSession = sessionManager.getSession(sessionId);
        
        // Then
        assertThat(retrievedSession).isNotNull();
        assertThat(retrievedSession.getSessionId()).isEqualTo(sessionId);
    }
    
    @Test
    void shouldReturnNullForNonExistentSession() {
        // When
        DefaultSynapseSession session = sessionManager.getSession("non-existent-id");
        
        // Then
        assertThat(session).isNull();
    }
    
    @Test
    void shouldRemoveSessionAndClose() {
        // Given
        ConversationContext context = new ConversationContext("temp-id");
        String sessionId = sessionManager.createSession(context);
        
        // Verify session exists
        assertThat(sessionManager.getSession(sessionId)).isNotNull();
        
        // When
        sessionManager.removeSession(sessionId);
        
        // Then
        assertThat(sessionManager.getSession(sessionId)).isNull();
    }
    
    @Test
    void shouldHandleRemovingNonExistentSession() {
        // When/Then - should not throw
        sessionManager.removeSession("non-existent-id");
    }
    
    @Test
    void shouldHandleConcurrentSessionCreation() throws InterruptedException {
        // Given
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        String[] sessionIds = new String[threadCount];
        
        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    ConversationContext context = new ConversationContext("temp-" + index);
                    sessionIds[index] = sessionManager.createSession(context);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
        
        // Then - all session IDs should be unique
        assertThat(sessionIds)
                .doesNotContainNull()
                .doesNotHaveDuplicates();
        
        // All sessions should be retrievable
        for (String sessionId : sessionIds) {
            assertThat(sessionManager.getSession(sessionId)).isNotNull();
        }
    }
    
    @Test
    void shouldCancelAllSessionsOnShutdown() {
        // Given
        ConversationContext context1 = new ConversationContext("temp-1");
        ConversationContext context2 = new ConversationContext("temp-2");
        ConversationContext context3 = new ConversationContext("temp-3");
        
        String sessionId1 = sessionManager.createSession(context1);
        String sessionId2 = sessionManager.createSession(context2);
        String sessionId3 = sessionManager.createSession(context3);
        
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(3);
        
        // When
        sessionManager.cancelAllSessions();
        
        // Then
        assertThat(sessionManager.getActiveSessionCount()).isZero();
        assertThat(sessionManager.getSession(sessionId1)).isNull();
        assertThat(sessionManager.getSession(sessionId2)).isNull();
        assertThat(sessionManager.getSession(sessionId3)).isNull();
    }
    
    @Test
    void shouldTrackActiveSessionCount() {
        // Given
        assertThat(sessionManager.getActiveSessionCount()).isZero();
        
        // When
        ConversationContext context1 = new ConversationContext("temp-1");
        String sessionId1 = sessionManager.createSession(context1);
        
        // Then
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);
        
        // When
        ConversationContext context2 = new ConversationContext("temp-2");
        sessionManager.createSession(context2);
        
        // Then
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(2);
        
        // When
        sessionManager.removeSession(sessionId1);
        
        // Then
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);
    }
}
