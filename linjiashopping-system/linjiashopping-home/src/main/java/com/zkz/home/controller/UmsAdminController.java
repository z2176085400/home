package com.zkz.home.controller;


import com.zkz.common.api.CommonPage;
import com.zkz.common.api.CommonResult;
import com.zkz.common.api.config.ProjectConfig;
import com.zkz.common.api.util.IdGenerator;
import com.zkz.common.api.util.JedisUtil;
import com.zkz.dto.Menu;
import com.zkz.entity.UmsAdmin;
import com.zkz.entity.UmsPermission;
import com.zkz.entity.UmsRole;
import com.zkz.home.service.UserService;
import com.zkz.home.util.JwtTokenUtil;
import com.zkz.param.UmsAdminParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Multipart;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;

/**
 * 后台用户管理
 */
@Controller
@Api(tags = "UmsAdminController", description = "后台用户管理")
@RequestMapping("/admin")
@CrossOrigin
public class UmsAdminController {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserService userService;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Autowired
    private JedisUtil jedisUtil;
  /*  @Autowired
    private UmsRoleService roleService;*/

    private static final Logger log = LoggerFactory.getLogger(UmsAdminController.class);

    @Autowired
    private IdGenerator idGenerator;

    @Multipart
    @ApiOperation(value = "用户注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody

    public CommonResult<UmsAdmin> register(@RequestParam("username") String username, @RequestParam(value = "nickname", required = false) String nickname,
                                           @RequestParam("password") String password, @RequestParam(value = "email", required = false) String email, @RequestParam(value = "note", required = false) String note, Long[] id) {
        try {
            log.info("-----------发送短信或者邮箱验证码接口请求参数-----------：username:" + username + ";  password:" + password + ":  nickname:" + nickname + ":  email:" + email + ":  note:" + note + "; ids[]" + Arrays.toString(id));
            UmsAdminParam umsAdminParam = new UmsAdminParam();
            umsAdminParam.setEmail(email);
            umsAdminParam.setNickName(nickname);
            umsAdminParam.setPassword(password);
            umsAdminParam.setUsername(username);
            umsAdminParam.setNote(note);
            UmsAdmin umsAdmin = userService.register(umsAdminParam);

            if (umsAdmin == null && umsAdmin.getId() == 0 && id == null) {
                CommonResult.failed();
            }

            Long adminId = umsAdmin.getId();
            /*   roleService.adminAddRole(adminId,id);*/
            return CommonResult.success(umsAdmin);

        } catch (Exception e) {
            log.error("用户注册接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation(value = "登录以后返回token")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult login(@RequestParam("username") String username, @RequestParam("password") String password) {
        try {
            log.info("-----------登录接口请求参数-----------：username" + username + ";  password:" + password);
            if (jedisUtil.exists(ProjectConfig.USERSD + username)) {
                return CommonResult.validateFailed("账户锁定，请等待" + (jedisUtil.ttl(ProjectConfig.USERSD + username) / 60) + "分钟后再试");
            }
            String token = userService.login(username, password);
            if (token == null) {
                String key = ProjectConfig.USERLOGINCOUNT + username;
                Set<String> set = jedisUtil.keys(key + "*");
                if (set.size() > 3) {
                    jedisUtil.setex(ProjectConfig.USERSD + username, 3600, "10分钟连续失败三次冻结账号");
                    return CommonResult.validateFailed("10分钟连续失败三次冻结账号");
                }
                return CommonResult.validateFailed("用户名或密码错误");
            }

            //将用户令牌+用户名存入rides中。设置3分钟后失效
            jedisUtil.setex(ProjectConfig.TOKENPHONE + username, 180, token);

            jedisUtil.setex(ProjectConfig.TOKENJWT + token, 180, username);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", token);
            tokenMap.put("tokenHead", tokenHead);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "Basic" + token);

            return CommonResult.success(tokenMap);
        } catch (Exception e) {
            log.error("登陆接口异常");
            return CommonResult.failed();
        }

    }

    @Multipart
    @ApiOperation(value = "刷新token")
    @RequestMapping(value = "/token/refresh", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult refreshToken(HttpServletRequest request) {
        try {
            String token = request.getHeader(tokenHeader);
            String refreshToken = userService.refreshToken(token);
            if (refreshToken == null) {
                return CommonResult.failed();
            }
            String username = jwtTokenUtil.getUserNameFromToken(refreshToken);
            //将用户令牌+用户名存入rides中。设置3分钟后失效
            jedisUtil.setex(ProjectConfig.TOKENPHONE + username, 180, token);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", refreshToken);
            tokenMap.put("tokenHead", tokenHead);

            return CommonResult.success(tokenMap);
        } catch (Exception e) {
            log.error("刷新token接口异常");
            return CommonResult.failed();
        }

    }


    @ApiOperation(value = "获取当前登录用户信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult getAdminInfo(Principal principal) {
        try {
            String username = principal.getName();
            UmsAdmin umsAdmin = userService.getAdminByUsername(username);
            Map<String, Object> data = new HashMap<>();
            data.put("username", umsAdmin.getUsername());
            data.put("roles", new String[]{"TEST"});
            data.put("icon", umsAdmin.getIcon());
            return CommonResult.success(data);
        } catch (Exception e) {
            log.error("获取当前登陆用户信息接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation(value = "登出功能")
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult logout() {
        return CommonResult.success(null);
    }

    @ApiOperation("根据用户名或姓名分页获取用户列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<UmsAdmin>> list(@RequestParam(value = "name", required = false) String name,
                                                   @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        try {
            log.info("-----------根据用户名或姓名分页获取用户列表接口请求参数-----------：name:" + name + ";  pageSize:" + pageSize + ":  pageNum:" + pageNum);
            List<UmsAdmin> adminList = userService.list(name, pageSize, pageNum);
            return CommonResult.success(CommonPage.restPage(adminList));
        } catch (Exception e) {
            log.error("获取当前登陆用户信息接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation("获取指定用户信息")
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UmsAdmin> getItem(Integer id) {
        try {
            log.info("-----------获取指定用户信息接口请求参数-----------：id:" + id);
            UmsAdmin admin = userService.getItem(id);
            return CommonResult.success(admin);
        } catch (Exception e) {
            log.error("获取指定用户信息接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation("修改指定用户信息")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(Long id, @RequestParam(value = "username", required = false) String username, @RequestParam(value = "nickname", required = false) String nickname,
                               @RequestParam(value = "email", required = false) String email, @RequestParam(value = "note", required = false) String note, @RequestParam(value = "ids", required = false) Long[] ids) {
        try {
            log.info("-----------修改指定用户信息接口请求参数-----------：username:" + username + ":  nickname:" + nickname + ":  email:" + email + ":  note:" + note + "; ids[]" + Arrays.toString(ids));
            int count = -1;
            UmsAdmin umsAdmin = new UmsAdmin();
            if (username == null && nickname == null && email == null && note == null) {
                count = userService.update(id, null, ids);
            } else {

                umsAdmin.setEmail(email);
                umsAdmin.setNickName(nickname);
                umsAdmin.setNote(note);

                umsAdmin.setPassword(null);
                umsAdmin.setUsername(username);
                count = userService.update(id, umsAdmin, ids);
            }
            if (count > 0) {
                return CommonResult.success(count);
            }
            return CommonResult.failed();
        } catch (Exception e) {
            log.error("修改指定用户信息接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation("删除指定用户信息")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@PathVariable Long id) {
        try {
            log.info("-----------删除指定用户信息接口请求参数-----------：id:" + id);
            int count = userService.delete(id);
            if (count > 0) {
                return CommonResult.success(count);
            }
            return CommonResult.failed();
        } catch (Exception e) {
            log.error("获取当前登陆用户信息接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation("给用户分配角色")
    @RequestMapping(value = "/role/update", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateRole(@RequestParam("adminId") Long adminId,
                                   @RequestParam("roleIds") List<Long> roleIds) {
        try {
            log.info("-----------给用户分配角色接口请求参数-----------：adminId:" + adminId + ";  roles" + roleIds.toArray());
        } catch (Exception e) {
            log.error("获取当前登陆用户信息接口异常");
            return CommonResult.failed();
        }
            /*int count = userService.updateRole(adminId, roleIds);
            if (count >= 0) {
                return CommonResult.success(count);
            }*/
        return CommonResult.failed();
    }

    @ApiOperation("获取指定用户的角色")
    @RequestMapping(value = "/role/{adminId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsRole>> getRoleList(@PathVariable Long adminId) {
        try {
            log.info("-----------获取指定用户的角色-----------：adminId:" + adminId);
            List<UmsRole> roleList = userService.getRoleList(adminId);
            return CommonResult.success(roleList);
        } catch (Exception e) {
            log.error("获取当前登陆用户信息接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation("给用户分配+-权限")
    @RequestMapping(value = "/permission/update", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updatePermission(@RequestParam Long adminId,
                                         @RequestParam("permissionIds") List<Long> permissionIds) {
        try {
            log.info("-----------给用户分配+-权限接口请求参数-----------：adminId:" + adminId + ";  permissionIds" + permissionIds.toArray());
            int count = userService.updatePermission(adminId, permissionIds);
            if (count > 0) {
                return CommonResult.success(count);
            }
            return CommonResult.failed();
        } catch (Exception e) {
            log.error("给用户分配+-权限接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation("通过id获取用户所有权限（包括+-权限）")
    @RequestMapping(value = "/permission/{adminId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsPermission>> getPermissionList(@PathVariable Long adminId) {

        try {
            log.info("-----------通过id获取用户所有权限（包括+-权限）接口请求参数-----------：adminId:" + adminId);
            List<UmsPermission> permissionList = userService.getPermissionList(adminId);
            return CommonResult.success(permissionList);
        } catch (Exception e) {
            log.error("获取当前登陆用户信息接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation("通过token获取登陆用户所有权限（包括+-权限）")
    @RequestMapping(value = "/permission", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsPermission>> getPermissionListByToken1(Principal principal) {
        try {

            String username = principal.getName();
            UmsAdmin umsAdmin = userService.getAdminByUsername(username);
            List<UmsPermission> menuList = userService.getPermissionList(umsAdmin.getId());

            return CommonResult.success(menuList);

        } catch (Exception e) {
            log.error("通过token获取登陆用户所有权限（包括+-权限）信息接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation("获取用户所有权限（包括+-权限）")
    @RequestMapping(value = "/permissiona", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<Menu>> getPermissionListByToken(Principal principal) {
        try {
            String username = principal.getName();
            UmsAdmin umsAdmin = userService.getAdminByUsername(username);
            List<Menu> menuList = userService.getPermissionList1(umsAdmin.getId());

            return CommonResult.success(menuList);
        } catch (Exception e) {
            log.error("获取登陆用户所有权限（包括+-权限）信息接口异常");
            return CommonResult.failed();
        }

    }

    @ApiOperation("获取所有用户（包括+-角色）")
    @RequestMapping(value = "/allAdmin", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Map<String, Object>> getAllAdmin(@RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                         @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        try {
            log.info("-----------获取所有用户（包括+-角色接口请求参数-----------：pageSize:" + pageSize + ";  pageNum:" + pageNum);
            Map<String, Object> allAdmin = userService.getAllAdmin(pageSize, pageNum);

            return CommonResult.success(allAdmin);
        } catch (Exception e) {
            log.error("获取所有用户（包括+-角色接口异常");
            return CommonResult.failed();
        }


    }

    @ApiOperation("获取用户名是否可登陆")
    @RequestMapping(value = "/chickUserName", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult chickUserName(@RequestParam("username") String username) {
        if (null == username || "".equals(username)) {
            return CommonResult.validateFailed();
        }

        return CommonResult.success(userService.chickUSerName(username));


    }
}


