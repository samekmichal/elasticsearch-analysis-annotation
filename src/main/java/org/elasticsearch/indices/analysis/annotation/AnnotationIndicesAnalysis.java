/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.indices.analysis.annotation;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.index.analysis.PreBuiltTokenFilterFactoryFactory;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.plugin.analysis.annotation.AnnotationAnalyzer;
import org.elasticsearch.plugin.analysis.annotation.InlineAnnotationFilter;

/**
 * Registers indices level analysis components so, if not explicitly configured,
 * will be shared among all indices.
 */
public class AnnotationIndicesAnalysis extends AbstractComponent {

	@Inject
	public AnnotationIndicesAnalysis(Settings settings,
			IndicesAnalysisService indicesAnalysisService) {
		super(settings);
		indicesAnalysisService.analyzerProviderFactories().put(
				"default",
				new PreBuiltAnalyzerProviderFactory("default",
						AnalyzerScope.INDICES, new AnnotationAnalyzer(
								Lucene.ANALYZER_VERSION)));

		indicesAnalysisService.tokenFilterFactories().put("annotation_filter",
				new PreBuiltTokenFilterFactoryFactory(new TokenFilterFactory() {
					@Override
					public String name() {
						return "annotation_filter";
					}

					@Override
					public TokenStream create(TokenStream tokenStream) {
						return new InlineAnnotationFilter(tokenStream);
					}
				}));
	}
}
