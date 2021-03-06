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
package org.sonar.server.db;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.core.persistence.DbSession;
import org.sonar.core.persistence.MyBatis;
import org.sonar.core.persistence.TestDatabase;
import org.sonar.core.qualityprofile.db.QualityProfileDao;
import org.sonar.server.qualityprofile.persistence.ActiveRuleDao;
import org.sonar.server.rule2.persistence.RuleDao;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DbClientTest {

  @Rule
  public TestDatabase db = new TestDatabase();

  @Test
  public void facade() throws Exception {
    MyBatis myBatis = db.myBatis();
    RuleDao ruleDao = mock(RuleDao.class);
    ActiveRuleDao activeRuleDao = mock(ActiveRuleDao.class);
    QualityProfileDao qualityProfileDao = mock(QualityProfileDao.class);

    DbClient client = new DbClient(db.database(), myBatis, ruleDao, activeRuleDao, qualityProfileDao);

    assertThat(client.database()).isSameAs(db.database());
    DbSession dbSession = client.openSession(true);
    assertThat(dbSession).isNotNull();
    assertThat(dbSession.getConnection().isClosed()).isFalse();
    dbSession.close();

    // DAO
    assertThat(client.ruleDao()).isSameAs(ruleDao);
    assertThat(client.activeRuleDao()).isSameAs(activeRuleDao);
    assertThat(client.qualityProfileDao()).isSameAs(qualityProfileDao);
  }
}
