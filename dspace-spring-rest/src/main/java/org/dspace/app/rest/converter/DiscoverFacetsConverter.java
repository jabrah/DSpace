/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.rest.model.SearchFacetEntryRest;
import org.dspace.app.rest.model.SearchFacetValueRest;
import org.dspace.app.rest.model.SearchResultsRest;
import org.dspace.app.rest.parameter.SearchFilter;
import org.dspace.core.Context;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoverySearchFilterFacet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class DiscoverFacetsConverter {

    private static final Logger log = Logger.getLogger(DiscoverFacetsConverter.class);

    private DiscoverFacetValueConverter facetValueConverter = new DiscoverFacetValueConverter();

    @Autowired
    private SearchService searchService;

    public SearchResultsRest convert(Context context, String query, String dsoType, String configurationName,
                                     String dsoScope, List<SearchFilter> searchFilters, final Pageable page,
                                     DiscoveryConfiguration configuration, DiscoverResult searchResult) {

        SearchResultsRest searchResultsRest = new SearchResultsRest();

        setRequestInformation(context, query, dsoType, configurationName, dsoScope, searchFilters, page,
                              searchResultsRest);
        addFacetValues(context, searchResult, searchResultsRest, configuration);

        return searchResultsRest;
    }


    private void addFacetValues(Context context, final DiscoverResult searchResult,
                                final SearchResultsRest searchResultsRest,
                                final DiscoveryConfiguration configuration) {

        List<DiscoverySearchFilterFacet> facets = configuration.getSidebarFacets();
        for (DiscoverySearchFilterFacet field : CollectionUtils.emptyIfNull(facets)) {

            List<DiscoverResult.FacetResult> facetValues = searchResult.getFacetResult(field);

            SearchFacetEntryRest facetEntry = new SearchFacetEntryRest(field.getIndexFieldName());
            int valueCount = 0;
            facetEntry.setFacetLimit(field.getFacetLimit());
            facetEntry.setExposeMinMax(field.exposeMinAndMaxValue());
            if (field.exposeMinAndMaxValue()) {
                handleExposeMinMaxValues(context,field,facetEntry);
            }
            for (DiscoverResult.FacetResult value : CollectionUtils.emptyIfNull(facetValues)) {
                //The discover results contains max facetLimit + 1 values. If we reach the "+1", indicate that there are
                //more results available.
                if (valueCount < field.getFacetLimit()) {
                    SearchFacetValueRest valueRest = facetValueConverter.convert(value);
                    facetEntry.addValue(valueRest);
                } else {
                    facetEntry.setHasMore(true);
                }

                if (StringUtils.isBlank(facetEntry.getFacetType())) {
                    facetEntry.setFacetType(value.getFieldType());
                }

                valueCount++;
            }

            searchResultsRest.addFacetEntry(facetEntry);
        }
    }

    /**
      * This method will fill the facetEntry with the appropriate min and max values if they're not empty
      * @param context       The relevant DSpace context
      * @param field         The DiscoverySearchFilterFacet field to search for this value in solr
      * @param facetEntry    The SearchFacetEntryRest facetEntry for which this needs to be filled in
      */
    private void handleExposeMinMaxValues(Context context,DiscoverySearchFilterFacet field,
                                          SearchFacetEntryRest facetEntry) {
        try {
            String minValue = searchService.calculateExtremeValue(context,
                                                                  field.getIndexFieldName() + "_min",
                                                                  field.getIndexFieldName() + "_min_sort",
                                                                  DiscoverQuery.SORT_ORDER.asc);
            String maxValue = searchService.calculateExtremeValue(context,
                                                                  field.getIndexFieldName() + "_max",
                                                                  field.getIndexFieldName() + "_max_sort",
                                                                  DiscoverQuery.SORT_ORDER.desc);

            if (StringUtils.isNotBlank(minValue) && StringUtils.isNotBlank(maxValue)) {
                facetEntry.setMinValue(minValue);
                facetEntry.setMaxValue(maxValue);
            }
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }
    }


    private void setRequestInformation(final Context context, final String query, final String dsoType,
                                       final String configurationName, final String scope,
                                       final List<SearchFilter> searchFilters, final Pageable page,
                                       final SearchResultsRest resultsRest) {
        resultsRest.setQuery(query);
        resultsRest.setConfigurationName(configurationName);
        resultsRest.setDsoType(dsoType);
        resultsRest.setSort(SearchResultsRest.Sorting.fromPage(page));

        resultsRest.setScope(scope);

        SearchFilterToAppliedFilterConverter searchFilterToAppliedFilterConverter = new
            SearchFilterToAppliedFilterConverter();
        for (SearchFilter searchFilter : CollectionUtils.emptyIfNull(searchFilters)) {

            resultsRest
                .addAppliedFilter(searchFilterToAppliedFilterConverter.convertSearchFilter(context, searchFilter));
        }
    }
}
