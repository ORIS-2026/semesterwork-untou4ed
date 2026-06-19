package ru.itis.services.compilationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.dto.compilation.response.CompilationResponse;
import ru.itis.dto.gift.GiftResponse;
import ru.itis.entities.Category;
import ru.itis.entities.Compilation;
import ru.itis.entities.Gift;
import ru.itis.entities.GiftOwnerType;
import ru.itis.entities.Group;
import ru.itis.entities.GroupMemberStatus;
import ru.itis.exceptions.CategoryNotFoundException;
import ru.itis.exceptions.GroupNotFoundException;
import ru.itis.exceptions.NotGroupMemberException;
import ru.itis.mappers.CompilationMapper;
import ru.itis.mappers.GiftMapper;
import ru.itis.repositories.CategoryRepository;
import ru.itis.repositories.CompilationRepository;
import ru.itis.repositories.GiftRepository;
import ru.itis.repositories.GroupAccountRepository;
import ru.itis.repositories.GroupRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final GroupRepository        groupRepository;
    private final CategoryRepository     categoryRepository;
    private final GroupAccountRepository groupAccountRepository;
    private final CompilationMapper      compilationMapper;
    private final GiftRepository         giftRepository;
    private final GiftMapper             giftMapper;

    @Override
    public Slice<CompilationResponse> findCompilationsFeed(UUID userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<Compilation> slice = compilationRepository.findCompilationsByUserId(userId, pageable);

        Map<UUID, List<GiftResponse>> giftsMap = loadGiftsMap(
                slice.getContent().stream().map(Compilation::getId).toList()
        );

        return slice.map(c -> compilationMapper.toResponse(c).toBuilder()
                .gifts(giftsMap.getOrDefault(c.getId(), List.of()))
                .build());
    }

    @Override
    @Transactional
    public CompilationResponse addCompilation(UUID requesterId, UUID groupId, String title, String description, int categoryId) {
        GroupMemberStatus status = groupAccountRepository
                .findStatusByGroupIdAndAccountId(groupId, requesterId)
                .orElseThrow(() -> new NotGroupMemberException("Вы не состоите в этой группе"));

        if (status != GroupMemberStatus.CREATOR && status != GroupMemberStatus.ADMIN) {
            throw new NotGroupMemberException("Только создатель или администратор могут добавлять подборки");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Группа не найдена"));
        Category category = categoryRepository.findById((long) categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));

        Compilation compilation = Compilation.builder()
                .group(group)
                .title(title)
                .description(description)
                .category(category)
                .build();

        Compilation saved = compilationRepository.save(compilation);
        log.info("Добавлена подборка '{}' в группу {} пользователем {}", title, groupId, requesterId);
        return compilationMapper.toResponse(saved).toBuilder()
                .gifts(List.of())
                .build();
    }

    // SELECT * FROM gifts WHERE owner_id IN () AND owner_type = 'COMPILATION'
    private Map<UUID, List<GiftResponse>> loadGiftsMap(List<UUID> compilationIds) {
        if (compilationIds.isEmpty()) return Map.of();
        return giftRepository
                .findByOwnerIdInAndOwnerType(compilationIds, GiftOwnerType.COMPILATION)
                .stream()
                .collect(Collectors.groupingBy(
                        Gift::getOwnerId,
                        Collectors.mapping(giftMapper::toResponse, Collectors.toList())
                ));
    }
}
