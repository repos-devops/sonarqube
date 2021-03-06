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
package org.sonar.core.component;

import org.junit.Test;
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.Library;
import org.sonar.api.resources.Project;

import static org.fest.assertions.Assertions.assertThat;

public class ComponentKeysTest {

  @Test
  public void shouldCreateUID() {
    Project project = new Project("my_project");
    assertThat(ComponentKeys.createEffectiveKey(project, project)).isEqualTo("my_project");

    Directory dir = Directory.create("src/org/foo", "org/foo");
    assertThat(ComponentKeys.createEffectiveKey(project, dir)).isEqualTo("my_project:src/org/foo");

    Library library = new Library("junit:junit", "4.7");
    assertThat(ComponentKeys.createEffectiveKey(project, library)).isEqualTo("junit:junit");
  }

  @Test
  public void isValidModuleKey() {
    assertThat(ComponentKeys.isValidModuleKey("")).isFalse();
    assertThat(ComponentKeys.isValidModuleKey("abc")).isTrue();
    assertThat(ComponentKeys.isValidModuleKey("0123")).isFalse();
    assertThat(ComponentKeys.isValidModuleKey("ab 12")).isFalse();
    assertThat(ComponentKeys.isValidModuleKey("ab_12")).isTrue();
    assertThat(ComponentKeys.isValidModuleKey("ab/12")).isFalse();
  }

  @Test
  public void isValidBranchKey() {
    assertThat(ComponentKeys.isValidBranch("")).isTrue();
    assertThat(ComponentKeys.isValidBranch("abc")).isTrue();
    assertThat(ComponentKeys.isValidBranch("0123")).isTrue();
    assertThat(ComponentKeys.isValidBranch("ab 12")).isFalse();
    assertThat(ComponentKeys.isValidBranch("ab_12")).isTrue();
    assertThat(ComponentKeys.isValidBranch("ab/12")).isFalse();
  }

}
