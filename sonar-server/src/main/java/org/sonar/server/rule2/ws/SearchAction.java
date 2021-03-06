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
package org.sonar.server.rule2.ws;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.io.Resources;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.utils.text.JsonWriter;
import org.sonar.server.qualityprofile.ActiveRule;
import org.sonar.server.rule2.Rule;
import org.sonar.server.rule2.RuleParam;
import org.sonar.server.rule2.RuleService;
import org.sonar.server.rule2.index.RuleIndex;
import org.sonar.server.rule2.index.RuleNormalizer;
import org.sonar.server.rule2.index.RuleQuery;
import org.sonar.server.rule2.index.RuleResult;
import org.sonar.server.search.QueryOptions;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since 4.4
 */
public class SearchAction implements RequestHandler {

  private static final String PARAM_TEXT_QUERY = "q";
  private static final String PARAM_REPOSITORIES = "repositories";
  private static final String PARAM_SEVERITIES = "severities";
  private static final String PARAM_STATUSES = "statuses";
  private static final String PARAM_LANGUAGES = "languages";
  private static final String PARAM_DEBT_CHARACTERISTICS = "debt_characteristics";
  private static final String PARAM_HAS_DEBT_CHARACTERISTIC = "has_debt_characteristic";
  private static final String PARAM_TAGS = "tags";
  private static final String PARAM_ALL_OF_TAGS = "all_of_tags";

  // generic search parameters
  private static final String PARAM_PAGE = "p";
  private static final String PARAM_PAGE_SIZE = "ps";
  private static final String PARAM_FIELDS = "f";
  private static final String PARAM_SORT = "s";
  private static final String PARAM_ASCENDING = "asc";

  private final RuleService service;

  public SearchAction(RuleService service) {
    this.service = service;
  }

  void define(WebService.NewController controller) {
    WebService.NewAction action = controller
      .createAction("search")
      .setDescription("Search for a collection of relevant rules matching a specified query")
      .setResponseExample(Resources.getResource(getClass(), "example-search.json"))
      .setSince("4.4")
      .setHandler(this);

    defineSearchParameters(action);

    action
      .createParam(PARAM_FIELDS)
      .setDescription("Comma-separated list of the fields to be returned in response. All the fields are returned by default.")
      .setPossibleValues(RuleIndex.PUBLIC_FIELDS)
      .setExampleValue(String.format("%s,%s,%s", RuleNormalizer.RuleField.KEY, RuleNormalizer.RuleField.REPOSITORY, RuleNormalizer.RuleField.LANGUAGE));

    action
      .createParam(PARAM_PAGE)
      .setDescription("1-based page number")
      .setExampleValue("42")
      .setDefaultValue("1");

    action
      .createParam(PARAM_PAGE_SIZE)
      .setDescription("Page size. Must be greater than 0.")
      .setExampleValue("10")
      .setDefaultValue("25");

    // TODO limit the fields to sort on + document possible values + default value ?
    action
      .createParam(PARAM_SORT)
      .setDescription("Sort field")
      .setExampleValue(RuleNormalizer.RuleField.LANGUAGE.key());

    action
      .createParam(PARAM_ASCENDING)
      .setDescription("Ascending sort")
      .setBooleanPossibleValues()
      .setDefaultValue("true");
  }

  public static void defineSearchParameters(WebService.NewAction action) {
    action
      .createParam(PARAM_TEXT_QUERY)
      .setDescription("UTF-8 search query")
      .setExampleValue("null pointer");

    action
      .createParam(PARAM_REPOSITORIES)
      .setDescription("Comma-separated list of repositories")
      .setExampleValue("checkstyle,findbugs");

    action
      .createParam(PARAM_SEVERITIES)
      .setDescription("Comma-separated list of default severities. Not the same than severity of rules in Quality profiles.")
      .setPossibleValues(Severity.ALL)
      .setExampleValue("CRITICAL,BLOCKER");

    action
      .createParam(PARAM_LANGUAGES)
      .setDescription("Comma-separated list of languages")
      .setExampleValue("java,js");

    action
      .createParam(PARAM_STATUSES)
      .setDescription("Comma-separated list of status codes")
      .setPossibleValues(RuleStatus.values())
      .setExampleValue(RuleStatus.READY.toString());

    action
      .createParam(PARAM_DEBT_CHARACTERISTICS)
      .setDescription("Comma-separated list of technical debt characteristics or sub-characteristics")
      .setExampleValue("RELIABILITY");

    action
      .createParam(PARAM_HAS_DEBT_CHARACTERISTIC)
      .setDescription("Filter rules that have a technical debt characteristic")
      .setBooleanPossibleValues();

    action
      .createParam(PARAM_TAGS)
      .setDescription("Comma-separated list of tags. Returned rules match any of the tags (OR operator)")
      .setExampleValue("security,java8");

    action
      .createParam(PARAM_ALL_OF_TAGS)
      .setDescription("Comma-separated list of tags. Returned rules match all the tags (AND operator)")
      .setExampleValue("security,java8");

    action
      .createParam("qprofile")
      .setDescription("Key of Quality profile")
      .setExampleValue("java:Sonar way");

    action
      .createParam("activation")
      .setDescription("Used only if 'qprofile' is set")
      .setExampleValue("java:Sonar way")
      .setPossibleValues("false", "true", "all");
  }

  @Override
  public void handle(Request request, Response response) {
    RuleQuery query = service.newRuleQuery();
    query.setQueryText(request.param(PARAM_TEXT_QUERY));
    query.setSeverities(request.paramAsStrings(PARAM_SEVERITIES));
    query.setRepositories(request.paramAsStrings(PARAM_REPOSITORIES));
    query.setStatuses(toStatuses(request.paramAsStrings(PARAM_STATUSES)));
    query.setLanguages(request.paramAsStrings(PARAM_LANGUAGES));
    query.setDebtCharacteristics(request.paramAsStrings(PARAM_DEBT_CHARACTERISTICS));
    query.setHasDebtCharacteristic(request.paramAsBoolean(PARAM_HAS_DEBT_CHARACTERISTIC));

    // TODO move to QueryOptions ?
    query.setSortField(RuleQuery.SortField.valueOfOrNull(request.param(PARAM_SORT)));
    query.setAscendingSort(request.mandatoryParamAsBoolean(PARAM_ASCENDING));

    QueryOptions options = new QueryOptions();
    options.setFieldsToReturn(request.paramAsStrings(PARAM_FIELDS));
    options.setPage(
      request.mandatoryParamAsInt(PARAM_PAGE),
      request.mandatoryParamAsInt(PARAM_PAGE_SIZE));

    RuleResult results = service.search(query, options);

    JsonWriter json = response.newJsonWriter().beginObject();
    writeStatistics(results, json);
    writeRequestParams(request, json);
    writeRules(results, json);
    json.endObject();
    json.close();
  }

  private void writeStatistics(RuleResult results, JsonWriter json) {
    json.prop("total", results.getTotal());
  }

  private void writeRequestParams(Request request,JsonWriter json ){
    json.prop(PARAM_PAGE, request.mandatoryParamAsInt(PARAM_PAGE));
    json.prop(PARAM_PAGE_SIZE, request.mandatoryParamAsInt(PARAM_PAGE_SIZE));
  }
  private void writeRules(RuleResult result, JsonWriter json) {

    json.name("rules").beginArray();
    for (Rule rule : result.getHits()) {
      json.beginObject();
      json
        .prop("repo", rule.key().repository())
        .prop("key", rule.key().toString())
        .prop("lang", rule.language())
        .prop("name", rule.name())
        .prop("htmlDesc", rule.htmlDescription())
        .prop("status", rule.status().toString())
        .prop("template", rule.template())
        .prop("internalKey", rule.internalKey())
        .prop("severity", rule.severity())
        .name("tags").beginArray().values(rule.tags()).endArray()
        .name("sysTags").beginArray().values(rule.systemTags()).endArray();
      if(rule.debtSubCharacteristicKey() != null && !rule.debtSubCharacteristicKey().isEmpty()){
        json
          .prop("debtSubCharacteristicKey", rule.debtSubCharacteristicKey());
      }
      if(rule.debtRemediationFunction() != null){
        json
          .prop("debtRemediationFunctionType", rule.debtRemediationFunction().type().name())
          .prop("debtRemediationFunctionCoefficient", rule.debtRemediationFunction().coefficient())
          .prop("debtRemediationFunctionOffset", rule.debtRemediationFunction().offset());
      }
      json.name("params").beginArray();
      for (RuleParam param : rule.params()) {
        json
          .beginObject()
          .prop("key", param.key())
          .prop("desc", param.description())
          .prop("defaultValue", param.defaultValue())
          .endObject();
      }
      json.endArray();
      json.name("actives").beginArray();

      for (ActiveRule activeRule : result.getActiveRules()) {
        json
          .beginObject()
          .prop("key",activeRule.key().toString())
          .prop("inherit", activeRule.inheritance().name())
          .prop("severity", activeRule.severity());
        if(activeRule.parentKey() != null){
          json.prop("parent",activeRule.parentKey().toString());
        }

        json
          .name("params").beginArray();
        for (Map.Entry<String, String> param : activeRule.params().entrySet()) {
          json.beginObject()
            .prop("key", param.getKey())
            .prop("value", param.getValue())
            .endObject();
        }
        json.endArray();
        json.endObject();
      }
      json.endArray();
      json.endObject();
    }
    json.endArray();
  }


  @CheckForNull
  private Collection<RuleStatus> toStatuses(@Nullable List<String> statuses) {
    if (statuses == null) {
      return null;
    }
    return Collections2.transform(statuses, new Function<String, RuleStatus>() {
      @Override
      public RuleStatus apply(@Nullable String input) {
        return input == null ? null : RuleStatus.valueOf(input);
      }
    });
  }
}
