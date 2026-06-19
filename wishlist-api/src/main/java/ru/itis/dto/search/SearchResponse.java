package ru.itis.dto.search;

import lombok.Builder;
import lombok.Getter;
import ru.itis.dto.group.response.GroupResponse;
import ru.itis.dto.user.response.UserResponse;

import java.util.List;

@Getter
@Builder
public class SearchResponse {
    private List<UserResponse> users;
    private List<GroupResponse> groups;
}
