package com.zkz.home.service;

import com.zkz.dto.Menu;
import com.zkz.entity.UmsAdmin;
import com.zkz.entity.UmsPermission;
import com.zkz.entity.UmsRole;
import com.zkz.param.UmsAdminParam;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface UserService {
    /**
     * 根据用户名获取后台管理员
     */
    UmsAdmin getAdminByUsername(String username);

    /**
     * 注册功能
     */
    UmsAdmin register(UmsAdminParam umsAdminParam);

    /**
     * 登录功能
     *
     * @param username 用户名
     * @param password 密码
     * @return 生成的JWT的token
     */
    String login(String username, String password);

    /**
     * 刷新token的功能
     *
     * @param oldToken 旧的token
     */
    String refreshToken(String oldToken);

    /**
     * 根据用户id获取用户
     */
    UmsAdmin getItem(Integer id);

    /**
     * 根据用户名或昵称分页查询用户
     */
    List<UmsAdmin> list(String name, Integer pageSize, Integer pageNum);

    /**
     * 修改指定用户信息
     */
    @Transactional
    int update(Long adminId, UmsAdmin admin, Long[] ids);

    /**
     * 删除指定用户
     */
    int delete(Long id);

    /**
     * 获取用户对于角色
     */
    List<UmsRole> getRoleList(Long adminId);

    /**
     * 修改用户的+-权限
     */
    @Transactional
    int updatePermission(Long adminId, List<Long> permissionIds);

    /**
     * 获取用户所有权限（包括角色权限和+-权限）
     */
    List<UmsPermission> getPermissionList(Long adminId);

    long chickUSerName(String username);

    List<Menu> getPermissionList1(Long adminId);


    Map<String, Object> getAllAdmin(Integer pageSize, Integer pageNum);
}
