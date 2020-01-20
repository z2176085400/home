package com.zkz.dto;

import com.zkz.entity.UmsPermission;
import lombok.Data;

import java.util.List;

@Data
public class Menu {
    private UmsPermission umsPermission;
    private List<UmsPermission> permissionList;

}


