package ru.itis.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.dto.group.request.CreateGroupRequest;
import ru.itis.dto.group.response.GroupDetailResponse;
import ru.itis.dto.group.response.GroupResponse;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/groups")
@RestController
public interface GroupApi {

    @GetMapping
    List<GroupResponse> getMyGroups(@AuthenticationPrincipal Jwt jwt);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<GroupResponse> createGroup(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateGroupRequest request
    );

    @GetMapping("/{groupId}")
    GroupDetailResponse getGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId
    );

    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void joinGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId
    );

    @DeleteMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void leaveGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId
    );
}
