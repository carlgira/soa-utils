package com.carlgira.rules.bucket.forms;

import java.text.SimpleDateFormat;
import java.util.Date;

import oracle.rules.sdk2.decisiontable.BucketSet;
import oracle.rules.sdk2.dictionary.DOID;
import oracle.rules.sdk2.dictionary.RuleDictionary;
import oracle.rules.sdk2.exception.SDKException;
/**
 * Utiliy for creating BucketSet of List of Values 
 * @author carlgira
 *
 */
public class LovFormBSUtil
{
	public BucketSet createBooleanBucketSet(RuleDictionary localRuleDictionary, String bucketName) throws SDKException
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_LOV);
		bucketSet.setTypeID(DOID.BOOLEAN);
		return bucketSet;
	}
	
	public BucketSet createStringBucketSet(RuleDictionary localRuleDictionary, String bucketName, String[] listValues) throws SDKException
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_LOV);
		bucketSet.setTypeID(DOID.STRING);
		
		for(String value : listValues)
		{
			bucketSet.add(value);
		}
		
		return bucketSet;
	}
	
	public BucketSet createDoubleBucketSet(RuleDictionary localRuleDictionary, String bucketName, Double[] numbers) throws Exception
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_LOV);
		bucketSet.setTypeID(DOID.DOUBLE);
		
		for(Double number : numbers)
		{
			bucketSet.add(""+number.doubleValue());
			
		}
		
		return bucketSet;
	}
	
	public BucketSet createLongBucketSet(RuleDictionary localRuleDictionary, String bucketName, Long[] numbers) throws Exception
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_LOV);
		bucketSet.setTypeID(DOID.LONG);
		
		for(Long number : numbers)
		{
			bucketSet.add(""+number.longValue());
			
		}
		
		return bucketSet;
	}
	
	public BucketSet createIntBucketSet(RuleDictionary localRuleDictionary, String bucketName, Integer[] numbers) throws Exception
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_LOV);
		bucketSet.setTypeID(DOID.INT);
		
		for(Integer number : numbers)
		{
			bucketSet.add(""+number.intValue());
			
		}
		
		return bucketSet;
	}
	
	public BucketSet createFloatBucketSet(RuleDictionary localRuleDictionary, String bucketName, Float[] numbers) throws Exception
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_LOV);
		bucketSet.setTypeID(DOID.FLOAT);
		
		for(Float number : numbers)
		{
			bucketSet.add(""+number.floatValue());
			
		}
		
		return bucketSet;
	}
	
	public BucketSet createShortBucketSet(RuleDictionary localRuleDictionary, String bucketName, Short[] numbers) throws Exception
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_LOV);
		bucketSet.setTypeID(DOID.SHORT);
		
		for(Short number : numbers)
		{
			bucketSet.add(""+number.shortValue());
			
		}
		
		return bucketSet;
	}
	
	public BucketSet createCharBucketSet(RuleDictionary localRuleDictionary, String bucketName, Character[] characters) throws SDKException
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_LOV);
		bucketSet.setTypeID(DOID.CHAR);
		
		for(Character value : characters)
		{
			bucketSet.add("'" +value.charValue() + "'");
		}
		
		return bucketSet;
	}
	
	
	public BucketSet createTimeBucketSet(RuleDictionary localRuleDictionary, String bucketName, Date[] listValues) throws SDKException
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_LOV);
		bucketSet.setTypeID(DOID.CALENDAR);
		bucketSet.setCalendarForm("Time");
		
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ssXXX");
		
		for(Date value : listValues)
		{
			bucketSet.add(timeFormat.format(value));
		}
		
		return bucketSet;
	}
	
	public BucketSet createDateTimeBucketSet(RuleDictionary localRuleDictionary, String bucketName, Date[] listValues) throws SDKException
	{
		BucketSet bucketSet = (BucketSet) localRuleDictionary.getDataModel().getBucketSetTable().add();
		bucketSet.setName(bucketName);
		bucketSet.setForm(BucketSet.FORM_LOV);
		bucketSet.setTypeID(DOID.CALENDAR);
		bucketSet.setCalendarForm("DateTime");
		
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		
		for(Date value : listValues)
		{
			bucketSet.add(dateTimeFormat.format(value));
		}
		
		return bucketSet;
	}
}