package com.shop.common.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 熔断降级规则配置
 *
 * @author kaitou
 * @since 2026/06/23
 */
@Component
public class SentinelConfig {

    @PostConstruct
    public void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 默认规则：慢调用比例熔断
        // - 统计窗口：10秒
        // - 最小请求数：5
        // - 慢调用阈值：500ms
        // - 慢调用比例阈值：50%
        // - 熔断时长：30秒
        DegradeRule defaultRule = new DegradeRule("default")
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setCount(500)
                .setTimeWindow(30)
                .setSlowRatioThreshold(0.5)
                .setStatIntervalMs(10000)
                .setMinRequestAmount(5);
        rules.add(defaultRule);

        // Feign 调用规则：异常比例熔断
        DegradeRule feignRule = new DegradeRule("feign")
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.5)
                .setTimeWindow(30)
                .setStatIntervalMs(10000)
                .setMinRequestAmount(5);
        rules.add(feignRule);

        DegradeRuleManager.loadRules(rules);
    }
}
