package com.shop.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.feign.client.NotificationClient;
import com.shop.common.web.PageResult;
import com.shop.community.controller.request.NoteCreateRequest;
import com.shop.community.controller.request.NoteUpdateRequest;
import com.shop.community.controller.response.NoteDetailResponse;
import com.shop.community.controller.response.NoteResponse;
import com.shop.community.entity.*;
import com.shop.community.mapper.*;
import com.shop.community.service.CommunityExternalService;
import com.shop.community.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    private final NoteImageMapper noteImageMapper;
    private final NoteProductMapper noteProductMapper;
    private final NoteLikeMapper noteLikeMapper;
    private final NoteFavoriteMapper noteFavoriteMapper;
    private final CommunityExternalService externalService;

    @Autowired(required = false)
    private NotificationClient notificationClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteDetailResponse create(Long userId, NoteCreateRequest request) {
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());

        int noteStatus = request.getStatus() != null ? request.getStatus() : NoteStatus.UNDER_REVIEW.getCode();
        if (noteStatus != NoteStatus.DRAFT.getCode() && noteStatus != NoteStatus.UNDER_REVIEW.getCode()) {
            throw new BusinessException("只能保存草稿或提交审核");
        }
        note.setStatus(noteStatus);

        baseMapper.insert(note);

        saveImages(note.getId(), request.getImages());
        saveProducts(note.getId(), request.getProductIds());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            note.setCoverUrl(request.getImages().get(0).getImageUrl());
            baseMapper.updateById(note);
        }

        log.info("创建笔记成功: id={}, userId={}, status={}", note.getId(), userId, noteStatus);

        // 提交审核时通知管理员
        if (noteStatus == NoteStatus.UNDER_REVIEW.getCode()) {
            try {
                if (notificationClient != null) {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("type", 3);
                    notif.put("title", "新的内容待审核");
                    notif.put("content", "笔记「" + (request.getTitle() != null ? request.getTitle() : "无标题") + "」已提交审核");
                    notif.put("refId", note.getId());
                    notif.put("refType", "note_audit");
                    notificationClient.createNotification(notif);
                }
            } catch (Exception e) {
                log.warn("发送内容审核通知失败: {}", e.getMessage());
            }
        }

        return buildDetail(note, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteDetailResponse update(Long userId, Long noteId, NoteUpdateRequest request) {
        Note note = getNoteById(noteId);
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此笔记");
        }

        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }
        if (request.getStatus() != null) {
            if (request.getStatus() != NoteStatus.DRAFT.getCode() && request.getStatus() != NoteStatus.UNDER_REVIEW.getCode()) {
                throw new BusinessException("只能保存草稿或提交审核");
            }
            note.setStatus(request.getStatus());
        }

        baseMapper.updateById(note);

        if (request.getImages() != null) {
            LambdaQueryWrapper<NoteImage> imgWrapper = new LambdaQueryWrapper<>();
            imgWrapper.eq(NoteImage::getNoteId, noteId);
            noteImageMapper.delete(imgWrapper);
            saveImages(noteId, request.getImages());
            if (!request.getImages().isEmpty()) {
                note.setCoverUrl(request.getImages().get(0).getImageUrl());
                baseMapper.updateById(note);
            }
        }

        if (request.getProductIds() != null) {
            LambdaQueryWrapper<NoteProduct> prodWrapper = new LambdaQueryWrapper<>();
            prodWrapper.eq(NoteProduct::getNoteId, noteId);
            noteProductMapper.delete(prodWrapper);
            saveProducts(noteId, request.getProductIds());
        }

        log.info("更新笔记成功: id={}", noteId);
        return buildDetail(note, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long noteId) {
        Note note = getNoteById(noteId);
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此笔记");
        }
        baseMapper.deleteById(noteId);
        log.info("删除笔记成功: id={}", noteId);
    }

    @Override
    public NoteDetailResponse getDetail(Long noteId, Long currentUserId) {
        Note note = getNoteById(noteId);
        baseMapper.increaseViewCount(noteId, 1);
        note.setViewCount(note.getViewCount() + 1);
        return buildDetail(note, currentUserId);
    }

    @Override
    public PageResult<NoteResponse> page(Integer pageNum, Integer pageSize, String tab, Long currentUserId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, NoteStatus.PUBLISHED.getCode());
        wrapper.orderByDesc(Note::getCreateTime);

        if ("following".equals(tab) && currentUserId != null) {
            List<Long> followingIds = externalService.getFollowingUserIds(currentUserId);
            if (followingIds.isEmpty()) {
                return PageResult.empty();
            }
            wrapper.in(Note::getUserId, followingIds);
        }

        Page<Note> page = new Page<>(pageNum, pageSize);
        Page<Note> result = baseMapper.selectPage(page, wrapper);

        List<NoteResponse> records = batchBuildResponses(result.getRecords(), currentUserId);

        return PageResult.of(records, result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }

    @Override
    public PageResult<NoteResponse> pageByUserId(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        wrapper.eq(Note::getStatus, NoteStatus.PUBLISHED.getCode());
        wrapper.orderByDesc(Note::getCreateTime);

        Page<Note> page = new Page<>(pageNum, pageSize);
        Page<Note> result = baseMapper.selectPage(page, wrapper);

        List<NoteResponse> records = batchBuildResponses(result.getRecords(), null);

        return PageResult.of(records, result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }

    /**
     * 批量构建 NoteResponse，消除 N+1 查询
     */
    private List<NoteResponse> batchBuildResponses(List<Note> notes, Long currentUserId) {
        if (notes.isEmpty()) {
            return List.of();
        }

        List<Long> noteIds = notes.stream().map(Note::getId).collect(Collectors.toList());

        // 1) 批量获取用户信息（通过 Feign）
        List<Long> userIds = notes.stream().map(Note::getUserId).distinct().collect(Collectors.toList());
        Map<Long, CommunityExternalService.UserInfo> userInfoMap = externalService.batchGetUserInfo(userIds);

        // 2) 批量获取图片
        LambdaQueryWrapper<NoteImage> imgWrapper = new LambdaQueryWrapper<>();
        imgWrapper.in(NoteImage::getNoteId, noteIds);
        imgWrapper.orderByAsc(NoteImage::getSortOrder);
        Map<Long, List<String>> imageMap = noteImageMapper.selectList(imgWrapper).stream()
                .collect(Collectors.groupingBy(NoteImage::getNoteId,
                        Collectors.mapping(NoteImage::getImageUrl, Collectors.toList())));

        // 3) 批量获取关联商品
        LambdaQueryWrapper<NoteProduct> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(NoteProduct::getNoteId, noteIds);
        List<Long> allProductIds = noteProductMapper.selectList(productWrapper).stream()
                .map(NoteProduct::getProductId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, List<NoteResponse.ProductItem>> productMap;
        if (!allProductIds.isEmpty()) {
            Map<Long, CommunityExternalService.ProductInfo> productInfoMap = externalService.batchGetProductInfo(allProductIds);
            // 按 noteId 关联
            Map<Long, List<Long>> noteProductIdMap = noteProductMapper.selectList(productWrapper).stream()
                    .collect(Collectors.groupingBy(NoteProduct::getNoteId,
                            Collectors.mapping(NoteProduct::getProductId, Collectors.toList())));
            productMap = new HashMap<>();
            for (var entry : noteProductIdMap.entrySet()) {
                List<NoteResponse.ProductItem> items = new ArrayList<>();
                for (Long pid : entry.getValue()) {
                    var pi = productInfoMap.get(pid);
                    if (pi != null) {
                        var item = new NoteResponse.ProductItem();
                        item.setId(pi.id());
                        item.setName(pi.name());
                        item.setMainImage(pi.mainImage());
                        item.setSalePrice(pi.salePrice());
                        items.add(item);
                    }
                }
                if (!items.isEmpty()) {
                    productMap.put(entry.getKey(), items);
                }
            }
        } else {
            productMap = Map.of();
        }

        // 4) 批量获取点赞/收藏状态
        Set<Long> likedNoteIds = Set.of();
        Set<Long> favoritedNoteIds = Set.of();
        if (currentUserId != null) {
            LambdaQueryWrapper<NoteLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.in(NoteLike::getNoteId, noteIds);
            likeWrapper.eq(NoteLike::getUserId, currentUserId);
            likedNoteIds = noteLikeMapper.selectList(likeWrapper).stream()
                    .map(NoteLike::getNoteId)
                    .collect(Collectors.toSet());

            LambdaQueryWrapper<NoteFavorite> favWrapper = new LambdaQueryWrapper<>();
            favWrapper.in(NoteFavorite::getNoteId, noteIds);
            favWrapper.eq(NoteFavorite::getUserId, currentUserId);
            favoritedNoteIds = noteFavoriteMapper.selectList(favWrapper).stream()
                    .map(NoteFavorite::getNoteId)
                    .collect(Collectors.toSet());
        }

        // 5) 组装结果
        Set<Long> finalLikedNoteIds = likedNoteIds;
        Set<Long> finalFavoritedNoteIds = favoritedNoteIds;
        return notes.stream().map(note -> {
            NoteResponse resp = new NoteResponse();
            resp.setId(note.getId());
            resp.setUserId(note.getUserId());
            resp.setTitle(note.getTitle());
            resp.setContent(limitContent(note.getContent(), 200));
            resp.setCoverUrl(note.getCoverUrl());
            resp.setStatus(note.getStatus());
            resp.setLikeCount(note.getLikeCount());
            resp.setCommentCount(note.getCommentCount());
            resp.setFavoriteCount(note.getFavoriteCount());
            resp.setViewCount(note.getViewCount());
            resp.setCreateTime(note.getCreateTime());

            var userInfo = userInfoMap.get(note.getUserId());
            if (userInfo != null) {
                resp.setUserNickname(userInfo.nickname());
                resp.setUserAvatar(userInfo.avatar());
            }

            resp.setImages(imageMap.getOrDefault(note.getId(), List.of()));
            resp.setProducts(productMap.getOrDefault(note.getId(), List.of()));

            if (currentUserId != null) {
                resp.setIsLiked(finalLikedNoteIds.contains(note.getId()));
                resp.setIsFavorited(finalFavoritedNoteIds.contains(note.getId()));
            }

            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteDetailResponse audit(Long noteId, Integer status, String rejectReason) {
        Note note = getNoteById(noteId);
        int currentStatus = note.getStatus();
        if (currentStatus != NoteStatus.UNDER_REVIEW.getCode()
                && currentStatus != NoteStatus.PUBLISHED.getCode()
                && currentStatus != NoteStatus.REJECTED.getCode()) {
            throw new BusinessException("该笔记状态不允许审核操作");
        }

        if (status == NoteStatus.PUBLISHED.getCode()) {
            note.setStatus(NoteStatus.PUBLISHED.getCode());
            note.setRejectReason(null);
        } else if (status == NoteStatus.REJECTED.getCode()) {
            if (!StringUtils.hasText(rejectReason)) {
                throw new BusinessException("驳回时必须填写原因");
            }
            note.setStatus(NoteStatus.REJECTED.getCode());
            note.setRejectReason(rejectReason);
        } else {
            throw new BusinessException("无效的审核操作");
        }

        note.setAuditTime(LocalDateTime.now());
        baseMapper.updateById(note);

        log.info("审核笔记: id={}, status={}", noteId, status);
        return buildDetail(note, null);
    }

    private void saveImages(Long noteId, List<NoteCreateRequest.ImageItem> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            var img = images.get(i);
            NoteImage noteImage = new NoteImage();
            noteImage.setNoteId(noteId);
            noteImage.setImageUrl(img.getImageUrl());
            noteImage.setSortOrder(img.getSortOrder() != null ? img.getSortOrder() : i);
            noteImageMapper.insert(noteImage);
        }
    }

    private void saveProducts(Long noteId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        for (Long productId : productIds) {
            NoteProduct noteProduct = new NoteProduct();
            noteProduct.setNoteId(noteId);
            noteProduct.setProductId(productId);
            noteProductMapper.insert(noteProduct);
        }
    }

    private Note getNoteById(Long noteId) {
        Note note = baseMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        return note;
    }

    private NoteResponse buildResponse(Note note, Long currentUserId) {
        NoteResponse resp = new NoteResponse();
        resp.setId(note.getId());
        resp.setUserId(note.getUserId());
        resp.setTitle(note.getTitle());
        resp.setContent(limitContent(note.getContent(), 200));
        resp.setCoverUrl(note.getCoverUrl());
        resp.setStatus(note.getStatus());
        resp.setLikeCount(note.getLikeCount());
        resp.setCommentCount(note.getCommentCount());
        resp.setFavoriteCount(note.getFavoriteCount());
        resp.setViewCount(note.getViewCount());
        resp.setCreateTime(note.getCreateTime());

        var userInfoMap = externalService.batchGetUserInfo(List.of(note.getUserId()));
        var userInfo = userInfoMap.get(note.getUserId());
        if (userInfo != null) {
            resp.setUserNickname(userInfo.nickname());
            resp.setUserAvatar(userInfo.avatar());
        }

        resp.setImages(getNoteImageUrls(note.getId()));
        resp.setProducts(getNoteProducts(note.getId()));

        if (currentUserId != null) {
            resp.setIsLiked(isLikedBy(note.getId(), currentUserId));
            resp.setIsFavorited(isFavoritedBy(note.getId(), currentUserId));
        }

        return resp;
    }

    private NoteDetailResponse buildDetail(Note note, Long currentUserId) {
        NoteDetailResponse resp = new NoteDetailResponse();
        resp.setId(note.getId());
        resp.setUserId(note.getUserId());
        resp.setTitle(note.getTitle());
        resp.setContent(note.getContent());
        resp.setCoverUrl(note.getCoverUrl());
        resp.setStatus(note.getStatus());
        resp.setLikeCount(note.getLikeCount());
        resp.setCommentCount(note.getCommentCount());
        resp.setFavoriteCount(note.getFavoriteCount());
        resp.setShareCount(note.getShareCount());
        resp.setViewCount(note.getViewCount());
        resp.setLocationName(note.getLocationName());
        resp.setCreateTime(note.getCreateTime());
        resp.setUpdateTime(note.getUpdateTime());

        var userInfoMap = externalService.batchGetUserInfo(List.of(note.getUserId()));
        var userInfo = userInfoMap.get(note.getUserId());
        if (userInfo != null) {
            resp.setUserNickname(userInfo.nickname());
            resp.setUserAvatar(userInfo.avatar());
        }

        resp.setImages(getNoteImageItems(note.getId()));
        resp.setProducts(getNoteProducts(note.getId()));

        if (currentUserId != null) {
            resp.setIsLiked(isLikedBy(note.getId(), currentUserId));
            resp.setIsFavorited(isFavoritedBy(note.getId(), currentUserId));
        }

        return resp;
    }

    private List<String> getNoteImageUrls(Long noteId) {
        LambdaQueryWrapper<NoteImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteImage::getNoteId, noteId);
        wrapper.orderByAsc(NoteImage::getSortOrder);
        return noteImageMapper.selectList(wrapper).stream()
                .map(NoteImage::getImageUrl)
                .collect(Collectors.toList());
    }

    private List<NoteDetailResponse.NoteImageItem> getNoteImageItems(Long noteId) {
        LambdaQueryWrapper<NoteImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteImage::getNoteId, noteId);
        wrapper.orderByAsc(NoteImage::getSortOrder);
        return noteImageMapper.selectList(wrapper).stream()
                .map(img -> {
                    var item = new NoteDetailResponse.NoteImageItem();
                    item.setId(img.getId());
                    item.setImageUrl(img.getImageUrl());
                    item.setSortOrder(img.getSortOrder());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<NoteResponse.ProductItem> getNoteProducts(Long noteId) {
        LambdaQueryWrapper<NoteProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteProduct::getNoteId, noteId);
        List<Long> productIds = noteProductMapper.selectList(wrapper).stream()
                .map(NoteProduct::getProductId)
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return List.of();
        }

        var productInfoMap = externalService.batchGetProductInfo(productIds);
        return productInfoMap.values().stream().map(pi -> {
            var item = new NoteResponse.ProductItem();
            item.setId(pi.id());
            item.setName(pi.name());
            item.setMainImage(pi.mainImage());
            item.setSalePrice(pi.salePrice());
            return item;
        }).collect(Collectors.toList());
    }

    private boolean isLikedBy(Long noteId, Long userId) {
        LambdaQueryWrapper<NoteLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteLike::getNoteId, noteId);
        wrapper.eq(NoteLike::getUserId, userId);
        return noteLikeMapper.selectCount(wrapper) > 0;
    }

    private boolean isFavoritedBy(Long noteId, Long userId) {
        LambdaQueryWrapper<NoteFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteFavorite::getNoteId, noteId);
        wrapper.eq(NoteFavorite::getUserId, userId);
        return noteFavoriteMapper.selectCount(wrapper) > 0;
    }

    private String limitContent(String content, int maxLen) {
        if (content == null) {
            return null;
        }
        return content.length() > maxLen ? content.substring(0, maxLen) + "..." : content;
    }
}
