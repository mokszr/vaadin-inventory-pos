package com.muratoksuzer.vp.view.components;

import com.muratoksuzer.vp.service.TranslationService;
import com.vaadin.flow.component.login.LoginI18n;

public class LoginFormI18nSupport extends LoginI18n {

    public LoginFormI18nSupport(TranslationService translations) {

        Header i18nHeader = new Header();
        i18nHeader.setTitle(translations.t("login.title"));
        i18nHeader.setDescription(translations.t("login.additionalInfo"));
        setHeader(i18nHeader);

        Form i18nForm = new Form();
        i18nForm.setTitle(translations.t("login.title"));
        i18nForm.setUsername(translations.t("login.username"));
        i18nForm.setPassword(translations.t("login.password"));
        i18nForm.setSubmit(translations.t("login.submit"));
        i18nForm.setForgotPassword(translations.t("login.forgotPassword"));
        setForm(i18nForm);

        ErrorMessage i18nErrorMessage = new ErrorMessage();
        i18nErrorMessage.setTitle(translations.t("login.error.title"));
        i18nErrorMessage.setMessage(translations.t("login.error.message"));
        i18nErrorMessage.setUsername(translations.t("login.error.username"));
        i18nErrorMessage.setPassword(translations.t("login.error.password"));
        setErrorMessage(i18nErrorMessage);

        setAdditionalInformation(translations.t("login.additionalInfo"));

    }
}
