package ru.itis.services.groupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.dto.compilation.response.CompilationResponse;
import ru.itis.dto.group.response.GroupDetailResponse;
import ru.itis.dto.group.response.GroupResponse;
import ru.itis.entities.Group;
import ru.itis.entities.GroupAccount;
import ru.itis.entities.GroupAccountId;
import ru.itis.entities.GroupMemberStatus;
import ru.itis.entities.User;
import ru.itis.exceptions.AlreadyGroupMemberException;
import ru.itis.exceptions.GroupCreatorCannotLeaveException;
import ru.itis.exceptions.GroupNotFoundException;
import ru.itis.exceptions.NotGroupMemberException;
import ru.itis.mappers.CompilationMapper;
import ru.itis.mappers.GroupMapper;
import ru.itis.repositories.CompilationRepository;
import ru.itis.repositories.GroupAccountRepository;
import ru.itis.repositories.GroupRepository;
import ru.itis.repositories.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupAccountRepository groupAccountRepository;
    private final CompilationRepository compilationRepository;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public GroupResponse createGroup(String name, String description, UUID creatorId) {
        Group group = Group.builder()
                .name(name)
                .description(description)
                .build();
        group = groupRepository.save(group);

        User creator = userRepository.getReferenceById(creatorId);
        GroupAccount creatorAccount = GroupAccount.builder()
                .id(new GroupAccountId(group.getId(), creatorId))
                .group(group)
                .account(creator)
                .status(GroupMemberStatus.CREATOR)
                .build();
        groupAccountRepository.save(creatorAccount);

        log.info("Создана группа '{}', creatorId={}", name, creatorId);
        return groupMapper.toResponse(group);
    }

    @Override
    public List<GroupResponse> findGroupsByUserId(UUID userId) {
        return groupRepository.findGroupsByUserId(userId)
                .stream()
                .map(groupMapper::toResponse)
                .toList();
    }

    @Override
    public GroupDetailResponse getGroupDetail(UUID groupId, UUID currentUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Группа не найдена"));

        List<CompilationResponse> compilations = compilationRepository.findByGroupId(groupId)
                .stream()
                .map(compilationMapper::toResponse)
                .toList();

        String memberStatus = groupAccountRepository
                .findStatusByGroupIdAndAccountId(groupId, currentUserId)
                .map(GroupMemberStatus::getValue)
                .orElse(null);

        return GroupDetailResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdAt(group.getCreatedAt())
                .compilations(compilations)
                .memberStatus(memberStatus)
                .build();
    }

    @Override
    @Transactional
    public void joinGroup(UUID userId, UUID groupId) {
        if (groupAccountRepository.existsByIdGroupIdAndIdAccountId(groupId, userId)) {
            throw new AlreadyGroupMemberException("Вы уже состоите в этой группе");
        }

        User user = userRepository.getReferenceById(userId);
        Group group = groupRepository.getReferenceById(groupId);

        GroupAccount membership = GroupAccount.builder()
                .id(new GroupAccountId(groupId, userId))
                .group(group)
                .account(user)
                .status(GroupMemberStatus.MEMBER)
                .build();
        groupAccountRepository.save(membership);
        log.info("Пользователь {} вступил в группу {}", userId, groupId);
    }

    @Override
    @Transactional
    public void leaveGroup(UUID userId, UUID groupId) {
        GroupMemberStatus status = groupAccountRepository
                .findStatusByGroupIdAndAccountId(groupId, userId)
                .orElseThrow(() -> new NotGroupMemberException("Вы не состоите в этой группе"));

        if (status == GroupMemberStatus.CREATOR) {
            throw new GroupCreatorCannotLeaveException("Создатель не может покинуть группу");
        }

        groupAccountRepository.deleteByIdGroupIdAndIdAccountId(groupId, userId);
        log.info("Пользователь {} покинул группу {}", userId, groupId);
    }
}
