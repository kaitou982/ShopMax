package com.shop.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.community.controller.request.NoteCreateRequest;
import com.shop.community.controller.request.NoteUpdateRequest;
import com.shop.community.controller.response.NoteDetailResponse;
import com.shop.community.controller.response.NoteResponse;
import com.shop.community.entity.*;
import com.shop.community.mapper.*;
import com.shop.community.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserInfoMapper userInfoMapper;

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
    public NoteDetailResponse getDetail(Long noteId) {
        Note note = getNoteById(noteId);
        baseMapper.increaseViewCount(noteId, 1);
        return buildDetail(note, null);
    }

    @Override
    public PageResult<NoteResponse> page(Integer pageNum, Integer pageSize, String tab, Long currentUserId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, NoteStatus.PUBLISHED.getCode());
        wrapper.orderByDesc(Note::getCreateTime);

        if ("following".equals(tab) && currentUserId != null) {
            List<Long> followingIds = userInfoMapper.selectFollowingUserIds(currentUserId);
            if (followingIds.isEmpty()) {
                return PageResult.empty();
            }
            wrapper.in(Note::getUserId, followingIds);
        }

        Page<Note> page = new Page<>(pageNum, pageSize);
        Page<Note> result = baseMapper.selectPage(page, wrapper);

        List<NoteResponse> records = result.getRecords().stream()
                .map(note -> buildResponse(note, currentUserId))
                .collect(Collectors.toList());

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

        List<NoteResponse> records = result.getRecords().stream()
                .map(note -> buildResponse(note, null))
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
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

        var userInfo = userInfoMapper.selectUserInfo(note.getUserId());
        if (userInfo != null) {
            resp.setUserNickname(userInfo.getNickname());
            resp.setUserAvatar(userInfo.getAvatar());
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

        var userInfo = userInfoMapper.selectUserInfo(note.getUserId());
        if (userInfo != null) {
            resp.setUserNickname(userInfo.getNickname());
            resp.setUserAvatar(userInfo.getAvatar());
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

        return userInfoMapper.selectProductItems(productIds);
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
