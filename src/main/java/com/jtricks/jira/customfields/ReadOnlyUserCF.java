package com.jtricks.jira.customfields;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.sun.corba.se.spi.activation.IIOP_CLEAR_TEXT.value;
import static java.util.Optional.ofNullable;


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
            @ComponentImport
                    ChangeHistoryManager changeHistoryManager) {

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
        String value = jiraAuthenticationContext.getLoggedInUser().getName();
        params.put("value", value);

        if (issue.isCreated()) {
            Optional<List<ChangeHistory>> optHistory = ofNullable(
                    changeHistoryManager.getChangeHistories(issue));
            if (optHistory.isPresent() && (! optHistory.get().isEmpty())) {
                Collections.reverse(optHistory.get());
                value = optHistory.get().get(0).getAuthorObject().getName();
                log.warn("01 lastModifier : " + value);
                params.put("value", value);
            }
        }

        return params;
    }




    public void setValue(Issue issue, String new_value) {

        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        CustomField customField = customFieldManager.getCustomFieldObject("customfield_10102");
        String old_value = (String)issue.getCustomFieldValue(customField);
        ModifiedValue modifiedValue = new ModifiedValue(old_value, new_value);

        FieldLayoutManager fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
        FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue)
                .getFieldLayoutItem(customField);
        customField.updateValue(fieldLayoutItem, issue, modifiedValue, new DefaultIssueChangeHolder());
    }
}
