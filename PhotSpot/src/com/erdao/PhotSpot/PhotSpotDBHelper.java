/*
 * Copyright (C) 2009 Huan Erdao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.erdao.PhotSpot;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.provider.BaseColumns;

import com.google.android.maps.GeoPoint;

/**
 * Database manager class for PhotSpot.
 * @author Huan Erdao
 */
public class PhotSpotDBHelper extends SQLiteOpenHelper {

	/**
	 * Database storage definition for Spots
	 */
	public final class Spots implements BaseColumns {
        public Spots() {}
    	/** DEFAULT_SORT_ORDER */
        public static final String DEFAULT_SORT_ORDER	= "modified DESC";
    	/** TITLE */
        public static final String TITLE				= "title";
    	/** AUTHOR */
        public static final String AUTHOR				= "author";
    	/** THUMB_URL */
        public static final String THUMB_URL			= "thumb_url";
    	/** PHOTO_URL */
        public static final String PHOTO_URL			= "photo_url";
    	/** LATITUDE */
        public static final String LATITUDE				= "latitude";
    	/** LONGITUDE */
        public static final String LONGITUDE			= "longitude";
    	/** THUMBNAIL IMAGE DATA */
        public static final String THUMBDATA			= "thumbdata";
    	/** LABEL ID */
        public static final String LABEL				= "label";
    	/** REGION ID - RFU */
        public static final String REGION				= "region";
    	/** INDEX OF ID */
        public static final int IDX_ID			= 0;
    	/** INDEX OF TITLE */
        public static final int IDX_TITLE		= 1;
    	/** INDEX OF AUTHOR */
        public static final int IDX_AUTHOR		= 2;
    	/** INDEX OF THUMB_URL */
        public static final int IDX_THUMB_URL	= 3;
    	/** INDEX OF PHOTO_URL */
        public static final int IDX_ORIGINAL_URL	= 4;
    	/** INDEX OF LATITUDE */
        public static final int IDX_LATITUDE	= 5;
    	/** INDEX OF LONGITUDE */
        public static final int IDX_LONGITUDE	= 6;
    	/** INDEX OF THUMB_DATA */
        public static final int IDX_THUMBDATA	= 7;
    	/** INDEX OF LABEL ID */
        public static final int IDX_LABEL		= 8;
    	/** INDEX OF REGION */
        public static final int IDX_REGIOIN		= 9;
        /** id */
        public long id_;
        /** labelId */
        public long labelId_;
        /*
        public String title_;
        public String author_;
        public String thumbUrl;
        public String photoUrl;
        public double lat_;
        public double lng_;
        public byte[] thumbStream_;
        public String region_;
        */
    }
	/**
	 * Database storage definition for Labels 
	 */
    public final class Labels implements BaseColumns {
        public Labels() {}
    	/** DEFAULT_SORT_ORDER */
        public static final String DEFAULT_SORT_ORDER	= "modified DESC";
    	/** UNDEFINED_LABEL */
        public static final String UNDEFINED_LABEL		= "undefined";
    	/** LABEL */
        public static final String LABEL				= "label";
    	/** LABEL COUNT */
        public static final String COUNTS				= "counts";
    	/** INDEX OF ID */
        public static final int IDX_ID			= 0;
    	/** INDEX OF LABEL */
        public static final int IDX_LABEL		= 1;
    	/** INDEX OF COUNTS */
        public static final int IDX_COUNTS		= 2;
    	/** id */
        public long id_;
    	/** label */
        public String label_;
    	/** count */
        public long counts_;
    }
    
	/** DB Success ERRORCODE */
    public static final int DB_SUCCESS			= 0;
	/** DB General Failure ERRORCODE */
    public static final int DB_FAILED			= -1;
	/** DB Already Exist ERRORCODE */
    public static final int DB_EXISTS			= -2;
	/** DB Full ERRORCODE */
    public static final int DB_FULL				= -3;
	/** DB Unknown Error ERRORCODE */
    public static final int DB_UNKNOWN_ERROR	= -4;

    
	/** DB filename */
    private static final String DATABASE_NAME = "favorites.db";
	/** DB version */
    private static final int DATABASE_VERSION = 1;
	/** table name for spots */
    private static final String SPOTS_TABLE_NAME = "spots";
	/** table name for labels */
    private static final String LABEL_TABLE_NAME = "labels";
	
	/** IO Buffer size to read */
	private static final int IO_BUFFER_SIZE = 4 * 1024;
	
	/** Maximum Record */
	private static final int MAX_RECORDS = 256;	// MAX 256 

	/**
	 * @param context Context object
	 */
    PhotSpotDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * onCreate
	 * @param db SQLiteDatabase
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + SPOTS_TABLE_NAME + " ("
				+ Spots._ID			+ " INTEGER PRIMARY KEY,"
				+ Spots.TITLE		+ " TEXT,"
				+ Spots.AUTHOR		+ " TEXT,"
				+ Spots.THUMB_URL	+ " TEXT,"
				+ Spots.PHOTO_URL	+ " TEXT,"
				+ Spots.LATITUDE	+ " REAL,"
				+ Spots.LONGITUDE	+ " REAL,"
				+ Spots.THUMBDATA	+ " BLOB,"
				+ Spots.LABEL		+ " INTEGER,"
				+ Spots.REGION		+ " TEXT"				
				+ ");");
		db.execSQL("CREATE TABLE " + LABEL_TABLE_NAME + " ("
				+ Labels._ID		+ " INTEGER PRIMARY KEY,"
				+ Labels.LABEL		+ " TEXT,"
				+ Labels.COUNTS		+ " INTEGER"
				+ ");");
	}

	/**
	 * onUpgrade. if upgrading to newer db, then implement here
	 * @param db SQLiteDatabase
	 * @param oldVersion old version
	 * @param newVersion new version
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS"+SPOTS_TABLE_NAME);
		onCreate(db);
	}
	
	/**
	 * query a PhotoItem from THUMB_URL key
	 * @param db SQLiteDatabase
	 * @param thumb_url query key
	 * @return Spots object. if not found, return null.
	 */
	private Spots queryItemByThumbURL(SQLiteDatabase db, String thumb_url ){
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SPOTS_TABLE_NAME);
        qb.appendWhere(Spots.THUMB_URL + "='"+thumb_url+"'");
        Cursor c = qb.query(db, null, null, null, null, null, null);
        if(c.getCount()==0)
        	return null;
        c.moveToFirst();
        Spots spots = new Spots();
        spots.id_ = c.getLong(Spots.IDX_ID);
		spots.labelId_ = c.getLong(Spots.IDX_LABEL);
        /*
        spots.title_ = c.getString(Spots.IDX_TITLE);
        spots.author_ = c.getString(Spots.IDX_AUTHOR);
		spots.thumbUrl = c.getString(Spots.IDX_THUMB_URL);
		spots.photoUrl = c.getString(Spots.IDX_ORIGINAL_URL);
		spots.lat_ = c.getDouble(Spots.IDX_LATITUDE);
		spots.lng_ = c.getDouble(Spots.IDX_LONGITUDE);
        spots.region_ = c.getString(Spots.IDX_REGIOIN);
        */
        return spots;
	}

	/**
	 * query a PhotoItem from id key
	 * @param db SQLiteDatabase
	 * @param id query key
	 * @return Spots object. if not found, return null.
	 */
	private Spots queryItemById(SQLiteDatabase db, long id){
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SPOTS_TABLE_NAME);
        qb.appendWhere(Spots._ID+ "="+id);
        Cursor c = qb.query(db, null, null, null, null, null, null);
        if(c.getCount()==0)
        	return null;
        c.moveToFirst();
        Spots spots = new Spots();
        spots.id_ = c.getLong(Spots.IDX_ID);
		spots.labelId_ = c.getLong(Spots.IDX_LABEL);
        /*
        spots.title_ = c.getString(Spots.IDX_TITLE);
        spots.author_ = c.getString(Spots.IDX_AUTHOR);
		spots.thumbUrl = c.getString(Spots.IDX_THUMB_URL);
		spots.photoUrl = c.getString(Spots.IDX_ORIGINAL_URL);
		spots.lat_ = c.getDouble(Spots.IDX_LATITUDE);
		spots.lng_ = c.getDouble(Spots.IDX_LONGITUDE);
        spots.region_ = c.getString(Spots.IDX_REGIOIN);
        */
        return spots;
	}

	/**
	 * query all PhotoItem. returns cursor
	 * @param db SQLiteDatabase
	 * @return Cursor object.
	 */
	public Cursor queryAllItems(SQLiteDatabase db){
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SPOTS_TABLE_NAME);
        return qb.query(db, null, null, null, null, null, null);
	}

	/**
	 * query all Labels. returns cursor
	 * @param db SQLiteDatabase
	 * @return Cursor object.
	 */
	public Cursor queryAllLabels(SQLiteDatabase db){
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(LABEL_TABLE_NAME);
        return qb.query(db, null, null, null, null, null, null);
	}

	/**
	 * query a Label from PhotoItem
	 * @param item PhotoItem object
	 * @return label string. if undefined, return null.
	 */
	public String queryLabel(PhotoItem item){
		final SQLiteDatabase db = this.getWritableDatabase();
		Spots spots = queryItemById(db, item.getId());
		if(spots==null){
			db.close();
			return null;
		}
		Labels labels = this.queryLabel(db, spots.labelId_);
		if(labels==null){
			db.close();
			return null;
		}
		if( labels.label_.equals(Labels.UNDEFINED_LABEL) ){
			db.close();
			return null;
		}
		db.close();
		return labels.label_;
	}
	
	/**
	 * insert a PhotoItem.
	 * @param item PhotoItem object
	 * @param bmp Bitmap object
	 * @return  1:success, -1:exception 0:record exists
	 */
	public int insertItem(PhotoItem item, Bitmap bmp){
		final SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = this.queryAllItems(db);
		if(c.getCount()>=MAX_RECORDS){
			db.close();
			return DB_FULL;
		}
		Spots spots = this.queryItemByThumbURL(db,item.getFullThumbUrl());
		if( spots != null ){
			db.close();
			return DB_EXISTS;
		}
		GeoPoint location = item.getLocation();
		double lat = location.getLatitudeE6()/1E6;
		double lng = location.getLongitudeE6()/1E6;
		BufferedOutputStream bufferedOS = null;
        byte[] data = null;
		if(bmp!=null){
	        try {
				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				bufferedOS = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
		        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bufferedOS);
		        bufferedOS.flush();
		        data = dataStream.toByteArray();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				db.close();
				return DB_UNKNOWN_ERROR;
			}
		}
		ContentValues cv = new ContentValues();
		cv.put(Spots.TITLE,item.getTitle());
		cv.put(Spots.AUTHOR,item.getAuthor());
		cv.put(Spots.THUMB_URL,item.getFullThumbUrl());
		cv.put(Spots.PHOTO_URL,item.getOriginalUrl());
		cv.put(Spots.LATITUDE,lat);
		cv.put(Spots.LONGITUDE,lng);
		cv.put(Spots.THUMBDATA,data);
		long id = insertLabel(db,Labels.UNDEFINED_LABEL);
		cv.put(Spots.LABEL,id);
		cv.put(Spots.REGION,"");
		long newId = 0;
		try {
			newId = db.insertOrThrow(PhotSpotDBHelper.SPOTS_TABLE_NAME, null, cv);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			db.close();
			return DB_UNKNOWN_ERROR;
		}
    	if(bufferedOS!=null){
	        try {
	        	bufferedOS.close();
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
	        }
    	}
    	item.setId(newId);
		db.close();
		return DB_SUCCESS;
	}
	
	/**
	 * query a Label from Label Name.
	 * @param db SQLiteDatabase object
	 * @param label label string
	 * @return  Labels object. if not found, return null.
	 */
	private Labels queryLabel(SQLiteDatabase db, String label){
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(LABEL_TABLE_NAME);
        qb.appendWhere(Labels.LABEL + "='"+label+"'");
        Cursor c = qb.query(db, null, null, null, null, null, null);
        if(c.getCount()==0)
        	return null;
        c.moveToFirst();
        Labels labels = new Labels();
        labels.id_ = c.getLong(Labels.IDX_ID);
        labels.label_ = c.getString(Labels.IDX_LABEL);
        labels.counts_ = c.getLong(Labels.IDX_COUNTS);
        return labels;
	}

	/**
	 * query a Label from labelId
	 * @param db SQLiteDatabase object
	 * @param labelId label id
	 * @return  Labels object. if not found, return null.
	 */
	private Labels queryLabel(SQLiteDatabase db, long labelId){
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(LABEL_TABLE_NAME);
        qb.appendWhere(Labels._ID+ "="+labelId);
        Cursor c = qb.query(db, null, null, null, null, null, null);
        if(c.getCount()==0)
        	return null;
        c.moveToFirst();
        Labels labels = new Labels();
        labels.id_ = c.getLong(Labels.IDX_ID);
        labels.label_ = c.getString(Labels.IDX_LABEL);
        labels.counts_ = c.getLong(Labels.IDX_COUNTS);
        return labels;
	}

	/**
	 * update label count in label table
	 * @param db SQLiteDatabase object
	 * @param labelId label id
	 * @param count label count
	 */
	private void updateLabelCount(SQLiteDatabase db, long labelId, long count){
		String whereClause = Labels._ID + "="+labelId;
		if(count == 0){
    		db.delete(LABEL_TABLE_NAME, whereClause, null);
		}
		else{
			ContentValues cv = new ContentValues();
			cv.put(Labels.COUNTS,count);
			db.update(LABEL_TABLE_NAME, cv, whereClause, null);
		}
	}
	
	/**
	 * insert new label to label table
	 * @param db SQLiteDatabase object
	 * @param newLabelName new label name string
	 * @return label id for new label. if already exists, update label count andreturn existing id.
	 */
	public long insertLabel(SQLiteDatabase db, String newLabelName){
		Labels labels = this.queryLabel(db,newLabelName);
		long id = 0;
		if(labels != null){						// if exists
			id = labels.id_;
			updateLabelCount(db,id,labels.counts_+1);
		}
		else{
			ContentValues cv = new ContentValues();
			cv.put(Labels.LABEL,newLabelName);
			cv.put(Labels.COUNTS,1);
			try {
				id = db.insertOrThrow(LABEL_TABLE_NAME, null, cv);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return DB_UNKNOWN_ERROR;
			}
		}
		return id;
	}

	/**
	 * update label to a new label
	 * @param item PhotoItem object
	 * @param newLabelName new label name string
	 * @return label id for new label. if same, current labelid will return. if failed, DB_FAILED will return.
	 */
	public long updateLabel(PhotoItem item, String newLabelName){
		final SQLiteDatabase db = this.getWritableDatabase();
		Spots spots = this.queryItemById(db,item.getId());
		if( spots == null ){
			db.close();
			return DB_FAILED;
		}
		long labelId = spots.labelId_;
		// query for current label
		Labels curLabels = this.queryLabel(db,labelId);
		if(curLabels==null){
			db.close();
			return DB_FAILED;
		}
		long newLabelId = 0;
		if(newLabelName.equals(curLabels.label_)){
			db.close();
			return labelId;
		}
		// update old label
		updateLabelCount(db,curLabels.id_,curLabels.counts_-1);
		// insert new label
		newLabelId = insertLabel(db,newLabelName);
		// update spot table with new label id
		ContentValues cv = new ContentValues();
		cv.put(Spots.LABEL,newLabelId);
		String whereClause = Spots._ID + "="+item.getId();
		db.update(SPOTS_TABLE_NAME, cv, whereClause, null);
		db.close();
		return newLabelId;
	}

	
	/**
	 * delete a PhotoItem from table
	 * @param item PhotoItem object
	 * @return DB ERROR CODE.
	 */
	public int deleteItem(PhotoItem item){
		final SQLiteDatabase db = this.getWritableDatabase();
		Spots spots = this.queryItemById(db, item.getId());
		if(spots == null){
			db.close();
			return DB_FAILED;
		}
		Labels curLabels = this.queryLabel(db,spots.labelId_);
		if(curLabels!=null)
			updateLabelCount(db,curLabels.id_,curLabels.counts_-1);
		try {
			String whereClause = Spots._ID + "="+spots.id_;
			db.delete(SPOTS_TABLE_NAME, whereClause, null);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			db.close();
			return DB_UNKNOWN_ERROR;
		}
		db.close();
		return DB_SUCCESS;
	}
	
	/**
	 * delete all PhotoItem from table
	 * @return DB ERROR CODE.
	 */
	public int deleteAll(){
		final SQLiteDatabase db = this.getWritableDatabase();
		try {
			db.delete(SPOTS_TABLE_NAME, null, null);
			db.delete(LABEL_TABLE_NAME, null, null);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			db.close();
			return DB_UNKNOWN_ERROR;
		}
		db.close();
		return DB_SUCCESS;
	}
   
}

