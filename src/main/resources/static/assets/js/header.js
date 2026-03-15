document.addEventListener("DOMContentLoaded", function () {
    const links = document.querySelectorAll(".category-root-link");
    links.forEach((link) => {
        link.addEventListener("click", function () {
            links.forEach((item) => item.classList.remove("active", "fw-bold"));
            link.classList.add("active", "fw-bold");
        });
    });

    const currentUrl = window.location.href;

    const loginLinks = document.querySelectorAll(".js-login-link");
    loginLinks.forEach((loginLink) => {
        const basePath = loginLink.dataset.loginPath || "/login";
        const loginUrl = new URL(basePath, window.location.origin);
        loginUrl.searchParams.set("redirect", currentUrl);
        loginLink.setAttribute("href", loginUrl.toString());
    });

    const returnUrlInputs = document.querySelectorAll(".js-return-to-input");
    returnUrlInputs.forEach((input) => {
        input.value = currentUrl;
    });
});
