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
package org.sonar.api.batch;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.MeasureUtils;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @deprecated since 2.1, a formula has been implemented on the metric, so no need to have decorator anymore
 * @since 1.10
 */
@Deprecated
public abstract class AbstractFileComplexityDecorator implements Decorator {

  private Language language;

  /**
   * @param language this will be use to defined whether the decorator should be executed on a project
   */
  public AbstractFileComplexityDecorator(Language language) {
    this.language = language;
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    return language.equals(project.getLanguage());
  }

  /**
   * Used to define upstream dependencies
   */
  @DependsUpon
  public List<Metric> dependsUponFileAndComplexityMetrics() {
    return Arrays.asList(CoreMetrics.FILES, CoreMetrics.COMPLEXITY);
  }

  /**
   * Used to define downstream dependencies
   */
  @DependedUpon
  public Metric generateFileComplexityMetric() {
    return CoreMetrics.FILE_COMPLEXITY;
  }

  /**
   * {@inheritDoc}
   */
  public void decorate(Resource resource, DecoratorContext context) {
    if (!shouldDecorateResource(resource, context)) {
      return;
    }
    Double files = MeasureUtils.getValue(context.getMeasure(CoreMetrics.FILES), null);
    Double complexity = MeasureUtils.getValue(context.getMeasure(CoreMetrics.COMPLEXITY), null);
    if (complexity != null && files != null && files > 0.0) {
      context.saveMeasure(CoreMetrics.FILE_COMPLEXITY, complexity / files);
    }
  }

  private boolean shouldDecorateResource(Resource resource, DecoratorContext context) {
    return !MeasureUtils.hasValue(context.getMeasure(CoreMetrics.FILE_COMPLEXITY)) && !ResourceUtils.isEntity(resource);
  }
}
