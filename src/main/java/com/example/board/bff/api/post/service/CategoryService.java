package com.example.board.bff.api.post.service;

import com.example.board.bff.api.exception.ErrorMessageConst;
import com.example.board.bff.api.exception.FeignExceptions;
import com.example.board.bff.api.exception.ProtectedApiClient;
import com.example.board.bff.api.post.client.PostApiClient;
import com.example.board.bff.api.post.client.ProtectedPostApiClient;
import com.example.board.bff.api.post.exception.PostCategoryAddErrorCode;
import com.example.board.bff.api.post.service.result.AddCategoryResult;
import com.example.board.bff.api.post.service.result.GetCategoryTreeResult;
import com.example.board.bff.api.post.service.result.UpdateDisplayOrderResult;
import com.example.board.bff.commons.response.ApiResponse;
import com.example.board.bff.controller.dto.request.CategoryAddRequest;
import com.example.board.bff.controller.dto.request.DisplayOrderUpdateRequest;
import com.example.board.bff.controller.dto.response.CategoryHeaderMenuItem;
import com.example.board.bff.controller.dto.response.CategoryHeaderMenuResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private static final String DEPTH_EXCEEDED_MESSAGE = "상위 카테고리는 1개의 하위 카테고리만 가질 수 있습니다.";
    private static final String PARENT_CATEGORY_NOT_FOUND_MESSAGE = "유효하지 않은 요청입니다. 다시 시도해주세요.";
    private static final String NAME_DUPLICATED_MESSAGE = "이미 사용 중인 카테고리 이름입니다.";
    private static final String SLUG_DUPLICATED_MESSAGE = "이미 사용 중인 슬러그입니다.";
    private static final String CATEGORY_NOT_FOUND_MESSAGE = "존재하지 않는 카테고리입니다.";

    private final PostApiClient postApiClient;
    private final ProtectedPostApiClient protectedPostApiClient;
    private final FeignExceptions feignExceptions;
    private final ProtectedApiClient protectedApiClient;

    @PreAuthorize("hasRole('ADMIN')")
    public AddCategoryResult addCategory(String sessionId, CategoryAddRequest request) {
        return protectedApiClient.execute(
                sessionId,
                authorization -> {
                    protectedPostApiClient.addCategory(authorization, request);
                    return new AddCategoryResult.Success();
                },
                e -> feignExceptions.extractErrorResponse(e)
                        .map(this::handleAddDownstreamError)
                        .orElse(new AddCategoryResult.SystemError(ErrorMessageConst.SYSTEM_ERROR_MESSAGE)),
                AddCategoryResult.Unauthorized::new,
                () -> new AddCategoryResult.SystemError(ErrorMessageConst.SYSTEM_ERROR_MESSAGE)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public GetCategoryTreeResult getCategoryTree(String sessionId) {
        return protectedApiClient.execute(
                sessionId,
                authorization -> {
                    var downstreamResponse = protectedPostApiClient.getCategoryTree(authorization);
                    return new GetCategoryTreeResult.Success(downstreamResponse.data());
                },
                e -> {
                    log.warn("다운 스트림 서비스 에러 발생: {}", e.getMessage());
                    return new GetCategoryTreeResult.SystemError(ErrorMessageConst.SYSTEM_ERROR_MESSAGE);
                },
                GetCategoryTreeResult.Unauthorized::new,
                () -> new GetCategoryTreeResult.SystemError(ErrorMessageConst.SYSTEM_ERROR_MESSAGE)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UpdateDisplayOrderResult updateDisplayOrder(String sessionId, DisplayOrderUpdateRequest request) {
        return protectedApiClient.execute(
                sessionId,
                authorization -> {
                    protectedPostApiClient.updateDisplayOrder(authorization, request);
                    return new UpdateDisplayOrderResult.Success();
                },
                e -> feignExceptions.extractErrorResponse(e)
                        .map(response -> {
                            if("POST_CATEGORY_404_002".equals(response.code())) {
                                return new UpdateDisplayOrderResult.NotFound(CATEGORY_NOT_FOUND_MESSAGE);
                            }
                            return new UpdateDisplayOrderResult.SystemError(ErrorMessageConst.SYSTEM_ERROR_MESSAGE);
                        })
                        .orElse(new UpdateDisplayOrderResult.SystemError(ErrorMessageConst.SYSTEM_ERROR_MESSAGE)),
                UpdateDisplayOrderResult.Unauthorized::new,
                () -> new UpdateDisplayOrderResult.SystemError(ErrorMessageConst.SYSTEM_ERROR_MESSAGE)
        );
    }

    public List<CategoryHeaderMenuItem> getHeaderMenuRoots() {
        try {
            var response = postApiClient.getHeaderMenu();
            if (response == null || response.data() == null || response.data().roots() == null) {
                return List.of();
            }
            return response.data().roots();
        } catch (Exception e) {
            log.error("헤더 메뉴 로딩 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private AddCategoryResult handleAddDownstreamError(ApiResponse<Void> response) {
        var code = PostCategoryAddErrorCode.from(response.code());
        if(code == null) {
            return new AddCategoryResult.SystemError(ErrorMessageConst.SYSTEM_ERROR_MESSAGE);
        }

        return switch (code) {
            case DEPTH_EXCEEDED -> new AddCategoryResult.DepthExceeded(DEPTH_EXCEEDED_MESSAGE);
            case PARENT_CATEGORY_NOT_FOUND -> new AddCategoryResult.ParentNotFound(PARENT_CATEGORY_NOT_FOUND_MESSAGE);
            case NAME_DUPLICATED -> new AddCategoryResult.NameDuplicated(NAME_DUPLICATED_MESSAGE);
            case SLUG_DUPLICATED -> new AddCategoryResult.SlugDuplicated(SLUG_DUPLICATED_MESSAGE);
        };
    }
}
