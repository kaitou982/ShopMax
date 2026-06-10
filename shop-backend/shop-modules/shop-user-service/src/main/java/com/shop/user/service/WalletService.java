package com.shop.user.service;

import com.shop.common.web.PageResult;
import com.shop.user.entity.BalanceLog;
import com.shop.user.entity.IntegralLog;

import java.math.BigDecimal;

public interface WalletService {

    /** 积分变动 */
    void changeIntegral(Long userId, int amount, int type, String bizId, String remark);

    /** 余额变动 */
    void changeBalance(Long userId, BigDecimal amount, int type, String bizId, String payChannel, String remark);

    /** 增加成长值并检查会员升级 */
    void addGrowthValue(Long userId, int growth);

    /** 积分流水 */
    PageResult<IntegralLog> integralLogs(Long userId, int pageNum, int pageSize);

    /** 余额流水 */
    PageResult<BalanceLog> balanceLogs(Long userId, int pageNum, int pageSize);

    /** 积分支付比率: 100积分=1元 */
    int INTEGRAL_RATE = 100;
}
