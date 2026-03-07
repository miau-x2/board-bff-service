(() => {
    const passwordInput = document.getElementById("password");
    const togglePasswordBtn = document.getElementById("togglePasswordBtn");
    const togglePasswordIcon = document.getElementById("togglePasswordIcon");

    if (!passwordInput || !togglePasswordBtn || !togglePasswordIcon) {
        return;
    }

    togglePasswordBtn.addEventListener("click", () => {
        const showPassword = passwordInput.type === "password";
        passwordInput.type = showPassword ? "text" : "password";
        togglePasswordIcon.className = showPassword ? "bi bi-eye" : "bi bi-eye-slash";
        togglePasswordBtn.setAttribute("aria-pressed", String(showPassword));
    });
})();
