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
package org.sonar.colorizer;

import java.util.Set;

public class CaseInsensitiveKeywordsTokenizer extends KeywordsTokenizer {

  public CaseInsensitiveKeywordsTokenizer(String tagBefore, String tagAfter, Set<String> keywords) {
    super(tagBefore, tagAfter, keywords);
    setCaseInsensitive(true);
  }

  public CaseInsensitiveKeywordsTokenizer(String tagBefore, String tagAfter, String... keywords) {
    super(tagBefore, tagAfter, keywords);
    setCaseInsensitive(true);
  }

  @Override
  public KeywordsTokenizer clone() {
    KeywordsTokenizer clone = super.clone();
    clone.setCaseInsensitive(true);
    return clone;
  }
}
