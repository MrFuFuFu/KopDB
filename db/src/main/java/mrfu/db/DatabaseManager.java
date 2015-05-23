package mrfu.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class DatabaseManager {
	private static DatabaseManager databaseManager;
	private AppDatabaseHelper appAppDatabase;
	private DataBaseHandler handler;
	private HashMap<Class<?>, String> uniqueMap;
	private boolean isInited = false;;

	private DatabaseManager() {
		uniqueMap = new HashMap<Class<?>, String>();
	}

	public synchronized boolean isInited() {
		return isInited;
	}

	public synchronized void setInited(boolean isInited) {
		this.isInited = isInited;
	}
	/**
	 * 初始化数据化
	 * 
	 * @param context
	 * @param dbName
	 * @param version
	 */
	public void initDataBase(Context context, String dbName, int version, List<Class<?>> models) {
		if(!isInited()){
			Log.d("kop", "dbName = "+dbName);
			if (appAppDatabase == null) {
				appAppDatabase = new AppDatabaseHelper(context, dbName, null,
						version,models);
			}
			HandlerThread ht = new HandlerThread("dbOption");
			ht.start();
			handler = new DataBaseHandler(appAppDatabase, ht.getLooper());
			setInited(true);
		}
	}

	public synchronized static DatabaseManager getInstance() {
		if (databaseManager == null) {
			databaseManager = new DatabaseManager();
		}
		return databaseManager;
	}
	public static DatabaseManager newInstance(){
		return new DatabaseManager();
	}
	private void closeDB() {
		if (appAppDatabase != null) {
			appAppDatabase.close();
			appAppDatabase = null;
		}
	}
	
	public synchronized <T extends BaseModel> List<T> select(Class<T> claz){
		return select(claz, null, null, null, null, null, null, null);
	}

	/**
	 * 
	 * @param claz
	 * @param columns  要查询的列所有名称集
	 * @param selection  WHERE之后的条件语句，可以使用占位符
	 * @param selectionArgs  占位符实际参数集
	 * @param groupBy  groupBy指定分组的列名
	 * @param having   指定分组条件，配合groupBy使用
	 * @param orderBy  指定排序的列名
	 * @param limit    指定分页参数
	 * @return
	 */
	public synchronized <T extends BaseModel> List<T> select(Class<T> claz,
			String[] columns, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy, String limit) {
		String table = DatabaseTools.getTableName(claz);
		if (TextUtils.isEmpty(table)) {
			return null;
		}
		SQLiteDatabase database = appAppDatabase.getReadableDatabase();
		if (database != null) {
			Cursor cursor = database.query(table, columns, selection,
					selectionArgs, groupBy, having, orderBy, limit);
			BaseModel baseModel = null;
			try {
				baseModel = (BaseModel) claz.newInstance();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<T> list = null;
			if (baseModel != null) {
				try {
					list = baseModel.getModels(cursor);
				} catch (JsonSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(cursor != null){
				cursor.close();
			}
			return list;
		}
		return null;
	}

	public <T extends BaseModel> void insert(Class<T> claz, T t,
			DBOperateAsyncListener listener) {
		if (claz != null && t != null) {
			DBMsgObject<T> msgObj = new DBMsgObject<T>();
			msgObj.claz = claz;
			ContentValues contentValues = null;
			try {
				contentValues = t.getContentValues();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (contentValues != null) {
				List<DBMsgObject.ContentCondition<T>> contentConditionList = new ArrayList<DBMsgObject.ContentCondition<T>>();
				DBMsgObject.ContentCondition<T> condition = new DBMsgObject.ContentCondition<T>();
				condition.model = t;
				condition.contentValues = contentValues;
				contentConditionList.add(condition);
				msgObj.contentConditionList = contentConditionList;
				msgObj.listener = listener;
				Message msg = new Message();
				msg.what = DatabaseOptionType.OPTION_INSERT.VALUE;
				msg.obj = msgObj;
				handler.sendMessage(msg);
			}
		}
	}

	public <T extends BaseModel> void insert(Class<T> claz, T t) {
		insert(claz, t, null);
	}

	public <T extends BaseModel> void insert(Class<T> claz, List<T> models,
			DBOperateAsyncListener listener) {
		if (claz != null && models != null && models.size() > 0) {
			DBMsgObject<T> msgObj = new DBMsgObject<T>();
			msgObj.claz = claz;
			List<DBMsgObject.ContentCondition<T>> list = new ArrayList<DBMsgObject.ContentCondition<T>>();
			for (T model : models) {
				ContentValues contentValues = null;
				try {
					contentValues = model.getContentValues();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (contentValues != null) {
					DBMsgObject.ContentCondition<T> condition = new DBMsgObject.ContentCondition<T>();
					condition.contentValues = contentValues;
					condition.model = model;
					list.add(condition);
				}
			}
			if (list.size() > 0) {
				msgObj.contentConditionList = list;
				msgObj.listener = listener;
				Message msg = new Message();
				msg.what = DatabaseOptionType.OPTION_INSERT.VALUE;
				msg.obj = msgObj;
				handler.sendMessage(msg);
			}
		}
	}
	public   <T extends BaseModel> long syncInsert( Class<T> claz, T  value) {
	    return syncInsert(claz,value,true);
	}
	public  <T extends BaseModel> long syncInsert( Class<T> claz, T value,boolean bNeedNotify) {
		if(appAppDatabase == null){
            return -1;
        }
		long retValue=-1;
		try{
			if (  value != null) {
				Message msg = new Message();
				String table = DatabaseTools.getTableName(claz);
				ContentValues contentValues = null;
				try {
					contentValues = value.getContentValues();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


//				if (false == isWithinTransaction()) {
					SQLiteDatabase database = appAppDatabase
							.getWritableDatabase();
					if (database == null) {
						return -1;
					}
					database.beginTransaction();
					try {
//						retValue=DatabaseUtil.insertNoTx(msg, database);
						database.insert(table, null, contentValues);
						//dataChange(table);
						database.setTransactionSuccessful();
					} finally {
						database.endTransaction();
					}
//					if(bNeedNotify){
//					    dataChange(table);
//					}
//				} else {
//					TLSTransactionObject tlsObj = (TLSTransactionObject) mTls
//							.get();
//					tlsObj.setSyncOpDB(true);
//					tlsObj.getMsgList().add(msg);
//				}
			}
			return retValue;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return retValue;
	}
	public <T extends BaseModel> void insert(Class<T> claz, List<T> models) {
		insert(claz, models, null);
	}

	public <T extends BaseModel> void update(Class<T> claz, T t,
			DBOperateAsyncListener listener) {
		if (claz != null && t != null) {
			DBMsgObject<T> msgObj = new DBMsgObject<T>();
			msgObj.claz = claz;
			ContentValues contentValues = null;
			try {
				contentValues = t.getContentValues();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (contentValues != null) {
				String unique = getUniqueColumn(claz);
				if (!TextUtils.isEmpty(unique)) {
					List<DBMsgObject.ContentCondition<T>> contentConditionList = new ArrayList<DBMsgObject.ContentCondition<T>>();
					DBMsgObject.ContentCondition<T> condition = new DBMsgObject.ContentCondition<T>();
					condition.contentValues = contentValues;
					condition.model = t;
					condition.whereClause = unique + " = ?";
					condition.whereArgs = new String[] { contentValues
							.getAsString(unique) };
					contentConditionList.add(condition);
					msgObj.contentConditionList = contentConditionList;
					msgObj.listener = listener;
					Message msg = new Message();
					msg.what = DatabaseOptionType.OPTION_UPDATE.VALUE;
					msg.obj = msgObj;
					handler.sendMessage(msg);
				}
			}
		}
	}

	public <T extends BaseModel> void update(Class<T> claz, T t) {
		update(claz, t, null);
	}

	public <T extends BaseModel> void update(Class<T> claz, List<T> models,
			DBOperateAsyncListener listener) {
		if (claz != null && models != null && models.size() > 0) {
			DBMsgObject<T> msgObj = new DBMsgObject<T>();
			msgObj.claz = claz;
			List<DBMsgObject.ContentCondition<T>> contentConditionList = new ArrayList<DBMsgObject.ContentCondition<T>>();
			String unique = getUniqueColumn(claz);
			if (!TextUtils.isEmpty(unique)) {
				for (T model : models) {
					ContentValues contentValues = null;
					try {
						contentValues = model.getContentValues();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (contentValues != null) {
						if (!TextUtils.isEmpty(unique)) {
							String whereClause = unique + " = ?";
							String whereArgs = contentValues
									.getAsString(unique);
							DBMsgObject.ContentCondition<T> condition = new DBMsgObject.ContentCondition<T>();
							condition.whereClause = whereClause;
							condition.whereArgs = new String[] { whereArgs };
							condition.contentValues = contentValues;
							condition.model = model;
							contentConditionList.add(condition);
						}
					}
				}
				msgObj.contentConditionList = contentConditionList;
				msgObj.listener = listener;
				Message msg = new Message();
				msg.what = DatabaseOptionType.OPTION_UPDATE.VALUE;
				msg.obj = msgObj;
				handler.sendMessage(msg);
			}
		}
	}

	public <T extends BaseModel> void update(Class<T> claz, List<T> models) {
		update(claz, models, null);
	}

	public <T extends BaseModel> void replace(Class<T> claz, T t,
			DBOperateAsyncListener listener) {
		if (claz != null && t != null) {
			DBMsgObject<T> msgObj = new DBMsgObject<T>();
			msgObj.claz = claz;
			ContentValues contentValues = null;
			try {
				contentValues = t.getContentValues();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (contentValues != null) {
				List<DBMsgObject.ContentCondition<T>> contentConditionList = new ArrayList<DBMsgObject.ContentCondition<T>>();
				DBMsgObject.ContentCondition<T> condition = new DBMsgObject.ContentCondition<T>();
				condition.contentValues = contentValues;
				condition.model = t;
				contentConditionList.add(condition);
				msgObj.contentConditionList = contentConditionList;
				msgObj.listener = listener;
				Message msg = new Message();
				msg.what = DatabaseOptionType.OPTION_REPLACE.VALUE;
				msg.obj = msgObj;
				handler.sendMessage(msg);
			}
		}
	}

	public <T extends BaseModel> void replace(Class<T> claz, T t) {
		replace(claz, t, null);
	}

	public <T extends BaseModel> void replace(Class<T> claz, List<T> models,
			DBOperateAsyncListener listener) {
		if (claz != null && models != null && models.size() > 0) {
			DBMsgObject<T> msgObj = new DBMsgObject<T>();
			msgObj.claz = claz;
			List<DBMsgObject.ContentCondition<T>> contentConditionList = new ArrayList<DBMsgObject.ContentCondition<T>>();
			for (T model : models) {
				ContentValues contentValues = null;
				try {
					contentValues = model.getContentValues();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (contentValues != null) {
					DBMsgObject.ContentCondition<T> condition = new DBMsgObject.ContentCondition<T>();
					condition.contentValues = contentValues;
					condition.model = model;
					contentConditionList.add(condition);
				}
			}
			if (contentConditionList.size() > 0) {
				msgObj.contentConditionList = contentConditionList;
				msgObj.listener = listener;
				Message msg = new Message();
				msg.what = DatabaseOptionType.OPTION_REPLACE.VALUE;
				msg.obj = msgObj;
				handler.sendMessage(msg);
			}
		}
	}

	public <T extends BaseModel> void replace(Class<T> claz, List<T> models) {
		replace(claz, models, null);
	}

	public <T extends BaseModel> void delete(Class<T> claz, String whereClause,
			String[] whereArgs, DBOperateDeleteListener listener) {
		if (claz != null) {
			DBMsgObject<T> msgObj = new DBMsgObject<T>();
			msgObj.claz = claz;
			List<DBMsgObject.ContentCondition<T>> contentConditionList = new ArrayList<DBMsgObject.ContentCondition<T>>();
			DBMsgObject.ContentCondition<T> condition = new DBMsgObject.ContentCondition<T>();
			contentConditionList.add(condition);
			if (!TextUtils.isEmpty(whereClause)) {
				condition.whereClause = whereClause;
			}
			if (whereArgs != null && whereArgs.length > 0) {
				condition.whereArgs = whereArgs;
			}
			msgObj.contentConditionList = contentConditionList;
			msgObj.deleteListener = listener;
			Message msg = new Message();
			msg.what = DatabaseOptionType.OPTION_DELETE.VALUE;
			msg.obj = msgObj;
			handler.sendMessage(msg);
		}
	}

	public <T extends BaseModel> void delete(Class<T> claz, String whereClause,
			String[] whereArgs) {
		delete(claz, whereClause, whereArgs, null);
	}

	private <T extends BaseModel> String getUniqueColumn(Class<T> claz) {
		String unique = uniqueMap.get(claz);
		if (TextUtils.isEmpty(unique)) {
			HashMap<DatabaseField, String> map = DatabaseTools
					.getDatabaseFields(claz);
			if (map != null && map.size() > 0) {
				Iterator<Entry<DatabaseField, String>> iterator = map
						.entrySet().iterator();
				String tempUnique = "";
				if (iterator != null) {
					while (iterator.hasNext()) {
						Entry<DatabaseField, String> entry = iterator.next();
						DatabaseField field = entry.getKey();
						if (field != null) {
							if (TextUtils.isEmpty(tempUnique)) {
								tempUnique = field.columnName();
							}
							if (field.unique()) {
								unique = field.columnName();
								uniqueMap.put(claz, unique);
								break;
							}
						}
					}
				}
				// 如果没有unique的列,就默认第一列作为unique列
				if (TextUtils.isEmpty(unique) && !TextUtils.isEmpty(tempUnique)) {
					unique = tempUnique;
					uniqueMap.put(claz, unique);
				}
			}
		}
		return unique;
	}

	public void recycle() {
		closeDB();
		if (handler != null) {
			handler.getLooper().quit();
			handler = null;
		}
		if (uniqueMap != null) {
			uniqueMap.clear();
			uniqueMap = null;
		}
		if (databaseManager != null) {
			databaseManager = null;
		}
	}
}
