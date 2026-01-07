package com.javaee.se_final_backend.service;

import com.javaee.se_final_backend.model.DTO.FamilyRegisterRequest;
import com.javaee.se_final_backend.model.DTO.FamilyRegisterResponse;
import com.javaee.se_final_backend.model.entity.Family;
import com.javaee.se_final_backend.model.entity.User;
import com.javaee.se_final_backend.repository.FamilyRepository;
import com.javaee.se_final_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyRepository familyRepository;

    // 注册用户
    public User register(User user) {
        return userRepository.save(user);
    }

    // 登录验证
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    // 根据邮箱查找用户
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 家庭注册 - 创建家庭和批量注册成员
    @Transactional
    public FamilyRegisterResponse registerFamily(FamilyRegisterRequest request) {
        try {
            // 检查管理员邮箱是否已存在
            if (findByEmail(request.getAdmin().getEmail()) != null) {
                return new FamilyRegisterResponse(false, "管理员邮箱已存在");
            }

            // 检查成员邮箱是否重复
            for (FamilyRegisterRequest.UserInfo member : request.getMembers()) {
                if (findByEmail(member.getEmail()) != null) {
                    return new FamilyRegisterResponse(false, "成员邮箱 " + member.getEmail() + " 已存在");
                }
            }

            // 创建家庭
            Family family = new Family();
            family.setName(request.getAdmin().getName() + "的家庭");
            family = familyRepository.save(family);

            // 创建管理员账号
            User admin = new User();
            admin.setEmail(request.getAdmin().getEmail());
            admin.setPassword(request.getAdmin().getPassword());
            admin.setName(request.getAdmin().getName());
            admin.setFamilyId(family.getId());
            admin.setRole("admin");
            admin = userRepository.save(admin);

            // 创建成员账号
            for (FamilyRegisterRequest.UserInfo memberInfo : request.getMembers()) {
                User member = new User();
                member.setEmail(memberInfo.getEmail());
                member.setPassword(memberInfo.getPassword());
                member.setName(memberInfo.getName());
                member.setFamilyId(family.getId());
                member.setRole("member");
                userRepository.save(member);
            }

            return new FamilyRegisterResponse(true, "家庭创建成功", admin, family.getId());

        } catch (Exception e) {
            return new FamilyRegisterResponse(false, "创建失败: " + e.getMessage());
        }
    }

    // 创建家庭
    @Transactional
    public FamilyRegisterResponse createFamily(Integer adminId, String familyName) {
        try {
            User admin = userRepository.findById(adminId).orElse(null);
            if (admin == null) {
                return new FamilyRegisterResponse(false, "管理员不存在");
            }

            if (admin.getFamilyId() != null) {
                return new FamilyRegisterResponse(false, "管理员已有家庭");
            }

            // 创建家庭
            Family family = new Family();
            family.setName(familyName);
            family = familyRepository.save(family);

            // 更新管理员信息
            admin.setFamilyId(family.getId());
            admin.setRole("admin");
            admin = userRepository.save(admin);

            return new FamilyRegisterResponse(true, "家庭创建成功", admin, family.getId());

        } catch (Exception e) {
            return new FamilyRegisterResponse(false, "创建失败: " + e.getMessage());
        }
    }

    // 添加家庭成员
    @Transactional
    public FamilyRegisterResponse addFamilyMember(Integer adminId, FamilyRegisterRequest.UserInfo memberInfo) {
        try {
            User admin = userRepository.findById(adminId).orElse(null);
            if (admin == null) {
                return new FamilyRegisterResponse(false, "管理员不存在");
            }

            if (!"admin".equals(admin.getRole())) {
                return new FamilyRegisterResponse(false, "无权限添加成员");
            }

            // 检查邮箱是否已存在
            if (findByEmail(memberInfo.getEmail()) != null) {
                return new FamilyRegisterResponse(false, "邮箱已存在");
            }

            // 创建成员
            User member = new User();
            member.setEmail(memberInfo.getEmail());
            member.setPassword(memberInfo.getPassword());
            member.setName(memberInfo.getName());
            member.setFamilyId(admin.getFamilyId());
            member.setRole("member");
            member = userRepository.save(member);

            return new FamilyRegisterResponse(true, "成员添加成功", member, admin.getFamilyId());

        } catch (Exception e) {
            return new FamilyRegisterResponse(false, "添加失败: " + e.getMessage());
        }
    }

    // 获取家庭成员
    public List<User> getFamilyMembers(Integer familyId) {
        return userRepository.findAll().stream()
                .filter(user -> familyId.equals(user.getFamilyId()))
                .collect(java.util.stream.Collectors.toList());
    }
      
     public List<Map<String, Object>> members(Integer userId) {

        User self = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<User> users = userRepository.findByFamilyId(self.getFamilyId());

        return users.stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getName());
                    return m;
                })
                .collect(Collectors.toList());
     }
}
