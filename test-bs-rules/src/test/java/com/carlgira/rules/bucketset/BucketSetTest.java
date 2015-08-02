package com.carlgira.rules.bucketset;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.carlgira.rules.bucket.forms.LovFormBSUtil;
import com.carlgira.rules.bucket.forms.RangeFormBSUtil;
import com.carlgira.rules.util.DictionaryUtil;
import com.carlgira.rules.util.ListRuleFinder;

import oracle.rules.sdk2.dictionary.DOID;
import oracle.rules.sdk2.dictionary.RuleDictionary;

public class BucketSetTest 
{	
	@Test
	public void testBucketsForms() throws Exception
	{
		ListRuleFinder memoryRuleFinder = new ListRuleFinder();
		
		RuleDictionary commonDict = DictionaryUtil.loadDictionary(getClass().getClassLoader().getResource("CommonRules.rules").getFile(), memoryRuleFinder);
		memoryRuleFinder.addDictionary(commonDict);
		RuleDictionary testDict = DictionaryUtil.loadDictionary(getClass().getClassLoader().getResource("TestRule.rules").getFile(), memoryRuleFinder);
		
		testListOfValues(testDict);
		testRanges(testDict);
		
		DictionaryUtil.checkDictionary(testDict);
		
		testDict.writeDictionary(new FileWriter(new File("target/" + testDict.getName() + ".rules")));
	}
	
	/**
	 * Create a bucket of list of values using all the possible types. (Boolean, String, Double, Int, Float, Short, Long, Time , DateTime)
	 * @param testDict
	 * @throws Exception
	 */
	public void testListOfValues(RuleDictionary testDict) throws Exception
	{
		LovFormBSUtil bucketSetUtil = new LovFormBSUtil();
		
		Calendar dateOne = Calendar.getInstance();
		Calendar dateTwo = Calendar.getInstance();
		Calendar dateTree = Calendar.getInstance();
		dateTwo.add(Calendar.HOUR, 1);
		dateTree.add(Calendar.HOUR, 2);
		
		bucketSetUtil.createBooleanBucketSet(testDict, "BooleanBucketSet");
		bucketSetUtil.createStringBucketSet(testDict, "StringBucketSet", new String[]{"one", "two"});
		
		bucketSetUtil.createDoubleBucketSet(testDict, "DoubleBucketSet", new Double[]{1.0d , 2.0d});
		bucketSetUtil.createIntBucketSet(testDict, "IntBucketSet",  new Integer[]{1 ,2 });
		bucketSetUtil.createLongBucketSet(testDict, "LongBucketSet",  new Long[]{1l ,2l });
		bucketSetUtil.createFloatBucketSet(testDict, "FloatBucketSet", new Float[]{1.0f ,2.0f });
		bucketSetUtil.createShortBucketSet(testDict, "ShortBucketSet",  new Short[]{1 ,2 });
		
		bucketSetUtil.createCharBucketSet(testDict, "CharBucketSet",  new Character[]{'a' ,'b' });
		
		bucketSetUtil.createTimeBucketSet(testDict, "TimeBucketSet",   new Date[]{ dateOne.getTime(), dateTwo.getTime(), dateTree.getTime() }  )  ;
		bucketSetUtil.createDateTimeBucketSet(testDict, "DateTimeBucketSet", new Date[]{ dateOne.getTime(), dateTwo.getTime(), dateTree.getTime()  }  )  ;	
	}
	
	/**
	 * Create a bucket of range of values using all the possible types. ( Double, Int, Float, Short, Long, Time , DateTime)
	 * @param testDict
	 * @throws Exception
	 */
	public void testRanges(RuleDictionary testDict) throws Exception
	{
		RangeFormBSUtil rangeFormBSUtil = new RangeFormBSUtil();
		
		Calendar dateOne = Calendar.getInstance();
		Calendar dateTwo = Calendar.getInstance();
		Calendar dateTree = Calendar.getInstance();
		dateTwo.add(Calendar.HOUR, 1);
		dateTree.add(Calendar.HOUR, 2);
		
		rangeFormBSUtil.createRange(testDict, DOID.INT, "IntBucketSetRange1", new String[]{"[0..50)" , "[50..100)"});
		rangeFormBSUtil.createIntBucketSet(testDict, "IntBucketSetRange2", new Integer[]{1,2,3});
		
		rangeFormBSUtil.createRange(testDict, DOID.LONG, "LongBucketSetRange1", new String[]{"[0..50)" , "[50..100)"});
		rangeFormBSUtil.createLongBucketSet(testDict, "LongBucketSetRange2", new Long[]{1l,2l,3l});
		
		rangeFormBSUtil.createRange(testDict, DOID.SHORT, "ShortBucketSetRange1", new String[]{"[0..50)" , "[50..100)"});
		rangeFormBSUtil.createShortBucketSet(testDict, "ShortBucketSetRange2", new Short[]{1,2,3});
		
		rangeFormBSUtil.createRange(testDict, DOID.FLOAT, "FloatBucketSetRange1", new String[]{"[0.0..50.0)" , "[50.0..100.0)"});
		rangeFormBSUtil.createFloatBucketSet(testDict, "FloatBucketSetRange2", new Float[]{1.0f,2.0f,3.0f});
				
		rangeFormBSUtil.createRange(testDict, DOID.DOUBLE, "DoubleBucketSetRange1", new String[]{"[0.0..50.0)" , "[50..100.0)"});
		rangeFormBSUtil.createDoubleBucketSet(testDict, "DoubleBucketSetRange2", new Double[]{1.0d,2.0d,3.0d});
		
		rangeFormBSUtil.createTimeBucketSet(testDict, "DateBucketSetRange", new Date[]{ dateOne.getTime(), dateTwo.getTime(), dateTree.getTime()  });
		
		rangeFormBSUtil.createDateTimeBucketSet(testDict, "DateTimeBucketSetRange", new Date[]{ dateOne.getTime(), dateTwo.getTime(), dateTree.getTime()  });
	}
}