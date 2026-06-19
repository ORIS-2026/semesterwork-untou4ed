package ru.itis.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.api.GroupApi;
import ru.itis.dto.group.request.CreateGroupRequest;
import ru.itis.dto.group.response.GroupDetailResponse;
import ru.itis.dto.group.response.GroupResponse;
import ru.itis.services.groupService.GroupService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GroupController implements GroupApi {

    private final GroupService groupService;

    @Override
    public List<GroupResponse> getMyGroups(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return groupService.findGroupsByUserId(userId);
    }

    @Override
    public ResponseEntity<GroupResponse> createGroup(Jwt jwt, CreateGroupRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        GroupResponse group = groupService.createGroup(request.getName(), request.getDescription(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @Override
    public GroupDetailResponse getGroup(Jwt jwt, UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return groupService.getGroupDetail(groupId, userId);
    }

    @Override
    public void joinGroup(Jwt jwt, UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        groupService.joinGroup(userId, groupId);
    }

    @Override
    public void leaveGroup(Jwt jwt, UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        groupService.leaveGroup(userId, groupId);
    }
}
