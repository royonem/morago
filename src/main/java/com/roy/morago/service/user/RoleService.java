package com.roy.morago.service.user;

import com.roy.morago.entity.user.Role;
import com.roy.morago.exception.auth.RoleNotFoundException;
import com.roy.morago.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public Role getClientRole() {
        return roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

    }
    public Role getTranslatorRole() {
        return roleRepository.findByName("ROLE_TRANSLATOR")
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

    }
    public Role getAdminRole() {
        return roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

    }
}
