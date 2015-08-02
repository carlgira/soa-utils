package com.carlgira.rules.bucket.forms;

import java.text.SimpleDateFormat;
import java.util.Date;

import oracle.rules.sdk2.decisiontable.BucketSet;
import oracle.rules.sdk2.dictionary.DOID;
import oracle.rules.sdk2.dictionary.RuleDictionary;
import oracle.rules.sdk2.exception.SDKException;

/**
 * Utiliy for creating BucketSet of Range of Values 
 * @author carlgira
 *
 */
public class RangeFormBSUtil
{
	public BucketSet createDoubleBucketSet(RuleDictionary localRuleDictionary, String bucketName, Double[] numbers) throws Exception
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_RANGE);
		bucketSet.setTypeID(DOID.DOUBLE);
		
		for(int i=0;i<numbers.length-1;i++)
		{
			bucketSet.add("[" + numbers[i].doubleValue() +  ".." + numbers[i+1].doubleValue() +  ")");
			
		}
		
		return bucketSet;
	}
	
	public BucketSet createLongBucketSet(RuleDictionary localRuleDictionary, String bucketName, Long[] numbers) throws Exception
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_RANGE);
		bucketSet.setTypeID(DOID.LONG);
		
		for(int i=0;i<numbers.length-1;i++)
		{
			bucketSet.add("[" + numbers[i].longValue() +  ".." + numbers[i+1].longValue() +  ")");
			
		}
		
		return bucketSet;
	}
	
	public BucketSet createIntBucketSet(RuleDictionary localRuleDictionary, String bucketName, Integer[] numbers) throws Exception
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_RANGE);
		bucketSet.setTypeID(DOID.INT);
		
		for(int i=0;i<numbers.length-1;i++)
		{
			bucketSet.add("[" + numbers[i].intValue() +  ".." + numbers[i+1].intValue() +  ")");
		}
		
		return bucketSet;
	}
	
	public BucketSet createFloatBucketSet(RuleDictionary localRuleDictionary, String bucketName, Float[] numbers) throws Exception
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_RANGE);
		bucketSet.setTypeID(DOID.FLOAT);
		
		for(int i=0;i<numbers.length-1;i++)
		{
			bucketSet.add("[" + numbers[i].floatValue() +  ".." + numbers[i+1].floatValue() +  ")");	
		}
		
		return bucketSet;
	}
	
	public BucketSet createShortBucketSet(RuleDictionary localRuleDictionary, String bucketName, Short[] numbers) throws Exception
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_RANGE);
		bucketSet.setTypeID(DOID.SHORT);
		
		for(int i=0;i<numbers.length-1;i++)
		{
			bucketSet.add("[" + numbers[i].shortValue() +  ".." + numbers[i+1].shortValue() +  ")");
			
		}
		
		return bucketSet;
	}
	
	
	public BucketSet createTimeBucketSet(RuleDictionary localRuleDictionary, String bucketName, Date[] listValues) throws SDKException
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_RANGE);
		bucketSet.setTypeID(DOID.CALENDAR);
		bucketSet.setCalendarForm("Time");
		
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ssXXX");
		
		for(int i=0;i<listValues.length-1;i++)
		{
			bucketSet.add("[" + timeFormat.format(listValues[i]) +  ".." + timeFormat.format(listValues[i+1]) +  ")");
		}
		
		return bucketSet;
	}
	
	public BucketSet createDateTimeBucketSet(RuleDictionary localRuleDictionary, String bucketName, Date[] listValues) throws SDKException
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_RANGE);
		bucketSet.setTypeID(DOID.CALENDAR);
		bucketSet.setCalendarForm("DateTime");
		
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		
		for(int i=0;i<listValues.length-1;i++)
		{
			bucketSet.add("[" + dateTimeFormat.format(listValues[i]) +  ".." + dateTimeFormat.format(listValues[i+1]) +  ")");
		}
		
		return bucketSet;
	}
	
	public BucketSet createRange(RuleDictionary localRuleDictionary, DOID bucketType , String bucketName, String[] listValues) throws SDKException
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_RANGE);
		bucketSet.setTypeID(bucketType);
		
		for(String value : listValues)
		{
			bucketSet.add(value);
		}
		
		return bucketSet;
	}
}