package com.shop.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.security.jwt.JwtUtil;
import com.shop.common.web.PageResult;
import com.shop.user.service.EmailService;
import com.shop.user.service.SmsService;
import com.shop.user.controller.request.*;
import com.shop.user.controller.response.*;
import com.shop.user.entity.Coupon;
import com.shop.user.entity.CouponReceive;
import com.shop.user.entity.User;
import com.shop.user.enums.StoreStatus;
import com.shop.user.mapper.CouponMapper;
import com.shop.user.mapper.CouponReceiveMapper;
import com.shop.user.mapper.UserMapper;
import com.shop.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author shop
 * @since 2026-04-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SmsService smsService;
    private final EmailService emailService;
    private final CouponMapper couponMapper;
    private final CouponReceiveMapper couponReceiveMapper;

    // 会员等级名称映射
    private static final String[] MEMBER_LEVEL_NAMES = {"", "普通会员", "银卡会员", "金卡会员", "钻石会员"};

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRegisterResponse register(UserRegisterRequest request) {
        boolean hasEmail = StringUtils.hasText(request.getEmail());
        boolean hasPhone = StringUtils.hasText(request.getPhone());

        // 邮箱注册路径
        if (hasEmail) {
            User existUser = userMapper.selectByEmail(request.getEmail());
            if (existUser != null) {
                throw new BusinessException("该邮箱已注册");
            }
            if (!emailService.verifyCode(request.getEmail(), request.getVerifyCode(), "register")) {
                throw new BusinessException("验证码错误或已过期");
            }
        }

        // 手机号注册路径
        if (hasPhone) {
            User existUser = userMapper.selectByPhone(request.getPhone());
            if (existUser != null) {
                throw new BusinessException("该手机号已注册");
            }
            if (!smsService.verifyCode(request.getPhone(), request.getVerifyCode(), "register")) {
                throw new BusinessException("验证码错误或已过期");
            }
        }

        // 检查用户名是否已存在
        if (StringUtils.hasText(request.getUsername())) {
            User existUser = userMapper.selectByUsername(request.getUsername());
            if (existUser != null) {
                throw new BusinessException("用户名已存在");
            }
        }

        // 创建用户
        User user = createUser(request.getPhone(), request.getEmail(), request.getPassword(),
                request.getUsername(), request.getNickname());

        // 处理邀请码
        if (StringUtils.hasText(request.getReferralCode())) {
            processReferral(user, request.getReferralCode());
        }

        String contact = hasEmail ? request.getEmail() : request.getPhone();
        log.info("用户注册成功: contact={}, userId={}", contact, user.getId());

        UserRegisterResponse response = new UserRegisterResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse login(UserLoginRequest request) {
        // 先按用户名查，查不到再按手机号查，最后按邮箱查
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            user = userMapper.selectByPhone(request.getUsername());
        }
        if (user == null) {
            user = userMapper.selectByEmail(request.getUsername());
        }
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码（统一使用 BCrypt）
        String dbPassword = user.getPassword();
        if (dbPassword == null || !passwordEncoder.matches(request.getPassword(), dbPassword)) {
            throw new BusinessException("用户名或密码错误");
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(request.getIp());
        baseMapper.updateById(user);

        log.info("用户登录成功: username={}, userId={}", request.getUsername(), user.getId());

        return convertToLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse loginByPhone(PhoneLoginRequest request) {
        // 验证短信验证码
        if (!smsService.verifyCode(request.getPhone(), request.getVerifyCode(), "login")) {
            throw new BusinessException("验证码错误或已过期");
        }

        User user = userMapper.selectByPhone(request.getPhone());
        if (user == null) {
            // 自动注册
            user = createUser(request.getPhone(), null, request.getPhone().substring(5), null, null);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(request.getIp());
        baseMapper.updateById(user);

        log.info("手机号登录成功: phone={}, userId={}", request.getPhone(), user.getId());

        return convertToLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse loginByEmail(EmailLoginRequest request) {
        // 验证邮箱验证码
        if (!emailService.verifyCode(request.getEmail(), request.getVerifyCode(), "login")) {
            throw new BusinessException("验证码错误或已过期");
        }

        User user = userMapper.selectByEmail(request.getEmail());
        if (user == null) {
            throw new BusinessException("该邮箱未注册，请先注册");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(request.getIp());
        baseMapper.updateById(user);

        log.info("邮箱登录成功: email={}, userId={}", request.getEmail(), user.getId());

        return convertToLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse loginByWx(WxLoginRequest request) {
        // 先通过openid查询用户
        User user = userMapper.selectByOpenid(request.getOpenid());

        if (user == null) {
            // 检查是否已绑定手机号
            if (StringUtils.hasText(request.getPhone())) {
                user = userMapper.selectByPhone(request.getPhone());
                if (user != null) {
                    // 绑定微信
                    user.setOpenidMp(request.getOpenid());
                    if (StringUtils.hasText(request.getUnionid())) {
                        user.setUnionid(request.getUnionid());
                    }
                    baseMapper.updateById(user);
                }
            }

            // 如果没有找到用户，自动注册
            if (user == null) {
                user = new User();
                user.setUsername("wx_" + System.currentTimeMillis());
                user.setPassword(passwordEncoder.encode("wx_default_" + request.getOpenid()));
                user.setOpenidMp(request.getOpenid());
                user.setUnionid(request.getUnionid());
                user.setNickname(request.getNickname());
                user.setAvatar(request.getAvatar());
                user.setGender(request.getGender());
                user.setStatus(1);
                user.setMemberLevel(1);
                user.setRole("USER");
                user.setIntegral(0);
                user.setGrowthValue(0);

                // 如果有手机号
                if (StringUtils.hasText(request.getPhone())) {
                    user.setPhone(request.getPhone());
                }

                baseMapper.insert(user);
                log.info("微信用户自动注册: openid={}, userId={}", request.getOpenid(), user.getId());
            }
        } else {
            // 更新微信信息
            if (StringUtils.hasText(request.getNickname())) {
                user.setNickname(request.getNickname());
            }
            if (StringUtils.hasText(request.getAvatar())) {
                user.setAvatar(request.getAvatar());
            }
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(request.getIp());
            baseMapper.updateById(user);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        log.info("微信登录成功: openid={}, userId={}", request.getOpenid(), user.getId());

        return convertToLoginResponse(user);
    }

    @Override
    public UserInfoResponse getCurrentUserInfo(Long userId) {
        User user = getUserEntityById(userId);
        return convertToInfoResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = getUserEntityById(userId);

        // 更新字段
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }

        baseMapper.updateById(user);

        log.info("更新用户信息成功: userId={}", userId);

        return convertToInfoResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateAvatar(Long userId, String avatarUrl) {
        User user = getUserEntityById(userId);
        user.setAvatar(avatarUrl);
        baseMapper.updateById(user);

        log.info("更新用户头像成功: userId={}", userId);

        return convertToInfoResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUserEntityById(userId);

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        baseMapper.updateById(user);

        log.info("用户修改密码成功: userId={}", userId);
    }

    @Override
    public PageResult<UserInfoResponse> pageUsers(Integer pageNum, Integer pageSize, UserQueryRequest request) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);

        // 查询条件
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(User::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getPhone())) {
            wrapper.like(User::getPhone, request.getPhone());
        }
        if (request.getStatus() != null) {
            wrapper.eq(User::getStatus, request.getStatus());
        }
        if (request.getMemberLevel() != null) {
            wrapper.eq(User::getMemberLevel, request.getMemberLevel());
        }
        if (StringUtils.hasText(request.getRole())) {
            wrapper.eq(User::getRole, request.getRole());
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = baseMapper.selectPage(page, wrapper);

        List<UserInfoResponse> records = result.getRecords().stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    @Override
    public UserInfoResponse getUserById(Long id) {
        User user = getUserEntityById(id);
        return convertToInfoResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse update(Long id, UserUpdateRequest request) {
        User user = getUserEntityById(id);

        // 更新字段
        if (StringUtils.hasText(request.getUsername())) {
            user.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getRole())) {
            user.setRole(request.getRole());
        }

        int rows = baseMapper.updateById(user);
        log.info("管理员更新用户: userId={}, affectedRows={}", id, rows);

        return convertToInfoResponse(user);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        User user = getUserEntityById(id);
        user.setStatus(status);
        baseMapper.updateById(user);

        log.info("更新用户状态成功: userId={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 先查询用户是否存在
        User user = getUserEntityById(id);
        log.info("准备删除用户: userId={}", id);

        // 使用 MyBatis-Plus 逻辑删除（会自动设置 deleted=1）
        int rows = baseMapper.deleteById(id);

        log.info("删除用户完成: userId={}, affectedRows={}", id, rows);
        if (rows == 0) {
            throw new BusinessException("删除用户失败");
        }
    }

    @Override
    public List<UserInfoResponse> listUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);

        return baseMapper.selectList(wrapper).stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyStore(Long userId, StoreApplyRequest request) {
        User user = getUserEntityById(userId);

        if (!"USER".equals(user.getRole())) {
            throw new BusinessException("当前角色不支持申请入驻");
        }

        if (user.getStoreStatus() != null && StoreStatus.PENDING.getCode() == user.getStoreStatus()) {
            throw new BusinessException("您已有入驻申请正在审核中");
        }

        user.setStoreStatus(StoreStatus.PENDING.getCode());
        user.setStoreName(request.getStoreName());
        user.setStoreLogo(request.getStoreLogo());
        user.setStoreDescription(request.getStoreDescription());
        user.setStoreApplyTime(LocalDateTime.now());
        baseMapper.updateById(user);

        log.info("用户申请入驻成功: userId={}, storeName={}", userId, request.getStoreName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditStore(Long userId, StoreAuditRequest request) {
        User user = getUserEntityById(userId);

        if (user.getStoreStatus() == null || user.getStoreStatus() != StoreStatus.PENDING.getCode()) {
            throw new BusinessException("该申请已审核过");
        }

        if (request.getStatus() == StoreStatus.APPROVED.getCode()) {
            user.setRole("STORE");
            user.setStoreStatus(StoreStatus.APPROVED.getCode());
            user.setStoreAuditTime(LocalDateTime.now());
            log.info("店家入驻审核通过: userId={}, storeName={}", userId, user.getStoreName());
        } else if (request.getStatus() == StoreStatus.REJECTED.getCode()) {
            user.setStoreStatus(StoreStatus.REJECTED.getCode());
            user.setStoreRejectReason(request.getRejectReason());
            user.setStoreAuditTime(LocalDateTime.now());
            log.info("店家入驻审核拒绝: userId={}, reason={}", userId, request.getRejectReason());
        } else {
            throw new BusinessException("无效的审核状态");
        }

        baseMapper.updateById(user);
    }

    @Override
    public PageResult<UserInfoResponse> pageStoreApplications(Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        wrapper.eq(User::getStoreStatus, StoreStatus.PENDING.getCode());
        wrapper.orderByAsc(User::getStoreApplyTime);

        Page<User> result = baseMapper.selectPage(page, wrapper);

        List<UserInfoResponse> records = result.getRecords().stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    private User getUserEntityById(Long id) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, id);
        wrapper.eq(User::getDeleted, 0);

        User user = baseMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private UserLoginResponse convertToLoginResponse(User user) {
        UserLoginResponse response = new UserLoginResponse();
        BeanUtils.copyProperties(user, response);
        response.setUserId(user.getId());
        response.setMemberLevelName(MEMBER_LEVEL_NAMES[user.getMemberLevel()]);

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        response.setToken(token);

        return response;
    }

    private UserInfoResponse convertToInfoResponse(User user) {
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        response.setUserId(user.getId());
        response.setMemberLevelName(MEMBER_LEVEL_NAMES[user.getMemberLevel()]);
        return response;
    }

    private User createUser(String phone, String email, String password, String username, String nickname) {
        User user = new User();
        // 用户名：优先使用传入值，否则从手机号或邮箱生成
        if (StringUtils.hasText(username)) {
            user.setUsername(username);
        } else if (StringUtils.hasText(phone)) {
            user.setUsername("user_" + phone.substring(7));
        } else {
            user.setUsername("user_" + email.substring(0, email.indexOf('@')));
        }
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setEmail(email);
        // 昵称：优先使用传入值，否则从手机号或邮箱生成
        if (StringUtils.hasText(nickname)) {
            user.setNickname(nickname);
        } else if (StringUtils.hasText(phone)) {
            user.setNickname("用户" + phone.substring(7));
        } else {
            user.setNickname("用户" + email.substring(0, email.indexOf('@')));
        }
        user.setStatus(1);
        user.setMemberLevel(1);
        user.setRole("USER");
        user.setIntegral(0);
        user.setGrowthValue(0);
        user.setReferralCode(generateReferralCode(phone, email));

        baseMapper.insert(user);

        grantNewUserCoupons(user.getId());

        return user;
    }

    private String generateReferralCode(String phone, String email) {
        String suffix = StringUtils.hasText(phone)
                ? phone.substring(5)
                : String.valueOf(Math.abs(email.hashCode()) % 100000);
        return "RF" + suffix + System.currentTimeMillis() % 10000;
    }

    private void processReferral(User newUser, String referralCode) {
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getReferralCode, referralCode);
            wrapper.eq(User::getDeleted, 0);
            User inviter = baseMapper.selectOne(wrapper);
            if (inviter == null || inviter.getId().equals(newUser.getId())) return;

            newUser.setInviterId(inviter.getId());
            baseMapper.updateById(newUser);

            // Grant reward to inviter: 100 integral
            inviter.setIntegral((inviter.getIntegral() != null ? inviter.getIntegral() : 0) + 100);
            baseMapper.updateById(inviter);

            log.info("邀请奖励发放: inviterId={}, newUserId={}", inviter.getId(), newUser.getId());
        } catch (Exception e) {
            log.error("处理邀请失败: code={}, error={}", referralCode, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getReferralInfo(Long userId) {
        User user = baseMapper.selectById(userId);
        Map<String, Object> info = new java.util.LinkedHashMap<>();
        info.put("referralCode", user.getReferralCode() != null ? user.getReferralCode() : generateReferralCode(user.getPhone(), user.getEmail()));

        // Count invited users
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getInviterId, userId);
        wrapper.eq(User::getDeleted, 0);
        long count = baseMapper.selectCount(wrapper);
        info.put("invitedCount", count);
        return info;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.selectByEmail(email) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        // 验证邮箱验证码
        if (!emailService.verifyCode(request.getEmail(), request.getVerifyCode(), "reset")) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 查找用户
        User user = userMapper.selectByEmail(request.getEmail());
        if (user == null) {
            throw new BusinessException("该邮箱未注册");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        baseMapper.updateById(user);

        log.info("用户重置密码成功: email={}", request.getEmail());
    }

    private void grantNewUserCoupons(Long userId) {
        try {
            List<Coupon> newUserCoupons = couponMapper.findActiveNewUserCoupons();
            for (Coupon coupon : newUserCoupons) {
                if (coupon.getReceivedCount() >= coupon.getTotalCount()) continue;

                int updated = couponMapper.increaseReceivedCount(coupon.getId());
                if (updated == 0) continue;

                CouponReceive receive = new CouponReceive();
                receive.setCouponId(coupon.getId());
                receive.setUserId(userId);
                receive.setReceiveTime(LocalDateTime.now());
                receive.setStatus(0);
                couponReceiveMapper.insert(receive);

                log.info("新人券自动发放: userId={}, couponId={}", userId, coupon.getId());
            }
        } catch (Exception e) {
            log.error("新人券发放失败: userId={}, error={}", userId, e.getMessage());
        }
    }
}
