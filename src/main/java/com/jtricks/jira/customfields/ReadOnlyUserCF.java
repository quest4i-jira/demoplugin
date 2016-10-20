package com.jtricks.jira.customfields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Scanned
public class ReadOnlyUserCF extends GenericTextCFType {

    private static final Logger log = LoggerFactory.getLogger(ReadOnlyUserCF.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;

    protected ReadOnlyUserCF(
            @ComponentImport
            CustomFieldValuePersister customFieldValuePersister,
            @ComponentImport
            GenericConfigManager genericConfigManager,
            @ComponentImport
            TextFieldCharacterLengthValidator textFieldCharacterLengthValidator,
            @ComponentImport
            JiraAuthenticationContext jiraAuthenticationContext) {

        super(customFieldValuePersister, genericConfigManager,
                textFieldCharacterLengthValidator, jiraAuthenticationContext);
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }


    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue,
                                                     final CustomField field,
                                                     final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
        params.put("currentUser", jiraAuthenticationContext.getLoggedInUser().getName());

        // This method is also called to get the default value, in
        // which case issue is null so we can't use it to add currencyLocale
        if (issue == null) {
            return params;
        }

        FieldConfig fieldConfig = field.getRelevantConfig(issue);
        //add what you need to the params here

        return params;
    }
}