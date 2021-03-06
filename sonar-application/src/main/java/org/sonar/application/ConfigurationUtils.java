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
package org.sonar.application;

import org.apache.commons.lang.text.StrSubstitutor;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public final class ConfigurationUtils {

  private ConfigurationUtils() {
    // Utility class
  }

  static Properties interpolateEnvVariables(Properties properties) {
    return interpolateVariables(properties, System.getenv());
  }

  static Properties interpolateVariables(Properties properties, Map<String, String> variables) {
    Properties result = new Properties();
    Enumeration keys = properties.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      String value = (String) properties.get(key);
      String interpolatedValue = StrSubstitutor.replace(value, variables, "${env:", "}");
      result.setProperty(key, interpolatedValue);
    }
    return result;
  }
}
