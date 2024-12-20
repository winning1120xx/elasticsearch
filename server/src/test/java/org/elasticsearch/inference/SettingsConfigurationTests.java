/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */

package org.elasticsearch.inference;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.inference.configuration.SettingsConfigurationDependency;
import org.elasticsearch.inference.configuration.SettingsConfigurationSelectOption;
import org.elasticsearch.inference.configuration.SettingsConfigurationValidation;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xcontent.ToXContent;
import org.elasticsearch.xcontent.XContentParser;
import org.elasticsearch.xcontent.XContentType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentHelper.toXContent;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertToXContentEquivalent;
import static org.hamcrest.CoreMatchers.equalTo;

public class SettingsConfigurationTests extends ESTestCase {

    public void testToXContent() throws IOException {
        String content = XContentHelper.stripWhitespace("""
            {
               "default_value": null,
               "depends_on": [
                 {
                   "field": "some_field",
                   "value": true
                 }
               ],
               "display": "textbox",
               "label": "Very important field",
               "options": [],
               "order": 4,
               "required": true,
               "sensitive": false,
               "tooltip": "Wow, this tooltip is useful.",
               "type": "str",
               "ui_restrictions": [],
               "validations": [
                  {
                     "constraint": 0,
                     "type": "greater_than"
                  }
               ],
               "value": ""
            }
            """);

        SettingsConfiguration configuration = SettingsConfiguration.fromXContentBytes(new BytesArray(content), XContentType.JSON);
        boolean humanReadable = true;
        BytesReference originalBytes = toShuffledXContent(configuration, XContentType.JSON, ToXContent.EMPTY_PARAMS, humanReadable);
        SettingsConfiguration parsed;
        try (XContentParser parser = createParser(XContentType.JSON.xContent(), originalBytes)) {
            parsed = SettingsConfiguration.fromXContent(parser);
        }
        assertToXContentEquivalent(originalBytes, toXContent(parsed, XContentType.JSON, humanReadable), XContentType.JSON);
    }

    public void testToXContent_WithNumericSelectOptions() throws IOException {
        String content = XContentHelper.stripWhitespace("""
            {
               "default_value": null,
               "depends_on": [
                 {
                   "field": "some_field",
                   "value": true
                 }
               ],
               "display": "textbox",
               "label": "Very important field",
               "options": [
                 {
                   "label": "five",
                   "value": 5
                 },
                 {
                   "label": "ten",
                   "value": 10
                 }
               ],
               "order": 4,
               "required": true,
               "sensitive": false,
               "tooltip": "Wow, this tooltip is useful.",
               "type": "str",
               "ui_restrictions": [],
               "validations": [
                  {
                     "constraint": 0,
                     "type": "greater_than"
                  }
               ],
               "value": ""
            }
            """);

        SettingsConfiguration configuration = SettingsConfiguration.fromXContentBytes(new BytesArray(content), XContentType.JSON);
        boolean humanReadable = true;
        BytesReference originalBytes = toShuffledXContent(configuration, XContentType.JSON, ToXContent.EMPTY_PARAMS, humanReadable);
        SettingsConfiguration parsed;
        try (XContentParser parser = createParser(XContentType.JSON.xContent(), originalBytes)) {
            parsed = SettingsConfiguration.fromXContent(parser);
        }
        assertToXContentEquivalent(originalBytes, toXContent(parsed, XContentType.JSON, humanReadable), XContentType.JSON);
    }

    public void testToXContentCrawlerConfig_WithNullValue() throws IOException {
        String content = XContentHelper.stripWhitespace("""
            {
               "label": "nextSyncConfig",
               "value": null
            }
            """);

        SettingsConfiguration configuration = SettingsConfiguration.fromXContentBytes(new BytesArray(content), XContentType.JSON);
        boolean humanReadable = true;
        BytesReference originalBytes = toShuffledXContent(configuration, XContentType.JSON, ToXContent.EMPTY_PARAMS, humanReadable);
        SettingsConfiguration parsed;
        try (XContentParser parser = createParser(XContentType.JSON.xContent(), originalBytes)) {
            parsed = SettingsConfiguration.fromXContent(parser);
        }
        assertToXContentEquivalent(originalBytes, toXContent(parsed, XContentType.JSON, humanReadable), XContentType.JSON);
    }

    public void testToXContentWithMultipleConstraintTypes() throws IOException {
        String content = XContentHelper.stripWhitespace("""
            {
               "default_value": null,
               "depends_on": [
                 {
                   "field": "some_field",
                   "value": true
                 }
               ],
               "display": "textbox",
               "label": "Very important field",
               "options": [],
               "order": 4,
               "required": true,
               "sensitive": false,
               "tooltip": "Wow, this tooltip is useful.",
               "type": "str",
               "ui_restrictions": [],
               "validations": [
                   {
                       "constraint": 32,
                       "type": "less_than"
                   },
                   {
                       "constraint": "^\\\\\\\\d{4}-\\\\\\\\d{2}-\\\\\\\\d{2}$",
                       "type": "regex"
                   },
                   {
                       "constraint": "int",
                       "type": "list_type"
                   },
                   {
                       "constraint": [
                           1,
                           2,
                           3
                       ],
                       "type": "included_in"
                   },
                   {
                       "constraint": [
                           "string_1",
                           "string_2",
                           "string_3"
                       ],
                       "type": "included_in"
                   }
               ],
               "value": ""
            }
            """);

        SettingsConfiguration configuration = SettingsConfiguration.fromXContentBytes(new BytesArray(content), XContentType.JSON);
        boolean humanReadable = true;
        BytesReference originalBytes = toShuffledXContent(configuration, XContentType.JSON, ToXContent.EMPTY_PARAMS, humanReadable);
        SettingsConfiguration parsed;
        try (XContentParser parser = createParser(XContentType.JSON.xContent(), originalBytes)) {
            parsed = SettingsConfiguration.fromXContent(parser);
        }
        assertToXContentEquivalent(originalBytes, toXContent(parsed, XContentType.JSON, humanReadable), XContentType.JSON);
    }

    public void testToMap() {
        SettingsConfiguration configField = SettingsConfigurationTestUtils.getRandomSettingsConfigurationField();
        Map<String, Object> configFieldAsMap = configField.toMap();

        if (configField.getCategory() != null) {
            assertThat(configFieldAsMap.get("category"), equalTo(configField.getCategory()));
        } else {
            assertFalse(configFieldAsMap.containsKey("category"));
        }

        assertThat(configFieldAsMap.get("default_value"), equalTo(configField.getDefaultValue()));

        if (configField.getDependsOn() != null) {
            List<Map<String, Object>> dependsOnAsList = configField.getDependsOn()
                .stream()
                .map(SettingsConfigurationDependency::toMap)
                .toList();
            assertThat(configFieldAsMap.get("depends_on"), equalTo(dependsOnAsList));
        } else {
            assertFalse(configFieldAsMap.containsKey("depends_on"));
        }

        if (configField.getDisplay() != null) {
            assertThat(configFieldAsMap.get("display"), equalTo(configField.getDisplay().toString()));
        } else {
            assertFalse(configFieldAsMap.containsKey("display"));
        }

        assertThat(configFieldAsMap.get("label"), equalTo(configField.getLabel()));

        if (configField.getOptions() != null) {
            List<Map<String, Object>> optionsAsList = configField.getOptions()
                .stream()
                .map(SettingsConfigurationSelectOption::toMap)
                .toList();
            assertThat(configFieldAsMap.get("options"), equalTo(optionsAsList));
        } else {
            assertFalse(configFieldAsMap.containsKey("options"));
        }

        if (configField.getOrder() != null) {
            assertThat(configFieldAsMap.get("order"), equalTo(configField.getOrder()));
        } else {
            assertFalse(configFieldAsMap.containsKey("order"));
        }

        if (configField.getPlaceholder() != null) {
            assertThat(configFieldAsMap.get("placeholder"), equalTo(configField.getPlaceholder()));
        } else {
            assertFalse(configFieldAsMap.containsKey("placeholder"));
        }

        assertThat(configFieldAsMap.get("required"), equalTo(configField.isRequired()));
        assertThat(configFieldAsMap.get("sensitive"), equalTo(configField.isSensitive()));

        if (configField.getTooltip() != null) {
            assertThat(configFieldAsMap.get("tooltip"), equalTo(configField.getTooltip()));
        } else {
            assertFalse(configFieldAsMap.containsKey("tooltip"));
        }

        if (configField.getType() != null) {
            assertThat(configFieldAsMap.get("type"), equalTo(configField.getType().toString()));
        } else {
            assertFalse(configFieldAsMap.containsKey("type"));
        }

        if (configField.getUiRestrictions() != null) {
            assertThat(configFieldAsMap.get("ui_restrictions"), equalTo(configField.getUiRestrictions()));
        } else {
            assertFalse(configFieldAsMap.containsKey("ui_restrictions"));
        }

        if (configField.getValidations() != null) {
            List<Map<String, Object>> validationsAsList = configField.getValidations()
                .stream()
                .map(SettingsConfigurationValidation::toMap)
                .toList();
            assertThat(configFieldAsMap.get("validations"), equalTo(validationsAsList));
        } else {
            assertFalse(configFieldAsMap.containsKey("validations"));
        }

        assertThat(configFieldAsMap.get("value"), equalTo(configField.getValue()));

    }
}
