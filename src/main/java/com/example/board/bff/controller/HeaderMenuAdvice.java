package com.example.board.bff.controller;

import com.example.board.bff.api.post.service.CategoryService;
import com.example.board.bff.controller.dto.response.CategoryHeaderMenuItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class HeaderMenuAdvice {
    private final CategoryService categoryService;

    @ModelAttribute("headerMenuRoots")
    public List<CategoryHeaderMenuItem> headerMenuRoots() {
        return categoryService.getHeaderMenuRoots();
    }
}
