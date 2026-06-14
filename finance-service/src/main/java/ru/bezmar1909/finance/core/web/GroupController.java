package ru.bezmar1909.finance.core.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.bezmar1909.finance.core.service.GroupService;
import ru.bezmar1909.finance.core.web.dto.AddMemberRequest;
import ru.bezmar1909.finance.core.web.dto.CreateGroupRequest;
import ru.bezmar1909.finance.core.web.dto.GroupResponse;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse create(@Valid @RequestBody CreateGroupRequest request, Authentication authentication) {
        return GroupResponse.from(groupService.create(request.name(), CurrentUser.id(authentication)));
    }

    @GetMapping
    public List<GroupResponse> list(Authentication authentication) {
        return groupService.list(CurrentUser.id(authentication)).stream()
                .map(GroupResponse::from)
                .toList();
    }

    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupPermissions.canManageMembers(#groupId, authentication)")
    public void addMember(@PathVariable Long groupId, @Valid @RequestBody AddMemberRequest request, Authentication authentication) {
        groupService.addMember(groupId, CurrentUser.id(authentication), CurrentUser.role(authentication), request.userId());
    }
}
