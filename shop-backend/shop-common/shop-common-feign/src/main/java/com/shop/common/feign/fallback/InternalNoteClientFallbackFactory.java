package com.shop.common.feign.fallback;

import com.shop.common.feign.client.InternalNoteClient;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class InternalNoteClientFallbackFactory implements FallbackFactory<InternalNoteClient> {
    @Override
    public InternalNoteClient create(Throwable cause) {
        log.error("社区服务内部调用失败: {}", cause.getMessage(), cause);
        return new InternalNoteClient() {
            @Override
            public Result<Map<String, Object>> pageNotes(Integer pageNum, Integer pageSize, Integer status, String keyword) {
                return Result.error(503, "社区服务暂时不可用");
            }
            @Override
            public Result<Map<String, Object>> getNoteDetail(Long id) {
                return Result.error(503, "社区服务暂时不可用");
            }
            @Override
            public Result<Map<String, Object>> auditNote(Long id, Map<String, Object> request) {
                return Result.error(503, "社区服务暂时不可用");
            }
            @Override
            public Result<Map<String, Object>> getStatsOverview() {
                return Result.error(503, "社区服务暂时不可用");
            }
        };
    }
}
