package ru.itis.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.SliceImpl;
import ru.itis.dto.compilation.response.CompilationResponse;
import ru.itis.entities.*;
import ru.itis.exceptions.CategoryNotFoundException;
import ru.itis.exceptions.GroupNotFoundException;
import ru.itis.exceptions.NotGroupMemberException;
import ru.itis.mappers.CompilationMapper;
import ru.itis.repositories.CategoryRepository;
import ru.itis.repositories.CompilationRepository;
import ru.itis.repositories.GroupAccountRepository;
import ru.itis.repositories.GroupRepository;
import ru.itis.services.compilationService.CompilationServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceTest {

    @Mock CompilationRepository compilationRepository;
    @Mock GroupRepository groupRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock GroupAccountRepository groupAccountRepository;
    @Mock CompilationMapper compilationMapper;
    @InjectMocks CompilationServiceImpl compilationService;

    private Group group(UUID id) {
        return Group.builder().id(id).name("Группа").build();
    }

    private Category category(long id) {
        return Category.builder().id(id).name("Категория").build();
    }

    @Test
    void addCompilation_byCreator_success() {
        UUID groupId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Group group = group(groupId);
        Category cat = category(1L);
        Compilation saved = Compilation.builder().id(UUID.randomUUID()).title("Подборка").group(group).category(cat).build();
        CompilationResponse expected = CompilationResponse.builder().id(saved.getId()).title("Подборка").build();

        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, requesterId))
                .thenReturn(Optional.of(GroupMemberStatus.CREATOR));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(saved);
        when(compilationMapper.toResponse(saved)).thenReturn(expected);

        CompilationResponse result = compilationService.addCompilation(requesterId, groupId, "Подборка", "Описание", 1);

        assertThat(result.getTitle()).isEqualTo("Подборка");
        verify(compilationRepository).save(any(Compilation.class));
    }

    @Test
    void addCompilation_byAdmin_success() {
        UUID groupId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Group group = group(groupId);
        Category cat = category(2L);
        Compilation saved = Compilation.builder().id(UUID.randomUUID()).title("Подборка").group(group).category(cat).build();
        CompilationResponse expected = CompilationResponse.builder().id(saved.getId()).title("Подборка").build();

        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, requesterId))
                .thenReturn(Optional.of(GroupMemberStatus.ADMIN));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(saved);
        when(compilationMapper.toResponse(saved)).thenReturn(expected);

        CompilationResponse result = compilationService.addCompilation(requesterId, groupId, "Подборка", null, 2);

        assertThat(result).isNotNull();
    }

    @Test
    void addCompilation_byMember_throws() {
        UUID groupId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, requesterId))
                .thenReturn(Optional.of(GroupMemberStatus.MEMBER));

        assertThatThrownBy(() -> compilationService.addCompilation(requesterId, groupId, "x", null, 1))
                .isInstanceOf(NotGroupMemberException.class);
    }

    @Test
    void addCompilation_notMember_throws() {
        UUID groupId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, requesterId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> compilationService.addCompilation(requesterId, groupId, "x", null, 1))
                .isInstanceOf(NotGroupMemberException.class);
    }

    @Test
    void addCompilation_groupNotFound_throws() {
        UUID groupId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, requesterId))
                .thenReturn(Optional.of(GroupMemberStatus.CREATOR));
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compilationService.addCompilation(requesterId, groupId, "x", null, 1))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void addCompilation_categoryNotFound_throws() {
        UUID groupId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, requesterId))
                .thenReturn(Optional.of(GroupMemberStatus.CREATOR));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group(groupId)));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compilationService.addCompilation(requesterId, groupId, "x", null, 99))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void findCompilationsFeed_returnsMapped() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        Group group = group(groupId);
        Category cat = category(1L);
        Compilation c = Compilation.builder().id(UUID.randomUUID()).title("Подборка").group(group).category(cat).build();
        CompilationResponse mapped = CompilationResponse.builder().id(c.getId()).title("Подборка").build();

        when(compilationRepository.findCompilationsByUserId(eq(userId), any()))
                .thenReturn(new SliceImpl<>(List.of(c)));
        when(compilationMapper.toResponse(c)).thenReturn(mapped);

        var result = compilationService.findCompilationsFeed(userId, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Подборка");
    }

    @Test
    void findCompilationsFeed_empty() {
        UUID userId = UUID.randomUUID();
        when(compilationRepository.findCompilationsByUserId(eq(userId), any()))
                .thenReturn(new SliceImpl<>(List.of()));

        var result = compilationService.findCompilationsFeed(userId, 0, 20);

        assertThat(result.getContent()).isEmpty();
    }
}
