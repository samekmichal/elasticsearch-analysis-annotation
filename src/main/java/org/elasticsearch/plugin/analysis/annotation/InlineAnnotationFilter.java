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

package org.elasticsearch.plugin.analysis.annotation;

import java.io.IOException;
import java.util.Stack;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

/**
 * Characters surrounded by '[' SYNONYM_START_DELIMITER and ']' are considered
 * to be a synonym to the word located before the opening '['.
 * eg
 * 		"W. A. Mozart[artist]"
 * 		"Salzburg[city;Austria]" 
 * 
 * The surrounding brackets can be changed by assigning desired characters to
 * SYNONYM_START_DELIMITER or SYNONYM_END_DELIMITER respectively.
 * 
 * If more synonyms are present, they are delimited by SYNONYMS_DELIMITER, which
 * is by default ';'.
 * 
 * If it is desired to surround the resulting synonym with prefix/suffix, set
 * SYNONYMS_PREFIX or SYNONYMS_SUFIX.
 * 
 * @author Michal Samek, samek.michal @ gmail.com
 *
 */

public class InlineAnnotationFilter extends TokenFilter {
	public static final String SYNONYM_TOKEN_TYPE = "SYNONYM";
	
	public static char SYNONYM_START_DELIMITER = '[';
	public static char SYNONYM_END_DELIMITER = ']';
	public static char SYNONYMS_DELIMITER = ';';
	public static String SYNONYM_PREFIX = "[";
	public static String SYNONYM_SUFFIX = "]";
	
	private Stack<String> synonymStack;
	private AttributeSource.State current;
	private final CharTermAttribute termAtt;
	private final PositionIncrementAttribute posIncrAtt;
	private final TypeAttribute typeAtt;

	
	public InlineAnnotationFilter(TokenStream input) {
		super(input);
		synonymStack = new Stack<String>();
		this.termAtt = addAttribute(CharTermAttribute.class);
		this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
		this.typeAtt = addAttribute(TypeAttribute.class);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (synonymStack.size() > 0) {
			String syn = SYNONYM_PREFIX + synonymStack.pop() + SYNONYM_SUFFIX;
			restoreState(current);
			termAtt.copyBuffer(syn.toCharArray(), 0, syn.length());
			typeAtt.setType(SYNONYM_TOKEN_TYPE);
			posIncrAtt.setPositionIncrement(0);
			return true;
		}

		if (!input.incrementToken()) {
			return false;
		}
		
		if (addAliasesToStack()) {
			current = captureState();
		}
		return true;
	}

	
	/**
	 * Checks whether current token has synonyms appended. If it has, then they
	 * are pushed on the synonymStack.
	 * 
	 * @return true if synonyms were found, otherwise false
	 */
	private boolean addAliasesToStack() {
		String buffer = termAtt.toString();
		String synonyms = null;
		int length = buffer.length();
		
		searchingLoop:
		for (int i = 0; i < length; i++) {
			if (buffer.charAt(i) == SYNONYM_START_DELIMITER) {
				
				// It might not be necessary to search for closing delimiter
				for (int j = i + 1; j < length; j++) {
					if (buffer.charAt(j) == SYNONYM_END_DELIMITER) {
						synonyms = buffer.substring(i+1, j);
						termAtt.setLength(i);
						break searchingLoop;
					}
				}
			}
		}
		
		// No synonyms have been found
		if (synonyms == null) {
			return false;
		}
		
		
		int beginIndex = 0;
		int endIndex = -1;
		while ((endIndex = synonyms.indexOf(SYNONYMS_DELIMITER, beginIndex)) != -1) {
			synonymStack.push(synonyms.substring(beginIndex, endIndex).trim());
			beginIndex = endIndex+1;
		}
		
		// Single synonym, which is not ended by SYNONYMS_DELIMITER, eq [artist]
		// Last synonym, which is not ended by SYNONYMS_DELIMITER, eq [city;Austria]
		// For [city;Austria;] the beginIndex will be set to index, that is equal to the string length
		if (beginIndex < synonyms.length() && synonyms.length() > 0) {
			synonymStack.push(synonyms.substring(beginIndex).trim());
		}
		return true;
	}
}
