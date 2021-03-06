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
package org.sonar.server.qualityprofile;

import com.google.common.collect.Maps;
import org.sonar.core.qualityprofile.db.ActiveRuleDto;
import org.sonar.core.qualityprofile.db.ActiveRuleParamDto;
import org.sonar.core.qualityprofile.db.QualityProfileDto;
import org.sonar.core.rule.RuleDto;
import org.sonar.core.rule.RuleParamDto;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

class RuleActivationContext {

  private RuleDto rule;
  private Map<String, RuleParamDto> ruleParams;
  private QualityProfileDto profile, parentProfile;
  private ActiveRuleDto activeRule, parentActiveRule;
  private Map<String, ActiveRuleParamDto> activeRuleParams, parentActiveRuleParams;

  RuleDto rule() {
    return rule;
  }

  RuleActivationContext setRule(RuleDto rule) {
    this.rule = rule;
    return this;
  }

  Map<String, RuleParamDto> ruleParamsByKeys() {
    return ruleParams;
  }

  Collection<RuleParamDto> ruleParams() {
    return ruleParams.values();
  }

  RuleActivationContext setRuleParams(Collection<RuleParamDto> ruleParams) {
    this.ruleParams = Maps.newHashMap();
    for (RuleParamDto ruleParam : ruleParams) {
      this.ruleParams.put(ruleParam.getName(), ruleParam);
    }
    return this;
  }

  QualityProfileDto profile() {
    return profile;
  }

  RuleActivationContext setProfile(QualityProfileDto profile) {
    this.profile = profile;
    return this;
  }

  @CheckForNull
  QualityProfileDto parentProfile() {
    return parentProfile;
  }

  RuleActivationContext setParentProfile(@Nullable QualityProfileDto p) {
    this.parentProfile = p;
    return this;
  }

  @CheckForNull
  ActiveRuleDto activeRule() {
    return activeRule;
  }

  RuleActivationContext setActiveRule(@Nullable ActiveRuleDto a) {
    this.activeRule = a;
    return this;
  }

  @CheckForNull
  ActiveRuleDto parentActiveRule() {
    return parentActiveRule;
  }

  RuleActivationContext setParentActiveRule(@Nullable ActiveRuleDto a) {
    this.parentActiveRule = a;
    return this;
  }

  @CheckForNull
  Map<String, ActiveRuleParamDto> activeRuleParamsAsMap() {
    return activeRuleParams;
  }

  @CheckForNull
  Collection<ActiveRuleParamDto> activeRuleParams() {
    return activeRuleParams != null ? activeRuleParams.values() : null;
  }

  RuleActivationContext setActiveRuleParams(@Nullable Collection<ActiveRuleParamDto> a) {
    if (a == null) {
      this.activeRuleParams = null;
    } else {
      this.activeRuleParams = Maps.newHashMap();
      for (ActiveRuleParamDto ar : a) {
        this.activeRuleParams.put(ar.getKey(), ar);
      }
    }
    return this;
  }

  @CheckForNull
  Map<String, ActiveRuleParamDto> parentActiveRuleParams() {
    return parentActiveRuleParams;
  }

  RuleActivationContext setParentActiveRuleParams(@Nullable Collection<ActiveRuleParamDto> a) {
    if (a == null) {
      this.parentActiveRuleParams = null;
    } else {
      this.parentActiveRuleParams = Maps.newHashMap();
      for (ActiveRuleParamDto ar : a) {
        this.parentActiveRuleParams.put(ar.getKey(), ar);
      }
    }
    return this;
  }

  String defaultSeverity() {
    return parentActiveRule != null ? parentActiveRule.getSeverityString() : rule.getSeverityString();
  }
}
