package org.esupportail.esupagape.service.security;

import org.esupportail.esupagape.service.ldap.LdapGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import java.util.*;

public class CasLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

    private static final Logger logger = LoggerFactory.getLogger(CasLdapAuthoritiesPopulator.class);

    private LdapGroupService ldapGroupService;

    protected Map<String, String> mappingGroupesRoles;

    private Group2UserRoleService group2UserRoleService;

    public void setLdapGroupService(LdapGroupService ldapGroupService) {
        this.ldapGroupService = ldapGroupService;
    }

    public void setMappingGroupesRoles(Map<String, String> mappingGroupesRoles) {
        this.mappingGroupesRoles = mappingGroupesRoles;
    }

    public void setGroup2UserRoleService(Group2UserRoleService group2UserRoleService) {
        this.group2UserRoleService = group2UserRoleService;
    }

    public CasLdapAuthoritiesPopulator(ContextSource contextSource, String groupSearchBase) {
        super(contextSource, groupSearchBase);
    }

    @Override
    protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        List<String> ldapGroups = ldapGroupService.getGroups(username.toLowerCase());
        List<String> roles = new ArrayList<>(group2UserRoleService.getRoles(username.toLowerCase()));
        for (String role : roles) {
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role);
            grantedAuthorities.add(simpleGrantedAuthority);
            logger.debug("loading authorities : " + simpleGrantedAuthority.getAuthority());
        }
        ldapGroupService.addLdapRoles(grantedAuthorities, ldapGroups, mappingGroupesRoles);
        return grantedAuthorities;
    }

}
