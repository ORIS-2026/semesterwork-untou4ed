package ru.itis.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itis.dto.compilation.response.CompilationResponse;
import ru.itis.dto.group.response.GroupDetailResponse;
import ru.itis.dto.group.response.GroupResponse;
import ru.itis.entities.*;
import ru.itis.exceptions.AlreadyGroupMemberException;
import ru.itis.exceptions.GroupCreatorCannotLeaveException;
import ru.itis.exceptions.GroupNotFoundException;
import ru.itis.exceptions.NotGroupMemberException;
import ru.itis.mappers.CompilationMapper;
import ru.itis.mappers.GroupMapper;
import ru.itis.repositories.*;
import ru.itis.services.groupService.GroupServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock GroupRepository groupRepository;
    @Mock GroupAccountRepository groupAccountRepository;
    @Mock CompilationRepository compilationRepository;
    @Mock UserRepository userRepository;
    @Mock GroupMapper groupMapper;
    @Mock CompilationMapper compilationMapper;
    @InjectMocks GroupServiceImpl groupService;

    private Group group(UUID id) {
        return Group.builder().id(id).name("Группа").description("Описание").build();
    }

    private User user(UUID id) {
        return User.builder().id(id).username("u").name("N").enabled(true).phoneNumber("+7900").build();
    }

    @Test
    void createGroup_savesGroupAndCreatorMembership() {
        UUID creatorId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        Group group = group(groupId);
        GroupResponse expected = GroupResponse.builder().id(groupId).name("Группа").build();

        when(groupRepository.save(any(Group.class))).thenReturn(group);
        when(userRepository.getReferenceById(creatorId)).thenReturn(user(creatorId));
        when(groupMapper.toResponse(group)).thenReturn(expected);

        GroupResponse result = groupService.createGroup("Группа", "Описание", creatorId);

        assertThat(result.getId()).isEqualTo(groupId);
        verify(groupAccountRepository).save(argThat(ga -> ga.getStatus() == GroupMemberStatus.CREATOR));
    }

    @Test
    void findGroupsByUserId_returnsList() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        Group group = group(groupId);
        GroupResponse response = GroupResponse.builder().id(groupId).name("Группа").build();

        when(groupRepository.findGroupsByUserId(userId)).thenReturn(List.of(group));
        when(groupMapper.toResponse(group)).thenReturn(response);

        List<GroupResponse> result = groupService.findGroupsByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(groupId);
    }

    @Test
    void getGroupDetail_found_withMemberStatus() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Group group = group(groupId);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(compilationRepository.findByGroupId(groupId)).thenReturn(List.of());
        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, userId))
                .thenReturn(Optional.of(GroupMemberStatus.MEMBER));

        GroupDetailResponse result = groupService.getGroupDetail(groupId, userId);

        assertThat(result.getId()).isEqualTo(groupId);
        assertThat(result.getMemberStatus()).isEqualTo("member");
        assertThat(result.getCompilations()).isEmpty();
    }

    @Test
    void getGroupDetail_notMember_statusNull() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Group group = group(groupId);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(compilationRepository.findByGroupId(groupId)).thenReturn(List.of());
        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, userId))
                .thenReturn(Optional.empty());

        GroupDetailResponse result = groupService.getGroupDetail(groupId, userId);

        assertThat(result.getMemberStatus()).isNull();
    }

    @Test
    void getGroupDetail_groupNotFound_throws() {
        UUID groupId = UUID.randomUUID();
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.getGroupDetail(groupId, UUID.randomUUID()))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void joinGroup_success() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        when(groupAccountRepository.existsByIdGroupIdAndIdAccountId(groupId, userId)).thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(user(userId));
        when(groupRepository.getReferenceById(groupId)).thenReturn(group(groupId));

        groupService.joinGroup(userId, groupId);

        verify(groupAccountRepository).save(argThat(ga -> ga.getStatus() == GroupMemberStatus.MEMBER));
    }

    @Test
    void joinGroup_alreadyMember_throws() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        when(groupAccountRepository.existsByIdGroupIdAndIdAccountId(groupId, userId)).thenReturn(true);

        assertThatThrownBy(() -> groupService.joinGroup(userId, groupId))
                .isInstanceOf(AlreadyGroupMemberException.class);
    }

    @Test
    void leaveGroup_memberLeaves() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, userId))
                .thenReturn(Optional.of(GroupMemberStatus.MEMBER));

        groupService.leaveGroup(userId, groupId);

        verify(groupAccountRepository).deleteByIdGroupIdAndIdAccountId(groupId, userId);
    }

    @Test
    void leaveGroup_creatorCannotLeave_throws() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, userId))
                .thenReturn(Optional.of(GroupMemberStatus.CREATOR));

        assertThatThrownBy(() -> groupService.leaveGroup(userId, groupId))
                .isInstanceOf(GroupCreatorCannotLeaveException.class);
    }

    @Test
    void leaveGroup_notMember_throws() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        when(groupAccountRepository.findStatusByGroupIdAndAccountId(groupId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.leaveGroup(userId, groupId))
                .isInstanceOf(NotGroupMemberException.class);
    }
}
