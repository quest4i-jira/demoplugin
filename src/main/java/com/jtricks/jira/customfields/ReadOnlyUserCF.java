package com.jtricks.jira.customfields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


@Scanned
public class ReadOnlyUserCF extends GenericTextCFType {

    private static final Logger log = LoggerFactory.getLogger(ReadOnlyUserCF.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;

    private final ChangeHistoryManager changeHistoryManager;

    protected ReadOnlyUserCF(
            @ComponentImport
                    CustomFieldValuePersister customFieldValuePersister,
            @ComponentImport
                    GenericConfigManager genericConfigManager,
            @ComponentImport
                    TextFieldCharacterLengthValidator textFieldCharacterLengthValidator,
            @ComponentImport
                    JiraAuthenticationContext jiraAuthenticationContext,
            @ComponentImport ChangeHistoryManager changeHistoryManager) {

        super(customFieldValuePersister, genericConfigManager,
                textFieldCharacterLengthValidator, jiraAuthenticationContext);
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.changeHistoryManager = changeHistoryManager;
    }


    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue,
                                                     final CustomField field,
                                                     final FieldLayoutItem fieldLayoutItem) {

        final Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
        String currentUser = jiraAuthenticationContext.getLoggedInUser().getName();
        params.put("currentUser", currentUser);

        if (issue.isCreated()) {
            Optional<List<ChangeHistory>> optHistory = Optional.ofNullable(
                    changeHistoryManager.getChangeHistories(issue));
            if (optHistory.isPresent()) {
                Collections.reverse(optHistory.get());
                String lastModifier = optHistory.get().get(0).getAuthorObject().getName();
                log.warn("01 lastModifier : " + lastModifier);
                params.put("lastModifier", lastModifier);
            }
        }

        return params;
    }
}