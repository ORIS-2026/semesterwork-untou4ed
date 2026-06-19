package ru.itis.services.searchService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.dto.group.response.GroupResponse;
import ru.itis.dto.search.SearchResponse;
import ru.itis.dto.user.response.UserResponse;
import ru.itis.mappers.GroupMapper;
import ru.itis.mappers.UserMapper;
import ru.itis.repositories.GroupRepository;
import ru.itis.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;

    @Override
    public SearchResponse search(String query) {
        List<UserResponse> users = userRepository.searchUsers(query)
                .stream()
                .map(userMapper::toUserResponse)
                .toList();

        List<GroupResponse> groups = groupRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(groupMapper::toResponse)
                .toList();

        return SearchResponse.builder()
                .users(users)
                .groups(groups)
                .build();
    }
}
