package com.bddk.geocourse.module.identity.service;

import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录策略工厂。
 */
@Component
public class PortalAuthStrategyFactory {

    private final Map<String, PortalAuthStrategy> strategyByLoginType;

    public PortalAuthStrategyFactory(List<PortalAuthStrategy> strategies) {
        this.strategyByLoginType = strategies.stream()
                .sorted((left, right) -> Integer.compare(left.getOrder(), right.getOrder()))
                .collect(LinkedHashMap::new, (map, strategy) -> map.put(strategy.getLoginType(), strategy), Map::putAll);
    }

    public PortalAuthStrategy getStrategy(String loginType) {
        if (!StringUtils.hasText(loginType)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "登录类型不能为空");
        }
        PortalAuthStrategy strategy = strategyByLoginType.get(loginType);
        if (strategy == null) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "不支持的登录类型: " + loginType);
        }
        return strategy;
    }

    public List<AdminAuthDesign> listDesigns() {
        return strategyByLoginType.values().stream()
                .map(PortalAuthStrategy::getDesign)
                .toList();
    }
}
