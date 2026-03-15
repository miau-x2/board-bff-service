document.addEventListener("DOMContentLoaded", () => {
    const setFieldError = (input, feedback, message) => {
        input.classList.add("is-invalid");
        if (feedback) feedback.textContent = message;
    };

    const clearFieldError = (input, feedback) => {
        input.classList.remove("is-invalid");
        if (feedback) feedback.textContent = "";
    };

    const validateDisplayOrder = (input, feedback) => {
        const raw = input.value.trim();
        if (!raw) {
            setFieldError(input, feedback, "디스플레이 순서를 입력해주세요.");
            return false;
        }
        const value = Number(raw);
        if (!Number.isInteger(value) || value < 0) {
            setFieldError(input, feedback, "디스플레이 순서는 0 이상이어야 합니다.");
            return false;
        }
        if (value > 255) {
            setFieldError(input, feedback, "디스플레이 순서는 최대 255입니다.");
            return false;
        }
        clearFieldError(input, feedback);
        return true;
    };

    const addModalEl = document.getElementById("categoryAddModal");
    if (addModalEl) {
        const addModal = new bootstrap.Modal(addModalEl);
        const addForm = document.getElementById("categoryAddForm");
        const addTitle = document.getElementById("categoryAddModalTitle");
        const parentIdInput = document.getElementById("modalParentId");
        const nameInput = document.getElementById("modalName");
        const slugInput = document.getElementById("modalSlug");
        const orderInput = document.getElementById("modalAddDisplayOrder");
        const nameFeedback = document.getElementById("modalNameFeedback");
        const slugFeedback = document.getElementById("modalSlugFeedback");
        const orderFeedback = document.getElementById("modalAddDisplayOrderFeedback");

        const validateName = () => {
            const value = nameInput.value.trim();
            if (!value) {
                setFieldError(nameInput, nameFeedback, "카테고리 이름을 입력해주세요.");
                return false;
            }
            if (value.length > 50) {
                setFieldError(nameInput, nameFeedback, "카테고리 이름은 1~50자입니다.");
                return false;
            }
            clearFieldError(nameInput, nameFeedback);
            return true;
        };

        const validateSlug = () => {
            const value = slugInput.value.trim();
            if (!value) {
                setFieldError(slugInput, slugFeedback, "카테고리 슬러그를 입력해주세요.");
                return false;
            }
            if (value.length > 50) {
                setFieldError(slugInput, slugFeedback, "카테고리 슬러그는 1~50자입니다.");
                return false;
            }
            clearFieldError(slugInput, slugFeedback);
            return true;
        };

        const resetAddModal = () => {
            parentIdInput.value = "";
            nameInput.value = "";
            slugInput.value = "";
            orderInput.value = "";
            clearFieldError(nameInput, nameFeedback);
            clearFieldError(slugInput, slugFeedback);
            clearFieldError(orderInput, orderFeedback);
        };

        const openRootAdd = () => {
            resetAddModal();
            addTitle.textContent = "상위 카테고리 추가";
            addModal.show();
            nameInput.focus();
        };

        const openChildAdd = (parentId, parentName) => {
            resetAddModal();
            parentIdInput.value = parentId ?? "";
            addTitle.textContent = `${parentName} 하위 카테고리 추가`;
            addModal.show();
            nameInput.focus();
        };

        document.querySelector(".js-open-root-add")?.addEventListener("click", openRootAdd);
        document.querySelectorAll(".js-open-child-add").forEach((button) => {
            button.addEventListener("click", () => openChildAdd(button.dataset.parentId, button.dataset.parentName));
        });

        nameInput.addEventListener("blur", validateName);
        slugInput.addEventListener("blur", validateSlug);
        orderInput.addEventListener("blur", () => validateDisplayOrder(orderInput, orderFeedback));

        addForm.addEventListener("submit", (event) => {
            const valid = validateName() && validateSlug() && validateDisplayOrder(orderInput, orderFeedback);
            if (valid) return;
            event.preventDefault();
        });

        if (addModalEl.dataset.open === "true") {
            addTitle.textContent = addModalEl.dataset.mode === "CHILD_ADD" ? "하위 카테고리 추가" : "상위 카테고리 추가";
            addModal.show();
        }
    }

    const editModalEl = document.getElementById("displayOrderEditModal");
    if (editModalEl) {
        const editModal = new bootstrap.Modal(editModalEl);
        const editForm = document.getElementById("displayOrderEditForm");
        const editTitle = document.getElementById("displayOrderEditModalTitle");
        const editCategoryIdInput = document.getElementById("modalEditCategoryId");
        const editOrderInput = document.getElementById("modalEditDisplayOrder");
        const editOrderFeedback = document.getElementById("modalEditDisplayOrderFeedback");

        const openDisplayOrderEdit = (categoryId, categoryName, displayOrder) => {
            clearFieldError(editOrderInput, editOrderFeedback);
            editCategoryIdInput.value = categoryId ?? "";
            editOrderInput.value = displayOrder ?? "";
            editTitle.textContent = `${categoryName} 순서 수정`;
            editModal.show();
            editOrderInput.focus();
        };

        document.querySelectorAll(".js-open-edit").forEach((button) => {
            button.addEventListener("click", () => {
                openDisplayOrderEdit(button.dataset.categoryId, button.dataset.categoryName, button.dataset.displayOrder);
            });
        });

        editOrderInput.addEventListener("blur", () => validateDisplayOrder(editOrderInput, editOrderFeedback));
        editForm.addEventListener("submit", (event) => {
            if (validateDisplayOrder(editOrderInput, editOrderFeedback)) return;
            event.preventDefault();
        });

        if (editModalEl.dataset.open === "true") {
            editModal.show();
        }
    }
});
