package com.tuya.iot.suite.web.config;

import com.google.common.collect.Lists;
import com.tuya.iot.suite.ability.idaas.ability.GrantAbility;
import com.tuya.iot.suite.ability.idaas.ability.IdaasUserAbility;
import com.tuya.iot.suite.ability.idaas.ability.PermissionAbility;
import com.tuya.iot.suite.ability.idaas.ability.RoleAbility;
import com.tuya.iot.suite.ability.idaas.ability.SpaceAbility;
import com.tuya.iot.suite.ability.idaas.model.IdaasRole;
import com.tuya.iot.suite.ability.idaas.model.IdaasRoleCreateReq;
import com.tuya.iot.suite.ability.idaas.model.IdaasUser;
import com.tuya.iot.suite.ability.idaas.model.IdaasUserCreateReq;
import com.tuya.iot.suite.ability.idaas.model.PermissionCreateReq;
import com.tuya.iot.suite.ability.idaas.model.PermissionQueryByRolesReq;
import com.tuya.iot.suite.ability.idaas.model.PermissionQueryByRolesRespItem;
import com.tuya.iot.suite.ability.idaas.model.RoleGrantPermissionsReq;
import com.tuya.iot.suite.ability.idaas.model.RoleRevokePermissionsReq;
import com.tuya.iot.suite.ability.idaas.model.SpaceApplyReq;
import com.tuya.iot.suite.ability.idaas.model.UserGrantRoleReq;
import com.tuya.iot.suite.service.util.PermTemplateUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author benguan.zhou@tuya.com
 * @description
 * @date 2021/06/02
 */
@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IotSuiteServerAppRunner implements ApplicationRunner {
    @Autowired
    ProjectProperties projectProperties;
    @Autowired
    SpaceAbility spaceAbility;
    @Autowired
    RoleAbility roleAbility;
    @Autowired
    IdaasUserAbility idaasUserAbility;

    @Autowired
    GrantAbility grantAbility;

    @Autowired
    PermissionAbility permissionAbility;

    String adminUid = "";
    String adminRoleCode = "admin";
    String manageUid = "";
    String manageRoleCode = "manage-1000";

    /**
     *
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!initPermissionSpace()) {
            log.error("apply space failure!");
            return;
        }
        if (!initRole(adminRoleCode)) {
            log.error("init role(admin) failure!");
            return;
        }

        if (!initUser(adminUid,adminRoleCode)) {
            log.error("init user(admin) failure!");
            return;
        }
        List<PermissionCreateReq> adminPermissions = PermTemplateUtil.loadAsPermissionCreateReqList("classpath:template/permissions-admin.json");

        if (!initPermissions(adminPermissions)) {
            log.error("init permissions failure!");
            return;
        }

        if (!grantRoleToUser(adminRoleCode,adminUid)) {
            log.error("grant role(admin) to user(admin) failure!");
            return;
        }


        if(!grantPermissionsToRole(adminRoleCode, adminPermissions)){
            log.error("grant permissions to role(admin) failure!");
            return;
        }

        if(!initRole(manageRoleCode)){
            log.error("init role(manage) failure!");
            return;
        }

        if(!initUser(manageUid,manageRoleCode)){
            log.error("init user(manage) failure!");
            return;
        }

        if (!grantRoleToUser(manageRoleCode,manageUid)) {
            log.error("grant role(manage) to user(manage) failure!");
            return;
        }

        List<PermissionCreateReq> managePermissions = PermTemplateUtil.loadAsPermissionCreateReqList("classpath:template/permissions-manage.json");

        if(!grantPermissionsToRole(manageRoleCode,managePermissions)){
            log.error("grant permissions to role(manage) failure!");
            return;
        }
        log.info("permission data has been initialized successful!");
    }

    private boolean initPermissions(List<PermissionCreateReq> perms) {
        Long spaceId = projectProperties.getPermissionSpaceId();
        Map<String,PermissionCreateReq> allPerms = perms.stream().collect(Collectors.toMap(it->it.getPermissionCode(),it->it));
        Map<String,PermissionCreateReq> ownedPerms = permissionAbility.queryPermissionsByUser(spaceId,adminUid).stream()
                .map(it->
                        PermissionCreateReq.builder()
                                .name(it.getName())
                                .permissionCode(it.getPermissionCode())
                                .type(it.getType())
                                .order(it.getOrder())
                                .remark(it.getRemark())
                                .parentCode(it.getParentCode())
                                .build())
                .collect(Collectors.toMap(it->it.getPermissionCode(),it->it));
        Map<String,PermissionCreateReq> toAdd = new HashMap<>(16);
        Map<String,PermissionCreateReq> toDelete = new HashMap<>(16);

        toAdd.putAll(allPerms);
        ownedPerms.forEach((code,it)->toAdd.remove(code));
        if(!toAdd.isEmpty()){
            boolean addResult = permissionAbility.batchCreatePermission(spaceId,toAdd.values());
            if(!addResult){
                log.error("add permission error!");
                return false;
            }
        }

        toDelete.putAll(ownedPerms);
        allPerms.forEach((code,it)->toDelete.remove(code));
        if(!toDelete.isEmpty()){
            for(PermissionCreateReq p : toDelete.values()){
                boolean deleteResult = permissionAbility.deletePermission(spaceId,p.getPermissionCode());
                if(!deleteResult){
                    log.error("delete permission error, permissionCode="+p.getPermissionCode());
                    return false;
                }
            }
        }
        return true;
    }

    private boolean grantPermissionsToRole(String roleCode,List<PermissionCreateReq> perms) {
        Long spaceId = projectProperties.getPermissionSpaceId();
        List<PermissionQueryByRolesRespItem> existsPermList = permissionAbility.queryPermissionsByRoleCodes(PermissionQueryByRolesReq.builder()
                .spaceId(spaceId)
                .roleCodes(Lists.newArrayList(roleCode)).build());
        Set<String> allPerms = perms.stream().map(it->it.getPermissionCode()).collect(Collectors.toSet());
        Set<String> existsPerms = existsPermList.stream().flatMap(it->it.getPermissionList().stream().map(p->p.getPermissionCode())).collect(
                Collectors.toSet());
        Set<String> toAdd = new HashSet<>();
        toAdd.addAll(allPerms);
        toAdd.removeAll(existsPerms);
        if(!toAdd.isEmpty()){
            boolean addResult = grantAbility.grantPermissionsToRole(RoleGrantPermissionsReq.builder()
                    .spaceId(spaceId)
                    .roleCode(roleCode)
                    .permissionCodes(Lists.newArrayList(toAdd))
                    .build());
            if(!addResult){
                log.error("add permissions to role error!");
                return false;
            }
        }

        Set<String> toDelete = new HashSet<>();
        toDelete.addAll(existsPerms);
        toDelete.removeAll(allPerms);
        if(!toDelete.isEmpty()){
            boolean revokeResult = grantAbility.revokePermissionsFromRole(RoleRevokePermissionsReq.builder()
                    .spaceId(spaceId)
                    .roleCode(roleCode)
                    .permissionCodes(Lists.newArrayList(toDelete))
                    .build());
            if(!revokeResult){
                log.error("revoke permissions from role error!");
                return false;
            }
        }
        return true;
    }

    private boolean grantRoleToUser(String roleCode,String uid) {
        Long spaceId = projectProperties.getPermissionSpaceId();
        List<IdaasRole> roles = roleAbility.queryRolesByUser(spaceId,uid);
        Optional<IdaasRole> op = roles.stream().filter(it->it.getRoleCode().equals(roleCode)).findAny();
        if(op.isPresent()){
            return true;
        }
        return grantAbility.grantRoleToUser(UserGrantRoleReq.builder()
                .spaceId(spaceId)
                .roleCode(roleCode)
                .uid(uid)
                .build());
    }

    private boolean initUser(String uid,String username) {
        Long spaceId = projectProperties.getPermissionSpaceId();
        IdaasUser adminUser = idaasUserAbility.getUserByUid(spaceId,uid);
        if(adminUser!=null){
            return true;
        }
        return idaasUserAbility.createUser(spaceId, IdaasUserCreateReq.builder()
                .username(username)
                .uid(uid)
                .remark(username)
                .build());
    }

    private boolean initRole(String roleCode) {
        Long spaceId = projectProperties.getPermissionSpaceId();
        IdaasRole adminRole = roleAbility.getRole(spaceId,roleCode);
        if(adminRole!=null){
            return true;
        }
        return roleAbility.createRole(spaceId, IdaasRoleCreateReq.builder()
                .roleCode(roleCode)
                .roleName(roleCode)
                .remark(roleCode)
                .build()
        );
    }

    private boolean initPermissionSpace() {
        // if spaceId has config, use it.
        Long spaceId = projectProperties.getPermissionSpaceId();
        if (spaceId != null) {
            log.info("project.permission-space-id={}", spaceId);
            return true;
        }
        // else query spaceId.
        spaceId = spaceAbility.querySpace(projectProperties.getPermissionGroup(), projectProperties.getPermissionSpaceCode());
        if (spaceId != null) {
            projectProperties.setPermissionSpaceId(spaceId);
            log.info("exists spaceId {} at iot-cloud", spaceId);
            return true;
        }
        // else apply a spaceId.
        spaceId = spaceAbility.applySpace(SpaceApplyReq.builder()
                .spaceGroup(projectProperties.getPermissionGroup())
                .spaceCode(projectProperties.getPermissionSpaceCode()).build());
        if (spaceId != null) {
            projectProperties.setPermissionSpaceId(spaceId);
            log.info("applied spaceId: {}", spaceId);
            return true;
        }
        //throw new RuntimeException("apply space failure!");
        return false;
    }

}
