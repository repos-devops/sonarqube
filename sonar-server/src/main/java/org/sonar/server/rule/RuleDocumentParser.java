/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.rule;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.joda.time.format.ISODateTimeFormat;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.debt.internal.DefaultDebtRemediationFunction;
import org.sonar.api.server.rule.RuleParamType;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @deprecated to be dropped in 4.4
 */
@Deprecated
public class RuleDocumentParser {

  private RuleDocumentParser() {
    // Utility class
  }

  public static Rule parse(Map<String, Object> ruleSource) {
    Rule.Builder ruleBuilder = new Rule.Builder()
      .setId((Integer) ruleSource.get(RuleDocument.FIELD_ID))
      .setKey((String) ruleSource.get(RuleDocument.FIELD_KEY))
      .setLanguage((String) ruleSource.get(RuleDocument.FIELD_LANGUAGE))
      .setRepositoryKey((String) ruleSource.get(RuleDocument.FIELD_REPOSITORY_KEY))
      .setSeverity((String) ruleSource.get(RuleDocument.FIELD_SEVERITY))
      .setName((String) ruleSource.get(RuleDocument.FIELD_NAME))
      .setDescription((String) ruleSource.get(RuleDocument.FIELD_DESCRIPTION))
      .setStatus((String) ruleSource.get(RuleDocument.FIELD_STATUS))
      .setCardinality((String) ruleSource.get("cardinality"))
      .setTemplateId((Integer) ruleSource.get(RuleDocument.FIELD_TEMPLATE_ID))
      .setCreatedAt(parseOptionalDate(RuleDocument.FIELD_CREATED_AT, ruleSource))
      .setUpdatedAt(parseOptionalDate(RuleDocument.FIELD_UPDATED_AT, ruleSource));

    addRuleDebt(ruleSource, ruleBuilder);
    addRuleNote(ruleSource, ruleBuilder);
    addRuleParams(ruleSource, ruleBuilder);
    addRuleTags(ruleSource, ruleBuilder);
    return ruleBuilder.build();
  }

  private static void addRuleDebt(Map<String, Object> ruleSource, Rule.Builder ruleBuilder) {
    if (ruleSource.containsKey(RuleDocument.FIELD_CHARACTERISTIC_KEY)) {
      ruleBuilder
        .setDebtCharacteristicKey((String) ruleSource.get(RuleDocument.FIELD_CHARACTERISTIC_KEY))
        .setDebtSubCharacteristicKey((String) ruleSource.get(RuleDocument.FIELD_SUB_CHARACTERISTIC_KEY))
        .setDebtRemediationFunction(
          new DefaultDebtRemediationFunction(DebtRemediationFunction.Type.valueOf((String) ruleSource.get(RuleDocument.FIELD_REMEDIATION_FUNCTION)),
            (String) ruleSource.get(RuleDocument.FIELD_REMEDIATION_COEFFICIENT),
            (String) ruleSource.get(RuleDocument.FIELD_REMEDIATION_OFFSET))
        )
      ;
    }
  }

  private static void addRuleNote(Map<String, Object> ruleSource, Rule.Builder ruleBuilder) {
    if (ruleSource.containsKey(RuleDocument.FIELD_NOTE)) {
      Map<String, Object> ruleNoteDocument = (Map<String, Object>) ruleSource.get(RuleDocument.FIELD_NOTE);
      ruleBuilder.setRuleNote(new RuleNote(
        (String) ruleNoteDocument.get(RuleDocument.FIELD_NOTE_DATA),
        (String) ruleNoteDocument.get(RuleDocument.FIELD_NOTE_USER_LOGIN),
        parseOptionalDate(RuleDocument.FIELD_NOTE_CREATED_AT, ruleNoteDocument),
        parseOptionalDate(RuleDocument.FIELD_NOTE_UPDATED_AT, ruleNoteDocument)
      ));
    }
  }

  private static void addRuleParams(Map<String, Object> ruleSource, Rule.Builder ruleBuilder) {
    List<RuleParam> params = Lists.newArrayList();
    if (ruleSource.containsKey(RuleDocument.FIELD_PARAMS)) {
      Map<String, Map<String, Object>> ruleParams = Maps.newHashMap();
      for (Map<String, Object> ruleParam : (List<Map<String, Object>>) ruleSource.get(RuleDocument.FIELD_PARAMS)) {
        ruleParams.put((String) ruleParam.get(RuleDocument.FIELD_PARAM_KEY), ruleParam);
      }
      for (Map.Entry<String, Map<String, Object>> ruleParam : ruleParams.entrySet()) {
        RuleParamType type = RuleParamType.parse((String) ruleParam.getValue().get(RuleDocument.FIELD_PARAM_TYPE));
        params.add(new RuleParam(
          (String) ruleParam.getValue().get(RuleDocument.FIELD_PARAM_KEY),
          (String) ruleParam.getValue().get(RuleDocument.FIELD_PARAM_DESCRIPTION),
          (String) ruleParam.getValue().get(RuleDocument.FIELD_PARAM_DEFAULT_VALUE),
          type
        ));
      }
    }
    ruleBuilder.setParams(params);
  }

  private static void addRuleTags(Map<String, Object> ruleSource, Rule.Builder ruleBuilder) {
    List<String> systemTags = newArrayList();
    if (ruleSource.containsKey(RuleDocument.FIELD_SYSTEM_TAGS)) {
      for (String tag : (List<String>) ruleSource.get(RuleDocument.FIELD_SYSTEM_TAGS)) {
        systemTags.add(tag);
      }
    }
    ruleBuilder.setSystemTags(systemTags);

    List<String> adminTags = newArrayList();
    if (ruleSource.containsKey(RuleDocument.FIELD_ADMIN_TAGS)) {
      for (String tag : (List<String>) ruleSource.get(RuleDocument.FIELD_ADMIN_TAGS)) {
        adminTags.add(tag);
      }
    }
    ruleBuilder.setAdminTags(adminTags);
  }

  public static Date parseOptionalDate(String field, Map<String, Object> ruleSource) {
    String dateValue = (String) ruleSource.get(field);
    if (dateValue == null) {
      return null;
    } else {
      return ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(dateValue).toDate();
    }
  }
}

