package com.shop.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.user.controller.request.*;
import com.shop.user.controller.response.*;
import com.shop.user.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 *
 * @author shop
 * @since 2026-04-15
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    UserRegisterResponse register(UserRegisterRequest request);

    /**
     * 用户登录
     */
    UserLoginResponse login(UserLoginRequest request);

    /**
     * 手机号登录
     */
    UserLoginResponse loginByPhone(PhoneLoginRequest request);

    /**
     * 微信登录
     */
    UserLoginResponse loginByWx(WxLoginRequest request);

    /**
     * 邮箱验证码登录
     */
    UserLoginResponse loginByEmail(EmailLoginRequest request);

    /**
     * 获取当前登录用户信息
     */
    UserInfoResponse getCurrentUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request);

    /**
     * 更新头像
     */
    UserInfoResponse updateAvatar(Long userId, String avatarUrl);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 分页查询用户
     */
    PageResult<UserInfoResponse> pageUsers(Integer pageNum, Integer pageSize, UserQueryRequest request);

    /**
     * 根据ID查询用户
     */
    UserInfoResponse getUserById(Long id);

    /**
     * 更新用户（管理员）
     */
    UserInfoResponse update(Long id, UserUpdateRequest request);

    /**
     * 禁用/启用用户
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 列表查询用户
     */
    List<UserInfoResponse> listUsers();

    /**
     * 申请成为店家
     */
    void applyStore(Long userId, StoreApplyRequest request);

    /**
     * 审核店家入驻（管理员）
     */
    void auditStore(Long userId, StoreAuditRequest request);

    /**
     * 分页查询待审核店家
     */
    PageResult<UserInfoResponse> pageStoreApplications(Integer pageNum, Integer pageSize);

    Map<String, Object> getReferralInfo(Long userId);

    /**
     * 邮箱重置密码
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * 检查邮箱是否已注册
     */
    boolean existsByEmail(String email);

    /**
     * 获取用户会员等级（内部接口）
     */
    Integer getMemberLevel(Long userId);

    /**
     * 扣减积分（内部接口）
     */
    void deductIntegral(Long userId, Integer amount, String description);

    /**
     * 增加积分（内部接口）
     */
    void addIntegral(Long userId, Integer amount, String description);

    /**
     * 扣减余额（内部接口）
     */
    void deductBalance(Long userId, BigDecimal amount, String description);

    /**
     * 增加余额（内部接口）
     */
    void addBalance(Long userId, BigDecimal amount, String description);

    /**
     * 增加成长值并自动升级会员等级（内部接口）
     */
    void addGrowthValue(Long userId, Integer amount);

    /**
     * 退款余额（内部接口）
     */
    void refundBalance(Long userId, BigDecimal amount, String description, String bizId);

    /**
     * 获取注册统计数据（内部接口）
     */
    Map<String, Object> getRegisterStats();

    /**
     * 获取用户积分（内部接口）
     */
    Integer getIntegral(Long userId);

    /**
     * 获取用户基本信息（内部接口）
     */
    Map<String, Object> getUserBasicInfo(Long userId);

    /**
     * 批量获取用户基本信息（内部接口）
     */
    Map<String, Object> getBatchBasicInfo(List<Long> userIds);

    /**
     * 获取用户关注列表（内部接口）
     */
    List<Long> getFollowingUserIds(Long userId);
}
