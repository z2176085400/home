package com.zkz.home.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.zkz.common.api.config.ProjectConfig;
import com.zkz.common.api.util.IdGenerator;
import com.zkz.common.api.util.JedisUtil;

import com.zkz.dto.Menu;
import com.zkz.dto.UserDto;
import com.zkz.entity.*;
import com.zkz.home.component.MyPasswordEncoder;
import com.zkz.home.dao.*;


import com.zkz.home.service.UserService;
import com.zkz.home.util.JwtTokenUtil;
import com.zkz.param.UmsAdminParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UmsAdminRoleRelationMapper adminRoleRelationMapper;
    @Autowired
    private UmsAdminPermissionRelationMapper adminPermissionRelationMapper;
    @Autowired
    private UmsAdminPermissionRelationMapper adminPermissionRelationDao;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private MyPasswordEncoder passwordEncoder;
    @Autowired
    private UmsAdminMapper adminMapper;
    @Autowired
    private UmsAdminLoginLogMapper adminLoginLogMapper;
    @Autowired
    private UmsAdminRoleRelationMapper adminRoleRelationDao;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private UmsRoleMapper roleMapper;
    @Autowired
    private IdGenerator idGenerator;

    @Override
    public UmsAdmin getAdminByUsername(String username) {

        Example example = new Example(UmsAdmin.class);
        Example.Criteria criteria = example.createCriteria();
        if (StrUtil.isNotBlank(username)) {
            criteria.andEqualTo("username", username);
        }
        List<UmsAdmin> adminList = adminMapper.selectByExample(example);
        if (adminList != null && adminList.size() > 0) {
            return adminList.get(0);

        }
        return null;
    }

    @Override
    public UmsAdmin register(UmsAdminParam umsAdminParam) {
        UmsAdmin umsAdmin = new UmsAdmin();
        BeanUtils.copyProperties(umsAdminParam, umsAdmin);
        umsAdmin.setCreateTime(new Date());
        umsAdmin.setStatus(1);
        //查询是否有相同用户名的用户
        Example example = new Example(UmsAdmin.class);
        Example.Criteria criteria = example.createCriteria();

        if (StrUtil.isNotBlank(umsAdminParam.getUsername())) {
            criteria.andEqualTo("username", umsAdminParam.getUsername());
            if (adminMapper.selectByExample(example).size() > 0) {
                return null;
            }
        } else {
            return null;
        }

        //将密码进行加密操作
        String encodePassword = passwordEncoder.encode(umsAdmin.getPassword());
        umsAdmin.setPassword(encodePassword);
        adminMapper.insert(umsAdmin);
        List<UmsAdmin> adminList = adminMapper.selectByExample(example);

        return adminList.get(0);
    }

    @Override
    public String login(String username, String password) {

        String token = null;
        try {
            getAdminByUsername(username);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                String key = ProjectConfig.USERLOGINCOUNT + username;
                jedisUtil.setex(key + "_" + System.currentTimeMillis(), 600, "1");
                throw new BadCredentialsException("密码不正确");
            }
            token = jwtTokenUtil.generateToken(userDetails);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            insertLoginLog(username);

        } catch (Exception e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;
    }

    /**
     * 根据用户名修改登录时间
     */
    private void updateLoginTimeByUsername(String username) {
        UmsAdmin record = new UmsAdmin();
        record.setLoginTime(new Date());
        Example example = new Example(UmsAdmin.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        adminMapper.updateByExampleSelective(record, example);
    }

    /**
     * 添加登录记录
     *
     * @param username 用户名
     */
    private void insertLoginLog(String username) {
        UmsAdmin admin = getAdminByUsername(username);
        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
        loginLog.setAdminId(admin.getId());
        loginLog.setCreateTime(new Date());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        loginLog.setIp(request.getRemoteAddr());
        adminLoginLogMapper.insert(loginLog);
    }

    @Override
    public String refreshToken(String oldToken) {
        String token = oldToken.substring(tokenHead.length());
        if (jwtTokenUtil.canRefresh(token)) {
            return jwtTokenUtil.refreshToken(token);
        }
        return null;
    }

    @Override
    public UmsAdmin getItem(Integer id) {
        return adminMapper.getAdminById(id);
    }

    @Override
    public List<UmsAdmin> list(String name, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        Example example = new Example(UmsAdmin.class);
        Example.Criteria criteria = example.createCriteria();
        if (StrUtil.isNotBlank(name)) {

            name = "%" + name + "%";
            criteria.andLike("username", name);

        }
        return adminMapper.selectByExample(example);
    }

    @Transactional
    @Override
    public int update(Long adminId, UmsAdmin admin, Long[] ids) {
    /*    int count=-1;
        int count1=-1;
        int count2=-1;
        if (adminId != null &&   admin!= null) {

            admin.setId(adminId);
            UmsAdminPermissionRelationExample relationExample = new UmsAdminPermissionRelationExample();
            relationExample.createCriteria().andAdminIdEqualTo(adminId);
            count =  adminMapper.updateByPrimaryKeySelective(admin);


        }
        if (  ids == null || ids.length == 0){
            return count;
        }

        count1=adminRoleRelationDao.deleteAdminRoleByAdminId(adminId);
        count2 =umsRoleService.adminAddRole(adminId,ids);
        if ((count > 0 || count ==-1) || ((count1 > 0 ||count1 == -1)&&( count2 < 0 ) || count2 ==-1)){
            return  1;
        }
*/
        return -1;
    }

    @Override
    public int delete(Long id) {
        return 0;
    }

    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        return adminRoleRelationMapper.getRoleList(adminId);
    }

    @Override
    public int updatePermission(Long adminId, List<Long> permissionIds) {

        //删除原所有权限关系
        Example example = new Example(UmsAdminPermissionRelation.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("admin_id", adminId);
        adminPermissionRelationMapper.deleteByExample(example);
        //获取用户所有角色权限
        List<UmsPermission> permissionList = adminRoleRelationDao.getRolePermissionList(adminId);
        List<Long> rolePermissionList = permissionList.stream().map(UmsPermission::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(permissionIds)) {
            List<UmsAdminPermissionRelation> relationList = new ArrayList<>();
            //筛选出+权限
            List<Long> addPermissionIdList = permissionIds.stream().filter(permissionId -> !rolePermissionList.contains(permissionId)).collect(Collectors.toList());
            //筛选出-权限
            List<Long> subPermissionIdList = rolePermissionList.stream().filter(permissionId -> !permissionIds.contains(permissionId)).collect(Collectors.toList());
            //插入+-权限关系
            relationList.addAll(convert(adminId, 1, addPermissionIdList));
            relationList.addAll(convert(adminId, -1, subPermissionIdList));
            return adminPermissionRelationDao.insertList(relationList);
        }


        return 0;
    }

    @Override
    public List<UmsPermission> getPermissionList(Long adminId) {
        List<UmsPermission> list = adminRoleRelationMapper.getPermissionList(adminId);
        if (list.isEmpty()) {
            return null;
        }
        List<UmsPermission> permissionIdList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getType() == 1) {

                UmsPermission umsPermission = list.get(i);
                permissionIdList.add(umsPermission);
            }
        }
        return permissionIdList;
    }

    @Override
    public long chickUSerName(String username) {
        Example example = new Example(UmsAdmin.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("username", username);


        return adminMapper.selectByExample(example).size();
    }


    /**
     * 将+-权限关系转化为对象
     */
    private List<UmsAdminPermissionRelation> convert(Long adminId, Integer type, List<Long> permissionIdList) {
        List<UmsAdminPermissionRelation> relationList = permissionIdList.stream().map(permissionId -> {
            UmsAdminPermissionRelation relation = new UmsAdminPermissionRelation();
            relation.setAdminId(adminId);
            relation.setType(type);
            relation.setPermissionId(permissionId);
            return relation;
        }).collect(Collectors.toList());
        return relationList;
    }

    @Override


    public List<Menu> getPermissionList1(Long adminId) {

        List<UmsPermission> permissionList = adminRoleRelationMapper.getPermissionList(adminId);
        if (permissionList.isEmpty()) {
            return null;
        }


        List<Menu> menuLists = new ArrayList<>();

        for (int i = 0; i < permissionList.size(); i++) {


            if (permissionList.get(i).getType() == 1) {
                Menu menu = new Menu();
                List<UmsPermission> menuList = new ArrayList<>();
                menu.setUmsPermission(permissionList.get(i));

                for (int j = i + 1; j < permissionList.size(); j++) {
                    if (menu.getUmsPermission().getId() == permissionList.get(j).getPid()) {
                        menuList.add(permissionList.get(j));

                    }
                }
                menu.setPermissionList(menuList);
                menuLists.add(menu);

            }

        }

        return menuLists;

    }

    @Override
    public Map<String, Object> getAllAdmin(Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserDto> allAdmin = adminMapper.getAllAdmin();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("total", ((Page) (allAdmin)).getTotal());
        map.put("data", allAdmin);
        System.out.println();
        return map;
    }
}


