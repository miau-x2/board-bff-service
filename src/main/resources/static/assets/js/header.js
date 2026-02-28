document.addEventListener("DOMContentLoaded", function () {
    const links = document.querySelectorAll(".category-link");
    links.forEach((link) => {
        link.addEventListener("click", function (event) {
            event.preventDefault();
            links.forEach((item) => item.classList.remove("active", "fw-bold"));
            link.classList.add("active", "fw-bold");
        });
    });

    const currentUrl = window.location.href;

    const loginLinks = document.querySelectorAll(".js-login-link");
    loginLinks.forEach((loginLink) => {
        const basePath = loginLink.dataset.loginPath || "/login";
        const loginUrl = new URL(basePath, window.location.origin);
        loginUrl.searchParams.set("returnTo", currentUrl);
        loginLink.setAttribute("href", loginUrl.toString());
    });

    const returnToInputs = document.querySelectorAll(".js-return-to-input");
    returnToInputs.forEach((input) => {
        input.value = currentUrl;
    });
});