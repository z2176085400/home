package com.zkz.home.dao;

import com.zkz.dto.UserDto;
import com.zkz.entity.UmsAdmin;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UmsAdminMapper extends Mapper<UmsAdmin> {
    List<UserDto> getAllAdmin();

    UserDto getAdminById(Integer id);
}