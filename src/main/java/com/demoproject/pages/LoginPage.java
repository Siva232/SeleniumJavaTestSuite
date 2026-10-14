package com.demoproject.pages;

import org.openqa.selenium.By;

/**
 * Login practice page ({@code app.login.url}).
 */
public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By SIGN_IN = By.id("signInBtn");
    private static final String ERROR_MESSAGE_CSS = ".alert-danger";

    /**
     * @return this page, opened
     */
    public LoginPage open() {
        navigateTo(config.getLoginUrl());
        wait.visible(USERNAME);
        return this;
    }

    /**
     * @param username user name (may be empty)
     * @param password password (may be empty, never logged)
     */
    public void login(String username, String password) {
        type(USERNAME, username);
        typeSecret(PASSWORD, password);
        watchTransientText(ERROR_MESSAGE_CSS);
        click(SIGN_IN);
    }

    /** Logs in with {@code app.username} / {@code app.password} from config. */
    public void loginWithConfiguredUser() {
        login(config.getAppUsername(), config.getAppPassword());
    }

    /**
     * The page shows the banner for only about two seconds, so its text is captured in the browser
     * as soon as it appears (see {@link #watchTransientText(String)}).
     *
     * @return the error banner text
     */
    public String getErrorMessage() {
        return waitForTransientText(ERROR_MESSAGE_CSS);
    }

    /**
     * @param urlFragment part of the landing page URL
     * @return true once the browser has navigated there
     */
    public boolean isRedirectedTo(String urlFragment) {
        return wait.urlContains(urlFragment);
    }
}
