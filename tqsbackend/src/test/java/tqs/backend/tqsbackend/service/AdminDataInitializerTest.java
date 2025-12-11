package tqs.backend.tqsbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.repository.UserRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminDataInitializer adminDataInitializer;

    @Test
    void whenPasswordNotSet_thenSkipSeeding() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(adminDataInitializer, "adminPassword", null);

        // Act
        adminDataInitializer.run();

        // Assert
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenPasswordSetAndUserDoesNotExist_thenSeedAdmin() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(adminDataInitializer, "adminPassword", "securepass");
        when(userRepository.findByEmail("admin@partyshare.com")).thenReturn(Optional.empty());

        // Act
        adminDataInitializer.run();

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void whenPasswordSetAndUserExists_thenDoNotSeed() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(adminDataInitializer, "adminPassword", "securepass");
        when(userRepository.findByEmail("admin@partyshare.com")).thenReturn(Optional.of(new User()));

        // Act
        adminDataInitializer.run();

        // Assert
        verify(userRepository, never()).save(any(User.class));
    }
}
