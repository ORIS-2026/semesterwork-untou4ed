package ru.itis.services.groupService;

import ru.itis.dto.group.response.GroupDetailResponse;
import ru.itis.dto.group.response.GroupResponse;

import java.util.List;
import java.util.UUID;

public interface GroupService {

    List<GroupResponse> findGroupsByUserId(UUID userId);

    GroupResponse createGroup(String name, String description, UUID creatorId);

    GroupDetailResponse getGroupDetail(UUID groupId, UUID currentUserId);

    void joinGroup(UUID userId, UUID groupId);

    void leaveGroup(UUID userId, UUID groupId);
}
