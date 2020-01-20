package com.zkz.dto;

import com.zkz.entity.UmsAdmin;
import com.zkz.entity.UmsRole;
import lombok.Data;

import java.util.List;

@Data
public class UserDto extends UmsAdmin {
    List<UmsRole> nameList;
}
