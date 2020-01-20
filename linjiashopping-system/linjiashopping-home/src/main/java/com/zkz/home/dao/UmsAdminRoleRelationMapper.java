package com.zkz.home.dao;

import com.zkz.dto.Menu;
import com.zkz.entity.UmsAdminRoleRelation;
import com.zkz.entity.UmsPermission;
import com.zkz.entity.UmsRole;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UmsAdminRoleRelationMapper extends Mapper<UmsAdminRoleRelation> {

    /**
     * 批量插入用户角色关系
     */
    int insertList(@Param("list") List<UmsAdminRoleRelation> adminRoleRelationList);

    /**
     * 获取用于所有角色
     */
    List<UmsRole> getRoleList(@Param("adminId") Long adminId);

    /**
     * 获取用户所有角色权限
     */
    List<UmsPermission> getRolePermissionList(@Param("adminId") Long adminId);

    /**
     * 获取用户所有权限(包括+-权限)
     */
    List<UmsPermission> getPermissionList(@Param("adminId") Long adminId);

    List<Menu> getPermissionList1(@Param("adminId") Long adminId);

}