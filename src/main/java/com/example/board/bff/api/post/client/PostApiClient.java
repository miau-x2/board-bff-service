package com.example.board.bff.api.post.client;

import com.example.board.bff.commons.response.ApiResponse;
import com.example.board.bff.controller.dto.response.CategoryHeaderMenuResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "post-service", contextId = "postApiClient")
public interface PostApiClient {
    @GetMapping("/api/header-menu")
    ApiResponse<CategoryHeaderMenuResponse> getHeaderMenu();
}
