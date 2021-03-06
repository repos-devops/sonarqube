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
package org.sonar.server.search;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.terms.TermsFacet;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Result<K> {

  private final Collection<K> hits;
  private final Map<String, Collection<FacetValue>> facets;
  private final long total;
  private final long timeInMillis;

  public Result(SearchResponse response) {
    this.hits = new ArrayList<K>();
    this.total = (int) response.getHits().totalHits();
    this.timeInMillis = response.getTookInMillis();

    for (SearchHit hit : response.getHits()) {
      this.hits.add(getSearchResult(hit));
    }

    if (response.getFacets() != null &&
      !response.getFacets().facets().isEmpty()) {
      this.facets = new HashMap<String, Collection<FacetValue>>();
      for (Facet facet : response.getFacets().facets()) {
        TermsFacet termFacet = (TermsFacet) facet;
        List<FacetValue> facetValues = new ArrayList<FacetValue>();
        for (TermsFacet.Entry facetValue : termFacet.getEntries()) {
          facetValues.add(new FacetValue<Integer>(facetValue.getTerm().string(),
            facetValue.getCount()));
        }
        this.facets.put(facet.getName(), facetValues);
      }
    } else {
      this.facets = Collections.emptyMap();
    }
  }

  /* Transform Methods */

  protected abstract K getSearchResult(Map<String, Object> fields);

  protected K getSearchResult(SearchHit hit) {
    Map<String, Object> fields = new HashMap<String, Object>();
    for (Map.Entry<String, SearchHitField> field : hit.getFields().entrySet()) {
      fields.put(field.getKey(), field.getValue().getValue());
    }
    return this.getSearchResult(fields);
  }

  public Collection<K> getHits() {
    return hits;
  }

  public long getTotal() {
    return total;
  }

  public long getTimeInMillis() {
    return timeInMillis;
  }

  public Map<String, Collection<FacetValue>> getFacets() {
    return this.facets;
  }

  @CheckForNull
  public Collection<FacetValue> getFacet(String facetName) {
    return this.facets.get(facetName);
  }

  @CheckForNull
  public Collection<String> getFacetKeys(String facetName) {
    if (this.facets.containsKey(facetName)) {
      List<String> keys = new ArrayList<String>();
      for (FacetValue facetValue : facets.get(facetName)) {
        keys.add(facetValue.getKey());
      }
      return keys;
    }
    return null;
  }

  @CheckForNull
  public Object getFacetTermValue(String facetName, String key) {
    if (this.facets.containsKey(facetName)) {
      for (FacetValue facetValue : facets.get(facetName)) {
        if (facetValue.getKey().equals(key)) {
          return facetValue.getValue();
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }
}
