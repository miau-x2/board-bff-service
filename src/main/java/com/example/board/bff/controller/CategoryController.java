package com.example.board.bff.controller;

import com.example.board.bff.annotation.SessionId;
import com.example.board.bff.api.post.service.CategoryService;
import com.example.board.bff.api.post.service.result.AddCategoryResult;
import com.example.board.bff.api.post.service.result.GetCategoryTreeResult;
import com.example.board.bff.api.post.service.result.UpdateDisplayOrderResult;
import com.example.board.bff.controller.dto.request.CategoryAddRequest;
import com.example.board.bff.controller.dto.request.DisplayOrderUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.example.board.bff.api.utils.ErrorUtils.addFieldError;
import static com.example.board.bff.api.utils.ErrorUtils.addGlobalError;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class CategoryController {
    private static final String CATEGORY_VIEW = "category";
    private final CategoryService categoryService;

    @GetMapping("/categories")
    public String categoryForm(@SessionId String sessionId, Model model) {
        initializeFormModels(model);
        var redirect = populateCategoryTreeModel(sessionId, model, null);
        if (redirect != null) return redirect;
        return CATEGORY_VIEW;
    }

    @PostMapping("/categories")
    public String addCategory(
            @SessionId String sessionId,
            @Valid @ModelAttribute("categoryAddFormData") CategoryAddRequest request,
            BindingResult bindingResult,
            Model model) {
        initializeFormModels(model);

        if (bindingResult.hasErrors()) {
            prepareAddModalState(model, request);
            var redirect = populateCategoryTreeModel(sessionId, model, bindingResult);
            if (redirect != null) return redirect;
            return CATEGORY_VIEW;
        }

        var result = categoryService.addCategory(sessionId, request);
        return switch (result) {
            case AddCategoryResult.Success _ -> "redirect:/admin/categories";
            case AddCategoryResult.Unauthorized _ -> "redirect:/login";
            case AddCategoryResult.DepthExceeded(var message) -> {
                addGlobalError(bindingResult, "category.depthExceeded", message);
                prepareAddModalState(model, request);
                var redirect = populateCategoryTreeModel(sessionId, model, bindingResult);
                if (redirect != null) yield redirect;
                yield CATEGORY_VIEW;
            }
            case AddCategoryResult.ParentNotFound(var message) -> {
                addGlobalError(bindingResult, "category.parentNotFound", message);
                prepareAddModalState(model, request);
                var redirect = populateCategoryTreeModel(sessionId, model, bindingResult);
                if (redirect != null) yield redirect;
                yield CATEGORY_VIEW;
            }
            case AddCategoryResult.NameDuplicated(var message) -> {
                addFieldError(bindingResult, "name", "name.duplicate", message);
                prepareAddModalState(model, request);
                var redirect = populateCategoryTreeModel(sessionId, model, bindingResult);
                if (redirect != null) yield redirect;
                yield CATEGORY_VIEW;
            }
            case AddCategoryResult.SlugDuplicated(var message) -> {
                addFieldError(bindingResult, "slug", "slug.duplicate", message);
                prepareAddModalState(model, request);
                var redirect = populateCategoryTreeModel(sessionId, model, bindingResult);
                if (redirect != null) yield redirect;
                yield CATEGORY_VIEW;
            }
            case AddCategoryResult.SystemError(var message) -> {
                addGlobalError(bindingResult, "category.system", message);
                prepareAddModalState(model, request);
                var redirect = populateCategoryTreeModel(sessionId, model, bindingResult);
                if (redirect != null) yield redirect;
                yield CATEGORY_VIEW;
            }
        };
    }

    @PostMapping("/categories/display-order")
    public String editDisplayOrder(
            @SessionId String sessionId,
            @Valid @ModelAttribute("displayOrderEditFormData") DisplayOrderUpdateRequest request,
            BindingResult bindingResult,
            Model model) {
        initializeFormModels(model);

        if (bindingResult.hasErrors()) {
            prepareEditModalState(model);
            var redirect = populateCategoryTreeModel(sessionId, model, bindingResult);
            if (redirect != null) return redirect;
            return CATEGORY_VIEW;
        }

        var result = categoryService.updateDisplayOrder(sessionId, request);
        return switch (result) {
            case UpdateDisplayOrderResult.Success _ -> "redirect:/admin/categories";
            case UpdateDisplayOrderResult.Unauthorized _ -> "redirect:/login";
            case UpdateDisplayOrderResult.NotFound(var message) -> {
                addGlobalError(bindingResult, "category.displayOrder.notFound", message);
                prepareEditModalState(model);
                var redirect = populateCategoryTreeModel(sessionId, model, bindingResult);
                if (redirect != null) yield redirect;
                yield CATEGORY_VIEW;
            }
            case UpdateDisplayOrderResult.SystemError(var message) -> {
                addGlobalError(bindingResult, "category.displayOrder.system", message);
                prepareEditModalState(model);
                var redirect = populateCategoryTreeModel(sessionId, model, bindingResult);
                if (redirect != null) yield redirect;
                yield CATEGORY_VIEW;
            }
        };
    }

    private static void initializeFormModels(Model model) {
        if (!model.containsAttribute("categoryAddFormData")) {
            model.addAttribute("categoryAddFormData", CategoryAddRequest.empty());
        }
        if (!model.containsAttribute("displayOrderEditFormData")) {
            model.addAttribute("displayOrderEditFormData", DisplayOrderUpdateRequest.empty());
        }
    }

    private static void prepareAddModalState(Model model, CategoryAddRequest request) {
        model.addAttribute("openCategoryAddModal", true);
        model.addAttribute("categoryAddModalMode", request.parentId() == null ? "ROOT_ADD" : "CHILD_ADD");
    }

    private static void prepareEditModalState(Model model) {
        model.addAttribute("openDisplayOrderEditModal", true);
    }

    private String populateCategoryTreeModel(String sessionId, Model model, BindingResult bindingResult) {
        var treeResult = categoryService.getCategoryTree(sessionId);
        return switch (treeResult) {
            case GetCategoryTreeResult.Success(var categoryTree) -> {
                model.addAttribute("categoryTree", categoryTree);
                yield null;
            }
            case GetCategoryTreeResult.Unauthorized _ -> "redirect:/login";
            case GetCategoryTreeResult.SystemError(var message) -> {
                if (bindingResult != null) {
                    addGlobalError(bindingResult, "category.system", message);
                } else {
                    model.addAttribute("globalErrorMessage", message);
                }
                yield null;
            }
        };
    }
}
