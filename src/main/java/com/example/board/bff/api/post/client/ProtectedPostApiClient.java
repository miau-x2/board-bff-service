package com.example.board.bff.api.post.client;

import com.example.board.bff.commons.response.ApiResponse;
import com.example.board.bff.config.ProtectedFeignConfig;
import com.example.board.bff.controller.dto.request.CategoryAddRequest;
import com.example.board.bff.controller.dto.request.DisplayOrderUpdateRequest;
import com.example.board.bff.controller.dto.response.CategoryTreeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "post-service", contextId = "protectedPostApiClient", configuration = ProtectedFeignConfig.class)
public interface ProtectedPostApiClient {
    @PostMapping("/api/admin/categories")
    ApiResponse<Void> addCategory(@RequestHeader("Authorization") String authorization, @RequestBody CategoryAddRequest request);

    @GetMapping("/api/admin/categories/tree")
    ApiResponse<CategoryTreeResponse> getCategoryTree(@RequestHeader("Authorization") String authorization);

    @PostMapping("/api/admin/categories/display-order")
    ApiResponse<Void> updateDisplayOrder(@RequestHeader("Authorization") String authorization, @RequestBody DisplayOrderUpdateRequest request);
}
