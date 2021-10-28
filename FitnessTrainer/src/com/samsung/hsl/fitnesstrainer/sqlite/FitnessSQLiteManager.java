package com.samsung.hsl.fitnesstrainer.sqlite;

import java.io.File;
import java.util.ArrayList;

import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class FitnessSQLiteManager extends SQLiteOpenHelper {
	private static String tag = FitnessSQLiteManager.class.getName();

	/** @brief 데이터베이스 파일 위치 및 이름 */
	public static String DATABASE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separatorChar+"Fitness";
	public static String DATABASE_NAME = "fitness.db";
	public static String DATABASE_PATH = DATABASE_DIR+File.separatorChar+DATABASE_NAME;
	
	/** @brief 유저 테이블 생성문 */
	private static String SQL_CREATE_TABLE_BLE = "CREATE TABLE ble"
			+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "address VARCHAR(20) UNIQUE," 
			+ "sw INTEGER,"
			+ "hw INTEGER" 
			+ ");";


	/** @brief 유저 테이블 생성문 */
	private static String SQL_CREATE_TABLE_USER = "CREATE TABLE user"
			+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "email VARCHAR(20) UNIQUE,"
			+ "password VARCHAR(20),"
			+ "name VARCHAR(20),"
			+ "gender VARCHAR(6),"
			+ "birthday DATE,"
			+ "height FLOAT,"
			+ "weight FLOAT,"
			+ "stableHeartrate INTEGER,"
			+ "checktime DATETIME,"
			+ "picture BLOB,"
			+ "datetime DATETIME DEFAULT (DATETIME('now','localtime'))" 
			+ ");";
	
	/** @brief 피트니스 리스트 테이블 생성문 */
	private static String SQL_CREATE_TABLE_FITNESS_LIST = "CREATE TABLE fitnessList"
			+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "userId INTEGER,"
			+ "datetime DATETIME DEFAULT (DATETIME('now','localtime')),"
			+ "FOREIGN KEY(userId) REFERENCES user(id));";

	/** @brief 피트니스 테이블 생성문 */
	private static String SQL_CREATE_TABLE_FITNESS_DATA = "CREATE TABLE fitnessData"
			+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "listId INTEGER,"
			+ "heartrate INTEGER,"
			+ "filterHeartrate INTEGER,"
			+ "skinTemperature FLOAT,"
			+ "humidity FLOAT,"
			+ "consumeCalorie FLOAT,"
			+ "power INTEGER,"
			+ "fall INTEGER,"
			+ "datetime DATETIME DEFAULT (DATETIME('now','localtime'))," 
			+ "FOREIGN KEY(listId) REFERENCES fitnessList(id));";

	public FitnessSQLiteManager(Context context) {
		super(context, DATABASE_PATH, null, 2);
	}

	private FitnessSQLiteManager(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public static void checkExternalDirectory(){
		File directory = new File(FitnessSQLiteManager.DATABASE_DIR);
		if(!directory.exists()){
			if(!directory.mkdirs()){
				Log.i(tag, FitnessSQLiteManager.DATABASE_DIR+" create fail");
				DATABASE_PATH = DATABASE_NAME;
			}
		}
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(SQL_CREATE_TABLE_USER);
		db.execSQL(SQL_CREATE_TABLE_FITNESS_LIST);
		db.execSQL(SQL_CREATE_TABLE_FITNESS_DATA);
		db.execSQL(SQL_CREATE_TABLE_BLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("drop table user");
		db.execSQL("drop table fitnessList");
		db.execSQL("drop table fitnessData");
		db.execSQL("drop table ble");
		this.onCreate(db);
	}

	public User insert(User user) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("email", user.email);
		values.put("password", user.password);
		values.put("name", user.name);
		values.put("gender", user.gender);
		values.put("birthday", user.birthday);
		values.put("height", user.height);
		values.put("weight", user.weight);
		values.put("checktime", user.checktime);
		values.put("stableHeartrate", user.stableHeartrate);
		values.put("picture", user.picture);
		user.id = db.insert("user", null, values);
		FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_CHANGE_USER_INFO,user);
		return user;
	}
	
	public FitnessBLE insert(FitnessBLE ble) {
		if(select(ble).id!=0){ // 만약 존재한다면.
			return null;			
		}
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("address", ble.address);
		values.put("sw", ble.sw);
		values.put("hw", ble.hw);
		ble.id = db.insert("ble", null, values);
		return ble;
	}

	public FitnessList insert(FitnessList fitnessList) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("userId", fitnessList.userId);
		fitnessList.id = db.insert("fitnessList", null, values);
		return fitnessList;
	}

	public FitnessData insert(FitnessData fitness) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("listId", fitness.listId);
		values.put("heartrate", fitness.heartrate);
		values.put("filterHeartrate", fitness.filterHeartrate);
		values.put("skinTemperature", fitness.skinTemperature);
		values.put("humidity", fitness.humidity);
		values.put("consumeCalorie", fitness.consumeCalorie);
		values.put("power", fitness.power);
		values.put("fall", fitness.fall);
		fitness.id = db.insert("fitnessData", null, values);
		return fitness;
	}

	private static String SQL_SELECT_USER_BY_PASSWORD = 
			"select id,email,password,name,gender,birthday,height,weight,stableHeartrate,checktime,picture,datetime "
			+ " from user "
			+ " where password=?";
	

	/**
	 * @brief pw가 일치하는 user를 조회한다.
	 * @param user
	 * @return
	 */
	public User select(User user) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(SQL_SELECT_USER_BY_PASSWORD,
				new String[] { user.password });
		// 일치하는 유저가 없으면 id값에 0 설정
		if (cursor.getCount() == 0)
			user.id = 0;
		while (cursor.moveToNext()) {
			user.id = cursor.getLong(0);
			user.email = cursor.getString(1);
			user.password = cursor.getString(2);
			user.name = cursor.getString(3);
			user.gender = cursor.getString(4);
			user.birthday = cursor.getString(5);
			user.height = cursor.getFloat(6);
			user.weight = cursor.getFloat(7);
			user.stableHeartrate = cursor.getInt(8);
			user.checktime = cursor.getString(9);
			user.picture = cursor.getBlob(10);
			user.datetime = cursor.getString(11);
		}
		cursor.close();
		return user;
	}
	
	private static String SQL_SELECT_BLE_BY_ADDRESS = 
			"select id,address,sw,hw "
			+ " from ble "
			+ " where address=?";

	/**
	 * @brief 주소가 일치하는 ble를 조회한다.
	 * @param ble
	 * @return
	 */
	public FitnessBLE select(FitnessBLE ble) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(SQL_SELECT_BLE_BY_ADDRESS,
				new String[] { ble.address });
		// 일치하는 유저가 없으면 id값에 0 설정
		if (cursor.getCount() == 0)
			ble.id = 0;
		while (cursor.moveToNext()) {
			ble.id = cursor.getLong(0);
			ble.address = cursor.getString(1);
			ble.sw = cursor.getInt(2);
			ble.hw = cursor.getInt(3);
		}
		cursor.close();
		return ble;
	}

	private static String SQL_SELECT_USER = 
			"select id,email,password,name,gender,birthday,height,weight,stableHeartrate,checktime,picture,datetime "
			+ " from user order by name asc";

	/** @brief 하나의 사용자를 검색한다. */
	public User select() {
		User user = new User();
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(SQL_SELECT_USER, null);
		// 일치하는 유저가 없으면 id값에 0 설정
		if (cursor.getCount() == 0)
			user.id = 0;
		while (cursor.moveToNext()) {
			user.id = cursor.getLong(0);
			user.email = cursor.getString(1);
			user.password = cursor.getString(2);
			user.name = cursor.getString(3);
			user.gender = cursor.getString(4);
			user.birthday = cursor.getString(5);
			user.height = cursor.getFloat(6);
			user.weight = cursor.getFloat(7);
			user.stableHeartrate = cursor.getInt(8);
			user.checktime = cursor.getString(9);
			user.picture = cursor.getBlob(10);
			user.datetime = cursor.getString(11);
		}
		cursor.close();
		return user;
	}
	
	private static String SQL_SELECT_USER_BY_EMAIL = 
			"select id,email,password,name,gender,birthday,height,weight,stableHeartrate,checktime,picture,datetime "
			+ " from user where email=?";

	/** @brief 메일로 사용자를 검색한다. */
	public User selectUserByEmail(User user) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(SQL_SELECT_USER_BY_EMAIL, new String[]{user.email});
		// 일치하는 유저가 없으면 id값에 0 설정
		if (cursor.getCount() == 0)
			user.id = 0;
		while (cursor.moveToNext()) {
			user.id = cursor.getLong(0);
			user.email = cursor.getString(1);
			user.password = cursor.getString(2);
			user.name = cursor.getString(3);
			user.gender = cursor.getString(4);
			user.birthday = cursor.getString(5);
			user.height = cursor.getFloat(6);
			user.weight = cursor.getFloat(7);
			user.stableHeartrate = cursor.getInt(8);
			user.checktime = cursor.getString(9);
			user.picture = cursor.getBlob(10);
			user.datetime = cursor.getString(11);
		}
		cursor.close();
		return user;
	}

	/** @brief 모든 사용자를 검색한다. */
	public ArrayList<User> selectAllUsers() {
		ArrayList<User> userList = new ArrayList<User>();
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(SQL_SELECT_USER, null);
		while (cursor.moveToNext()) {
			User user = new User();
			user.id = cursor.getLong(0);
			user.email = cursor.getString(1);
			user.password = cursor.getString(2);
			user.name = cursor.getString(3);
			user.gender = cursor.getString(4);
			user.birthday = cursor.getString(5);
			user.height = cursor.getFloat(6);
			user.weight = cursor.getFloat(7);
			user.stableHeartrate = cursor.getInt(8);
			user.checktime = cursor.getString(9);
			user.picture = cursor.getBlob(10);
			user.datetime = cursor.getString(11);
			userList.add(user);
		}
		cursor.close();
		return userList;
	}

	private static String SQL_SELECT_FITNESS_LIST = 
			"select id,userId,sum(consumeCalorie) as consumeCalorie,sum(exerciseTime) as exerciseTime,strftime('%Y-%m-%d',datetime) as datetime"  
			+" from "
			+ " ( select fitnessList.id,fitnessList.userId,MAX(fitnessData.consumeCalorie) as consumeCalorie,MAX(strftime('%s',fitnessData.datetime) - strftime('%s',fitnessList.datetime)) as exerciseTime,fitnessList.datetime "
			+ " from fitnessList inner join fitnessData on fitnessList.id=fitnessData.listId group by fitnessList.id,fitnessList.userId,fitnessList.datetime) ";
	
	public static String SQL_CONDITION_FROM = "from";
	public static String SQL_CONDITION_TO = "to";
	
	public ArrayList<FitnessList> selectFitnessList(ContentValues condition) {
		String where = " where exerciseTime<> 0 ";
		int initSize = where.length();
		int argsCount = 0;
		
		if (condition.get("id") != null) {
			where += " and id=? ";
			argsCount++;
		}
		if (condition.get("userId") != null) {
			where += " and userId=? ";
			argsCount++;
		}
		if (condition.get(SQL_CONDITION_FROM) != null) {
			where += " and ? <= datetime ";
			argsCount++;
		}
		if (condition.get(SQL_CONDITION_TO) != null) {
			where += " and datetime <= ? ";
			argsCount++;
		}

		String[] selectArgs = new String[argsCount];
		argsCount = 0;
		if (condition.get("id") != null) {
			selectArgs[argsCount] = condition
					.getAsString("id");
			argsCount++;
		}
		if (condition.get("userId") != null) {
			selectArgs[argsCount] = condition
					.getAsString("userId");
			argsCount++;
		}
		if (condition.get(SQL_CONDITION_FROM) != null) {
			selectArgs[argsCount] = condition.getAsString(SQL_CONDITION_FROM);
			argsCount++;
		}
		if (condition.get(SQL_CONDITION_TO) != null) {
			selectArgs[argsCount] = condition.getAsString(SQL_CONDITION_TO);
			argsCount++;
		}

		String sql = SQL_SELECT_FITNESS_LIST + where + " group by strftime('%YYYY-%mm-%dd',datetime)  order by datetime asc ";

		ArrayList<FitnessList> list = new ArrayList<FitnessList>();
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(sql, selectArgs);

		while (cursor.moveToNext()) {
			FitnessList item = new FitnessList();
			item.id = cursor.getLong(0);
			item.userId = cursor.getLong(1);
			item.consumeCalorie = cursor.getFloat(2);
			item.exerciseTime = cursor.getInt(3);
			item.datetime = cursor.getString(4);
			list.add(item);
		}
		cursor.close();
		return list;
	}

	private static String SQL_SELECT_FITNESS_DATA = 
			"select fitnessData.id,listId,heartrate,filterHeartrate,skinTemperature,humidity,consumeCalorie,power,fall,fitnessData.datetime "
			+ " from fitnessData "
			+ " inner join fitnessList "
			+ " on fitnessList.userId=? and fitnessData.listId=fitnessList.id ";

	public ArrayList<FitnessData> selectFitnessData(User user,String from,String to) {
		String where = "";
		int argsCount = 1;
		if (from != null) {
			where += " and ? <= fitnessData.datetime ";
			argsCount++;
		}
		if (to != null) {
			where += " and fitnessData.datetime <= ? ";
			argsCount++;
		}

		String[] selectArgs = new String[argsCount];
		argsCount = 0;
		selectArgs[argsCount] = String.valueOf(user.id);
		argsCount++;
		if (from != null) {
			selectArgs[argsCount] = from;
			argsCount++;
		}
		if (to != null) {
			selectArgs[argsCount] = to;
			argsCount++;
		}

		String sql = SQL_SELECT_FITNESS_DATA + where + " order by fitnessData.datetime asc ";

		ArrayList<FitnessData> list = new ArrayList<FitnessData>();
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(sql, selectArgs);
		while (cursor.moveToNext()) {
			FitnessData item = new FitnessData();
			item.id = cursor.getLong(0);
			item.listId = cursor.getLong(1);
			item.heartrate = cursor.getInt(2);
			item.filterHeartrate = cursor.getInt(3);
			item.skinTemperature = cursor.getFloat(4);
			item.humidity = cursor.getFloat(5);
			item.consumeCalorie	= cursor.getFloat(6);
			item.power = cursor.getInt(7);
			item.fall = cursor.getInt(8);
			item.datetime	= cursor.getString(9);
			list.add(item);
		}
		cursor.close();
		return list;
	}

	public void update(User user) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("email", user.email);
		values.put("name", user.name);
		values.put("password", user.password);
		values.put("gender", user.gender);
		values.put("birthday", user.birthday);
		values.put("height", user.height);
		values.put("weight", user.weight);
		values.put("stableHeartrate", user.stableHeartrate);
		values.put("checktime", user.checktime);
		values.put("picture", user.picture);
		db.update("user", values, "email=?", new String[]{user.email});
		FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_CHANGE_USER_INFO,user);
	}

	public void update(FitnessList fitnessList) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("userId", fitnessList.userId);
		values.put("consumeCalorie",
				fitnessList.consumeCalorie);
		values.put("exerciseTime", fitnessList.exerciseTime);
		values.put("datetime", fitnessList.datetime);
		db.update("fitnessList", values, "id="+ fitnessList.id, null);
	}

	/** @brief 모든 사용자를 검색한다. */
	public User insertOrUpdateUser(User user) {
		User tempUser = new User();
		tempUser.email = user.email;
		selectUserByEmail(tempUser);
		//사용자가 없으면 새로 등록
		if(tempUser.id==0)
		{
			insert(user);
		}
		//기존 사용자가 있으면 갱신
		else 
		{
			user.id = tempUser.id;
			update(user);
		}
		return user;
	}
	
	@Deprecated
	public void delete(User user) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete("user", "id=" + user.id, null);
	}

	@Deprecated
	public void delete(FitnessData fitness) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete("fitnessData", "id="+ fitness.id, null);
	}

	/**
	 * 해당 일자의 운동 목록을 구한다.
	 * 
	 * @param user
	 *            검색하고자 하는 사용자
	 * @param from
	 *            검색하고자 하는 시작 일자
	 * @param to
	 *            검색하고자 하는 끝 일자
	 * @return 일자별로 정렬된 리스트
	 */
	public ArrayList<FitnessList> selectFitnessList(User user, String from,
			String to) {
		ContentValues condition = new ContentValues();
		condition.put("userId", user.id);
		condition.put(SQL_CONDITION_FROM, from);
		condition.put(SQL_CONDITION_TO, to);
		return selectFitnessList(condition);
	}


	public void testSQL(){
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery("select id,listId from fitnessData", null);
		while (cursor.moveToNext()) {
			Log.i(tag, "id : "+cursor.getInt(0));
			Log.i(tag, "listId : "+cursor.getInt(1));
			
		}
		cursor.close();
	}
}
