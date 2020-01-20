package com.zkz.home.dao;

import com.zkz.entity.UmsAdminPermissionRelation;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UmsAdminPermissionRelationMapper extends Mapper<UmsAdminPermissionRelation> {
    int insertList(@Param("list") List<UmsAdminPermissionRelation> list);
}