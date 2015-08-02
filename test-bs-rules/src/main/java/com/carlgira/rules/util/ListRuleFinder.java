package com.carlgira.rules.util;

import java.util.ArrayList;
import java.util.List;

import oracle.rules.sdk2.dictionary.AbstractDictionaryFinder;
import oracle.rules.sdk2.dictionary.DictionaryFinder;
import oracle.rules.sdk2.dictionary.RuleDictionary;
import oracle.rules.sdk2.repository.DictionaryFQN;

/**
 * A finder class to load in memory a list of dependant or linked dictionaries  
 * @author carlgira
 *
 */
public class ListRuleFinder extends AbstractDictionaryFinder 
{
	protected ListRuleFinder(DictionaryFinder paramDictionaryFinder) {
		super(paramDictionaryFinder);
	}
	
	private List<RuleDictionary> listDictionary;
	
	public ListRuleFinder(List<RuleDictionary> listDictionary)
	{
		super(null);
		this.listDictionary = listDictionary;
	}
	
	public ListRuleFinder()
	{
		super(null);
		this.listDictionary = new ArrayList<RuleDictionary>();
	}
	
	public void addDictionary(RuleDictionary dictionary)
	{
		this.listDictionary.add(dictionary);
	}
	
	@Override
	public RuleDictionary findDictionaryWithThisFinder(
			DictionaryFQN paramDictionaryFQN) 
	{
		for(RuleDictionary dictionary : this.listDictionary)
		{
			if(dictionary.getName().equals(paramDictionaryFQN.getName()) && dictionary.getPackage().equals(paramDictionaryFQN.getPackage() )) 
			{
				return dictionary;
			}
		}
		return null;
	}
}