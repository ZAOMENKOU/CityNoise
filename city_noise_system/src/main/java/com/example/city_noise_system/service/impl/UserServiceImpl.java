package com.example.city_noise_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.city_noise_system.dto.LoginDTO;
import com.example.city_noise_system.dto.RegisterDTO;
import com.example.city_noise_system.entity.User;
import com.example.city_noise_system.mapper.UserMapper;
import com.example.city_noise_system.service.UserService;
import com.example.city_noise_system.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import java.nio.charset.StandardCharsets;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public UserVO register(RegisterDTO registerDTO) {
        // 1. 参数校验
        validateRegisterParams(registerDTO);

        // 2. 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", registerDTO.getUsername());
        queryWrapper.eq("deleted", 0);
        User existingUser = getOne(queryWrapper);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 3. 创建用户实体
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);

        // 4. 强制设置角色为居民
        user.setRole("RESIDENT");

        // 5. 对密码进行MD5加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(
                registerDTO.getPassword().getBytes(StandardCharsets.UTF_8));
        user.setPassword(encryptedPassword);

        // 6. 设置默认头像
        if (!StringUtils.hasText(user.getAvatar())) {
            user.setAvatar("/uploads/default-avatar.png");
        }

        // 7. 保存用户
        boolean saved = save(user);
        if (!saved) {
            throw new RuntimeException("注册失败，请重试");
        }

        // 8. 返回用户信息
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 验证注册参数
     * 
     * @param registerDTO 注册参数
     */
    private void validateRegisterParams(RegisterDTO registerDTO) {
        // 验证用户名
        String username = registerDTO.getUsername();
        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new RuntimeException("用户名长度必须在3-20个字符之间");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new RuntimeException("用户名只能包含字母、数字和下划线");
        }

        // 验证密码
        String password = registerDTO.getPassword();
        if (!StringUtils.hasText(password)) {
            throw new RuntimeException("密码不能为空");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new RuntimeException("密码长度必须在6-20个字符之间");
        }

        // 验证真实姓名
        String realName = registerDTO.getRealName();
        if (!StringUtils.hasText(realName)) {
            throw new RuntimeException("真实姓名不能为空");
        }
        if (realName.length() < 2 || realName.length() > 20) {
            throw new RuntimeException("真实姓名长度必须在2-20个字符之间");
        }

        // 验证手机号码（如果提供）
        String phone = registerDTO.getPhone();
        if (StringUtils.hasText(phone) && !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号码格式不正确");
        }
    }

    @Override
    public UserVO login(LoginDTO loginDTO) {
        // 1. 查找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginDTO.getUsername());
        queryWrapper.eq("deleted", 0);
        User user = getOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 验证密码
        String encryptedPassword = DigestUtils.md5DigestAsHex(
                loginDTO.getPassword().getBytes(StandardCharsets.UTF_8));

        System.out.println("用户输入的密码: " + loginDTO.getPassword());
        System.out.println("加密后的密码: " + encryptedPassword);
        System.out.println("数据库中的密码: " + user.getPassword());

        if (!user.getPassword().equals(encryptedPassword)) {
            System.out.println("密码验证失败");
            throw new RuntimeException("用户名或密码错误");
        } else {
            System.out.println("密码验证成功");
        }

        // 3. 转换为VO返回
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public User getByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("deleted", 0);
        return getOne(queryWrapper);
    }

    @Override
    public UserVO getCurrentUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public boolean updateUserInfo(Long userId, User user) {
        User existingUser = getById(userId);
        if (existingUser == null || existingUser.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        // 只允许更新特定字段
        if (StringUtils.hasText(user.getRealName())) {
            existingUser.setRealName(user.getRealName());
        }

        if (StringUtils.hasText(user.getPhone())) {
            existingUser.setPhone(user.getPhone());
        }

        if (StringUtils.hasText(user.getAvatar())) {
            existingUser.setAvatar(user.getAvatar());
        }

        if (StringUtils.hasText(user.getRole())) {
            existingUser.setRole(user.getRole());
        }

        return updateById(existingUser);
    }
}