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
package org.sonar.server.rule2.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.Rule;
import org.sonar.api.utils.DateUtils;
import org.sonar.api.utils.System2;
import org.sonar.check.Cardinality;
import org.sonar.core.persistence.AbstractDaoTestCase;
import org.sonar.core.persistence.DbSession;
import org.sonar.core.rule.RuleDto;
import org.sonar.core.rule.RuleParamDto;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuleDaoTest extends AbstractDaoTestCase {

  private RuleDao dao;
  private DbSession session;
  private System2 system2;

  @Before
  public void before() throws Exception {
    this.session = getMyBatis().openSession(false);
    this.system2 = mock(System2.class);
    this.dao = new RuleDao(system2);
  }

  @After
  public void after() {
    this.session.close();
  }

  @Test
  public void select_all() throws Exception {
    setupData("selectAll");
    List<RuleDto> ruleDtos = dao.findAll(session);

    assertThat(ruleDtos).hasSize(1);

    RuleDto ruleDto = ruleDtos.get(0);
    assertThat(ruleDto.getId()).isEqualTo(1);
    assertThat(ruleDto.getName()).isEqualTo("Avoid Null");
    assertThat(ruleDto.getDescription()).isEqualTo("Should avoid NULL");
    assertThat(ruleDto.getStatus()).isEqualTo(Rule.STATUS_READY);
    assertThat(ruleDto.getRepositoryKey()).isEqualTo("checkstyle");
    assertThat(ruleDto.getNoteData()).isEqualTo("Rule note with accents \u00e9\u00e8\u00e0");
    assertThat(ruleDto.getSubCharacteristicId()).isEqualTo(100);
    assertThat(ruleDto.getDefaultSubCharacteristicId()).isEqualTo(101);
    assertThat(ruleDto.getRemediationFunction()).isEqualTo("linear");
    assertThat(ruleDto.getDefaultRemediationFunction()).isEqualTo("linear_offset");
    assertThat(ruleDto.getRemediationCoefficient()).isEqualTo("1h");
    assertThat(ruleDto.getDefaultRemediationCoefficient()).isEqualTo("5d");
    assertThat(ruleDto.getRemediationOffset()).isEqualTo("5min");
    assertThat(ruleDto.getDefaultRemediationOffset()).isEqualTo("10h");
    assertThat(ruleDto.getEffortToFixDescription()).isEqualTo("squid.S115.effortToFix");
  }

  @Test
  public void select_enables_and_non_manual() throws Exception {
    setupData("select_enables_and_non_manual");
    List<RuleDto> ruleDtos = dao.findByEnabledAndNotManual(session);

    assertThat(ruleDtos.size()).isEqualTo(1);
    RuleDto ruleDto = ruleDtos.get(0);
    assertThat(ruleDto.getId()).isEqualTo(1);
    assertThat(ruleDto.getName()).isEqualTo("Avoid Null");
    assertThat(ruleDto.getDescription()).isEqualTo("Should avoid NULL");
    assertThat(ruleDto.getStatus()).isEqualTo(Rule.STATUS_READY);
    assertThat(ruleDto.getRepositoryKey()).isEqualTo("checkstyle");
    assertThat(ruleDto.getNoteData()).isEqualTo("Rule note with accents \u00e9\u00e8\u00e0");
    assertThat(ruleDto.getSubCharacteristicId()).isEqualTo(100);
    assertThat(ruleDto.getDefaultSubCharacteristicId()).isEqualTo(101);
    assertThat(ruleDto.getRemediationFunction()).isEqualTo("LINEAR");
    assertThat(ruleDto.getDefaultRemediationFunction()).isEqualTo("LINEAR_OFFSET");
    assertThat(ruleDto.getRemediationCoefficient()).isEqualTo("1h");
    assertThat(ruleDto.getDefaultRemediationCoefficient()).isEqualTo("5d");
    assertThat(ruleDto.getRemediationOffset()).isEqualTo("5min");
    assertThat(ruleDto.getDefaultRemediationOffset()).isEqualTo("10h");
    assertThat(ruleDto.getEffortToFixDescription()).isEqualTo("squid.S115.effortToFix");
  }

  @Test
  public void select_by_id() throws Exception {
    setupData("selectById");
    RuleDto ruleDto = dao.getById(2, session);

    assertThat(ruleDto.getId()).isEqualTo(2);
    assertThat(ruleDto.getName()).isEqualTo("Avoid Null");
    assertThat(ruleDto.getDescription()).isEqualTo("Should avoid NULL");
    assertThat(ruleDto.getStatus()).isEqualTo(Rule.STATUS_READY);
    assertThat(ruleDto.getRepositoryKey()).isEqualTo("checkstyle");
  }

  @Test
  public void select_by_rule_key() throws Exception {
    setupData("select_by_rule_key");
    assertThat(dao.getByKey(RuleKey.of("checkstyle", "AvoidComparison"), session)).isNotNull();
    assertThat(dao.getByKey(RuleKey.of("checkstyle", "Unknown"), session)).isNull();
    assertThat(dao.getByKey(RuleKey.of("Unknown", "AvoidComparison"), session)).isNull();
  }

  @Test
  public void select_by_name() throws Exception {
    setupData("select_by_name");
    RuleDto ruleDto = dao.getByName("Avoid Null", session);

    assertThat(ruleDto).isNotNull();

    assertThat(ruleDto.getId()).isEqualTo(2);
    assertThat(ruleDto.getName()).isEqualTo("Avoid Null");
    assertThat(ruleDto.getDescription()).isEqualTo("Should avoid NULL");
    assertThat(ruleDto.getStatus()).isEqualTo(Rule.STATUS_READY);
    assertThat(ruleDto.getRepositoryKey()).isEqualTo("checkstyle");
  }

  @Test
  public void select_non_manual() throws Exception {
    setupData("selectNonManual");
    List<RuleDto> ruleDtos = dao.findByNonManual(session);
    session.commit();
    session.close();

    assertThat(ruleDtos.size()).isEqualTo(1);
    RuleDto ruleDto = ruleDtos.get(0);
    assertThat(ruleDto.getId()).isEqualTo(1);
    assertThat(ruleDto.getName()).isEqualTo("Avoid Null");
    assertThat(ruleDto.getDescription()).isEqualTo("Should avoid NULL");
    assertThat(ruleDto.getStatus()).isEqualTo(Rule.STATUS_READY);
    assertThat(ruleDto.getRepositoryKey()).isEqualTo("checkstyle");
  }

//  @Test
//  public void select_by_sub_characteristic_id(){
//    setupData("select_by_sub_characteristic_id");
//
//    // Rules from sub characteristic (even REMOVED ones are returned)
//    List<RuleDto> ruleDtos = dao.findBySubCharacteristicId(3);
//    assertThat(ruleDtos).hasSize(3);
//    assertThat(idsFromRuleDtos(ruleDtos)).containsExactly(2, 4, 5);
//
//    // Nothing on root characteristic
//    ruleDtos = dao.selectBySubCharacteristicId(1);
//    assertThat(ruleDtos).isEmpty();
//
//    // Rules from disabled characteristic
//    ruleDtos = dao.selectBySubCharacteristicId(11);
//    assertThat(idsFromRuleDtos(ruleDtos)).containsExactly(3);
//  }

  @Test
  public void update() {

    when(system2.now()).thenReturn(DateUtils.parseDate("2014-01-01").getTime());

    setupData("update");

    RuleDto ruleToUpdate = new RuleDto()
      .setId(1)
      .setRuleKey("NewRuleKey")
      .setRepositoryKey("plugin")
      .setName("new name")
      .setDescription("new description")
      .setStatus(Rule.STATUS_DEPRECATED)
      .setConfigKey("NewConfigKey")
      .setSeverity(Severity.INFO)
      .setCardinality(Cardinality.MULTIPLE)
      .setLanguage("dart")
      .setParentId(3)
      .setNoteData("My note")
      .setNoteUserLogin("admin")
      .setNoteCreatedAt(DateUtils.parseDate("2013-12-19"))
      .setNoteUpdatedAt(DateUtils.parseDate("2013-12-20"))
      .setSubCharacteristicId(100)
      .setDefaultSubCharacteristicId(101)
      .setRemediationFunction("linear")
      .setDefaultRemediationFunction("linear_offset")
      .setRemediationCoefficient("1h")
      .setDefaultRemediationCoefficient("5d")
      .setRemediationOffset("5min")
      .setDefaultRemediationOffset("10h")
      .setEffortToFixDescription("squid.S115.effortToFix");


    dao.update(ruleToUpdate, session);
    session.commit();

    checkTables("update", "rules");
  }

  @Test
  public void insert() {

    when(system2.now()).thenReturn(DateUtils.parseDate("2013-12-16").getTime());

    setupData("empty");

    RuleDto ruleToInsert = new RuleDto()
      .setId(1)
      .setRuleKey("NewRuleKey")
      .setRepositoryKey("plugin")
      .setName("new name")
      .setDescription("new description")
      .setStatus(Rule.STATUS_DEPRECATED)
      .setConfigKey("NewConfigKey")
      .setSeverity(Severity.INFO)
      .setCardinality(Cardinality.MULTIPLE)
      .setLanguage("dart")
      .setParentId(3)
      .setSubCharacteristicId(100)
      .setDefaultSubCharacteristicId(101)
      .setRemediationFunction("linear")
      .setDefaultRemediationFunction("linear_offset")
      .setRemediationCoefficient("1h")
      .setDefaultRemediationCoefficient("5d")
      .setRemediationOffset("5min")
      .setDefaultRemediationOffset("10h")
      .setEffortToFixDescription("squid.S115.effortToFix");

    dao.insert(ruleToInsert, session);
    session.commit();

    checkTables("insert", "rules");
  }

  @Test
  public void insert_all() {
    when(system2.now()).thenReturn(DateUtils.parseDate("2013-12-16").getTime());

    setupData("empty");

    RuleDto ruleToInsert1 = new RuleDto()
      .setId(1)
      .setRuleKey("NewRuleKey")
      .setRepositoryKey("plugin")
      .setName("new name")
      .setDescription("new description")
      .setStatus(Rule.STATUS_DEPRECATED)
      .setConfigKey("NewConfigKey")
      .setSeverity(Severity.INFO)
      .setCardinality(Cardinality.MULTIPLE)
      .setLanguage("dart")
      .setParentId(3)
      .setSubCharacteristicId(100)
      .setDefaultSubCharacteristicId(101)
      .setRemediationFunction("linear")
      .setDefaultRemediationFunction("linear_offset")
      .setRemediationCoefficient("1h")
      .setDefaultRemediationCoefficient("5d")
      .setRemediationOffset("5min")
      .setDefaultRemediationOffset("10h")
      .setEffortToFixDescription("squid.S115.effortToFix");

    RuleDto ruleToInsert2 = new RuleDto()
      .setId(2)
      .setRuleKey("NewRuleKey2")
      .setRepositoryKey("plugin2")
      .setName("new name2")
      .setDescription("new description2")
      .setStatus(Rule.STATUS_BETA)
      .setConfigKey("NewConfigKey2")
      .setSeverity(Severity.MAJOR)
      .setCardinality(Cardinality.SINGLE)
      .setLanguage("js")
      .setParentId(null)
      .setSubCharacteristicId(102)
      .setDefaultSubCharacteristicId(103)
      .setRemediationFunction("linear_offset")
      .setDefaultRemediationFunction("linear")
      .setRemediationCoefficient("5d")
      .setDefaultRemediationCoefficient("1h")
      .setRemediationOffset("10h")
      .setDefaultRemediationOffset("5min")
      .setEffortToFixDescription("squid.S115.effortToFix2");

    dao.insert(ImmutableList.of(ruleToInsert1, ruleToInsert2), session);
    session.commit();

    checkTables("insert_all", "rules");
  }

  @Test
  public void select_parameters() throws Exception {
    setupData("selectParameters");
    List<RuleParamDto> ruleDtos = dao.findAllRuleParams(session);

    assertThat(ruleDtos.size()).isEqualTo(1);
    RuleParamDto ruleDto = ruleDtos.get(0);
    assertThat(ruleDto.getId()).isEqualTo(1);
    assertThat(ruleDto.getName()).isEqualTo("myParameter");
    assertThat(ruleDto.getDescription()).isEqualTo("My Parameter");
    assertThat(ruleDto.getType()).isEqualTo("plop");
    assertThat(ruleDto.getDefaultValue()).isEqualTo("plouf");
  }

  @Test
  public void select_parameters_by_rule_id() throws Exception {
    setupData("select_parameters_by_rule_id");
    RuleDto rule = dao.getById(1, session);
    List<RuleParamDto> ruleDtos = dao.findRuleParamsByRuleKey(rule.getKey(), session);

    assertThat(ruleDtos.size()).isEqualTo(1);
    RuleParamDto ruleDto = ruleDtos.get(0);
    assertThat(ruleDto.getId()).isEqualTo(1);
    assertThat(ruleDto.getName()).isEqualTo("myParameter");
    assertThat(ruleDto.getDescription()).isEqualTo("My Parameter");
    assertThat(ruleDto.getType()).isEqualTo("plop");
    assertThat(ruleDto.getRuleId()).isEqualTo(1);
  }

  @Test
  public void select_parameters_by_rule_ids() throws Exception {
    setupData("select_parameters_by_rule_ids");

    RuleDto rule1 = dao.getById(1, session);
    RuleDto rule2 = dao.getById(2, session);
    assertThat(dao.findRuleParamsByRules(newArrayList(rule1, rule2), session)).hasSize(2);
    assertThat(dao.findRuleParamsByRules(newArrayList(rule1), session)).hasSize(1);
  }

  @Test
  public void insert_parameter() {
    setupData("insert_parameter");

    RuleDto rule1 = dao.getById(1, session);

    RuleParamDto param = RuleParamDto.createFor(rule1)
      .setName("max")
      .setType("INTEGER")
      .setDefaultValue("30")
      .setDescription("My Parameter");

    dao.addRuleParam(rule1, param, session);
    session.commit();

    checkTables("insert_parameter", "rules_parameters");
  }

  @Test
  public void update_parameter() {
    setupData("update_parameter");

    RuleDto rule1 = dao.getById(1, session);

    List<RuleParamDto> params = dao.findRuleParamsByRuleKey(rule1.getKey(), session);
    assertThat(params).hasSize(1);

    RuleParamDto param = Iterables.getFirst(params, null);

    param.setName("format")
      .setType("STRING")
      .setDefaultValue("^[a-z]+(\\.[a-z][a-z0-9]*)*$")
      .setDescription("Regular expression used to check the package names against.");

    dao.updateRuleParam(rule1, param, session);
    session.commit();

    checkTables("update_parameter", "rules_parameters");
  }

  @Test
  public void select_tags_by_rule() throws Exception {
    setupData("select_tags_by_rule_id");

    RuleDto rule = dao.getById(1, session);
    assertThat(rule.getSystemTags()).hasSize(2);
    assertThat(rule.getTags()).hasSize(3);
    assertThat(rule.getTags()).containsOnly("tag1", "tag2","tag3");
    assertThat(rule.getSystemTags()).containsOnly("systag1", "systag2");

  }
}
