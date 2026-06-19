package ru.itis.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itis.dto.group.response.GroupResponse;
import ru.itis.dto.search.SearchResponse;
import ru.itis.dto.user.response.UserResponse;
import ru.itis.entities.Group;
import ru.itis.entities.User;
import ru.itis.mappers.GroupMapper;
import ru.itis.mappers.UserMapper;
import ru.itis.repositories.GroupRepository;
import ru.itis.repositories.UserRepository;
import ru.itis.services.searchService.SearchServiceImpl;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock UserRepository userRepository;
    @Mock GroupRepository groupRepository;
    @Mock UserMapper userMapper;
    @Mock GroupMapper groupMapper;
    @InjectMocks SearchServiceImpl searchService;

    private User user(UUID id, String username) {
        return User.builder().id(id).username(username).name("N").enabled(true).phoneNumber("+7900").build();
    }

    private Group group(UUID id, String name) {
        return Group.builder().id(id).name(name).build();
    }

    @Test
    void search_returnsUsersAndGroups() {
        String query = "тест";
        UUID uid = UUID.randomUUID();
        UUID gid = UUID.randomUUID();
        User user = user(uid, "tester");
        Group group = group(gid, "Тестовая группа");
        UserResponse userResp = UserResponse.builder().id(uid).username("tester").build();
        GroupResponse groupResp = GroupResponse.builder().id(gid).name("Тестовая группа").build();

        when(userRepository.searchUsers(query)).thenReturn(List.of(user));
        when(groupRepository.findByNameContainingIgnoreCase(query)).thenReturn(List.of(group));
        when(userMapper.toUserResponse(user)).thenReturn(userResp);
        when(groupMapper.toResponse(group)).thenReturn(groupResp);

        SearchResponse result = searchService.search(query);

        assertThat(result.getUsers()).hasSize(1);
        assertThat(result.getUsers().get(0).getUsername()).isEqualTo("tester");
        assertThat(result.getGroups()).hasSize(1);
        assertThat(result.getGroups().get(0).getName()).isEqualTo("Тестовая группа");
    }

    @Test
    void search_noUsersFound_returnsEmptyUserList() {
        String query = "nomatch";

        when(userRepository.searchUsers(query)).thenReturn(List.of());
        when(groupRepository.findByNameContainingIgnoreCase(query)).thenReturn(List.of());

        SearchResponse result = searchService.search(query);

        assertThat(result.getUsers()).isEmpty();
        assertThat(result.getGroups()).isEmpty();
    }

    @Test
    void search_onlyUsersFound() {
        String query = "ivan";
        UUID uid = UUID.randomUUID();
        User user = user(uid, "ivan123");
        UserResponse userResp = UserResponse.builder().id(uid).username("ivan123").build();

        when(userRepository.searchUsers(query)).thenReturn(List.of(user));
        when(groupRepository.findByNameContainingIgnoreCase(query)).thenReturn(List.of());
        when(userMapper.toUserResponse(user)).thenReturn(userResp);

        SearchResponse result = searchService.search(query);

        assertThat(result.getUsers()).hasSize(1);
        assertThat(result.getGroups()).isEmpty();
    }

    @Test
    void search_onlyGroupsFound() {
        String query = "группа";
        UUID gid = UUID.randomUUID();
        Group group = group(gid, "Моя группа");
        GroupResponse groupResp = GroupResponse.builder().id(gid).name("Моя группа").build();

        when(userRepository.searchUsers(query)).thenReturn(List.of());
        when(groupRepository.findByNameContainingIgnoreCase(query)).thenReturn(List.of(group));
        when(groupMapper.toResponse(group)).thenReturn(groupResp);

        SearchResponse result = searchService.search(query);

        assertThat(result.getUsers()).isEmpty();
        assertThat(result.getGroups()).hasSize(1);
        assertThat(result.getGroups().get(0).getName()).isEqualTo("Моя группа");
    }

    @Test
    void search_multipleResults() {
        String query = "test";
        UUID uid1 = UUID.randomUUID();
        UUID uid2 = UUID.randomUUID();
        User u1 = user(uid1, "test_user1");
        User u2 = user(uid2, "test_user2");
        UserResponse r1 = UserResponse.builder().id(uid1).username("test_user1").build();
        UserResponse r2 = UserResponse.builder().id(uid2).username("test_user2").build();

        when(userRepository.searchUsers(query)).thenReturn(List.of(u1, u2));
        when(groupRepository.findByNameContainingIgnoreCase(query)).thenReturn(List.of());
        when(userMapper.toUserResponse(u1)).thenReturn(r1);
        when(userMapper.toUserResponse(u2)).thenReturn(r2);

        SearchResponse result = searchService.search(query);

        assertThat(result.getUsers()).hasSize(2);
    }
}
