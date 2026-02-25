package org.linlinjava.litemall.db.service;

import org.linlinjava.litemall.db.dao.LitemallPermissionMapper;
import org.linlinjava.litemall.db.dao.LitemallRoleMapper;
import org.linlinjava.litemall.db.domain.LitemallPermission;
import org.linlinjava.litemall.db.domain.LitemallPermissionExample;
import org.linlinjava.litemall.db.domain.LitemallRole;
import org.linlinjava.litemall.db.domain.LitemallRoleExample;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class LitemallPermissionService {
    @Resource
    private LitemallPermissionMapper permissionMapper;
    @Resource
    private LitemallRoleMapper roleMapper;

    public Set<String> queryByRoleIds(Integer[] roleIds) {
        Set<String> permissions = new HashSet<String>();
        if(roleIds.length == 0){
            return permissions;
        }

        LitemallPermissionExample example = new LitemallPermissionExample();
        example.or().andRoleIdIn(Arrays.asList(roleIds)).andDeletedEqualTo(false);
        List<LitemallPermission> permissionList = permissionMapper.selectByExample(example);

        for(LitemallPermission permission : permissionList){
            permissions.add(permission.getPermission());
        }

        if (hasSuperRole(Arrays.asList(roleIds))) {
            permissions.add("*");
        }
        return permissions;
    }


    public Set<String> queryByRoleId(Integer roleId) {
        Set<String> permissions = new HashSet<String>();
        if(roleId == null){
            return permissions;
        }

        LitemallPermissionExample example = new LitemallPermissionExample();
        example.or().andRoleIdEqualTo(roleId).andDeletedEqualTo(false);
        List<LitemallPermission> permissionList = permissionMapper.selectByExample(example);

        for(LitemallPermission permission : permissionList){
            permissions.add(permission.getPermission());
        }

        if (hasSuperRole(Arrays.asList(roleId))) {
            permissions.add("*");
        }
        return permissions;
    }

    public Set<String> queryByRoleId(List<Integer> roleIds) {
        Set<String> permissions = new HashSet<String>();
        if(roleIds == null || roleIds.isEmpty()){
            return permissions;
        }

        LitemallPermissionExample example = new LitemallPermissionExample();
        example.or().andRoleIdIn(roleIds).andDeletedEqualTo(false);
        List<LitemallPermission> permissionList = permissionMapper.selectByExample(example);

        for(LitemallPermission permission : permissionList){
            permissions.add(permission.getPermission());
        }

        if (hasSuperRole(roleIds)) {
            permissions.add("*");
        }
        return permissions;
    }

    public boolean checkSuperPermission(Integer roleId) {
        if(roleId == null){
            return false;
        }

        LitemallPermissionExample example = new LitemallPermissionExample();
        example.or().andRoleIdEqualTo(roleId).andPermissionEqualTo("*").andDeletedEqualTo(false);
        if (permissionMapper.countByExample(example) != 0) {
            return true;
        }
        LitemallRole role = roleMapper.selectByPrimaryKey(roleId);
        return isSuperRole(role);
    }

    public boolean checkSuperPermission(List<Integer> roleIds) {
        if(roleIds == null || roleIds.isEmpty()){
            return false;
        }

        LitemallPermissionExample example = new LitemallPermissionExample();
        example.or().andRoleIdIn(roleIds).andPermissionEqualTo("*").andDeletedEqualTo(false);
        if (permissionMapper.countByExample(example) != 0) {
            return true;
        }
        return hasSuperRole(roleIds);
    }

    public void deleteByRoleId(Integer roleId) {
        LitemallPermissionExample example = new LitemallPermissionExample();
        example.or().andRoleIdEqualTo(roleId).andDeletedEqualTo(false);
        permissionMapper.logicalDeleteByExample(example);
    }

    public void add(LitemallPermission litemallPermission) {
        litemallPermission.setAddTime(LocalDateTime.now());
        litemallPermission.setUpdateTime(LocalDateTime.now());
        permissionMapper.insertSelective(litemallPermission);
    }

    private boolean hasSuperRole(List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }
        LitemallRoleExample example = new LitemallRoleExample();
        example.or().andIdIn(roleIds).andEnabledEqualTo(true).andDeletedEqualTo(false);
        List<LitemallRole> roles = roleMapper.selectByExample(example);
        for (LitemallRole role : roles) {
            if (isSuperRole(role)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSuperRole(LitemallRole role) {
        if (role == null) {
            return false;
        }
        String name = role.getName();
        return "超级管理员".equals(name) || "超级管理".equals(name);
    }
}
