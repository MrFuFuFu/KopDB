package mrfu.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import mrfu.db.DBMsgObject.ContentCondition;

public class DataBaseHandler extends Handler {
	private AppDatabaseHelper appAppDatabase;
	private Handler uiHandler;
	
	public DataBaseHandler(AppDatabaseHelper apAppDatabase, Looper looper) {
		super(looper);
		this.appAppDatabase = apAppDatabase;
		uiHandler = new Handler(Looper.getMainLooper());
		try{
			apAppDatabase.getWritableDatabase();
		}catch(Exception e){
			
		}
	}

	@Override
	public void handleMessage(Message msg) {
		if (msg != null) {
			try{
			switch (DatabaseOptionType.VALUEOF(msg.what)) {
				case OPTION_INSERT:
					insert(msg);
					break;
				case OPTION_UPDATE:
					update(msg);
					break;
				case OPTION_REPLACE:
					replace(msg);
					break;
				case OPTION_DELETE:
					delete(msg);
					break;
				default:
					break;
			}
			}catch(Throwable t){
				AZusLog.w("DBHandler", t);
			}
		}
	}

	private <T extends BaseModel> void insert(Message msg){
		if(msg.obj == null){
			return;
		}
		@SuppressWarnings("unchecked")
		DBMsgObject<T> msgObj = (DBMsgObject<T>)msg.obj;
		String tableName = DatabaseTools.getTableName(msgObj.claz);
		if(!TextUtils.isEmpty(tableName) && msgObj.contentConditionList != null && msgObj.contentConditionList.size() > 0){
			SQLiteDatabase database = appAppDatabase
					.getWritableDatabase();
			if (database != null) {
				List<T> successModels = new ArrayList<T>();
				List<T> failModels = new ArrayList<T>();
				database.beginTransaction();
				try{
					for(ContentCondition<T> condition : msgObj.contentConditionList){
						if (condition != null && condition.contentValues != null) {
							long id = database.insert(tableName, null, condition.contentValues);
							if(id != -1){
								successModels.add(condition.model);
							}else{
								failModels.add(condition.model);
							}
						}
					}
					database.setTransactionSuccessful();// 设置事务处理成功，不设置会自动回滚不提交
				}
				finally{
					database.endTransaction();
					postToMainLoop(msgObj.listener,DatabaseOptionType.OPTION_INSERT ,msgObj.claz,successModels, failModels);//失败回调到主线程
				}
			}
		}
	}
	
	private <T extends BaseModel> void update(Message msg) {
		if(msg.obj == null){
			return;
		}
		@SuppressWarnings("unchecked")
		DBMsgObject<T> msgObj = (DBMsgObject<T>)msg.obj;
		String tableName = DatabaseTools.getTableName(msgObj.claz);
		if(!TextUtils.isEmpty(tableName) && msgObj.contentConditionList != null && msgObj.contentConditionList.size() > 0){
			SQLiteDatabase database = appAppDatabase
					.getWritableDatabase();
			if (database != null) {
				List<T> successModels = new ArrayList<T>();
				List<T> failModels = new ArrayList<T>();
				database.beginTransaction();
				try{
					for(ContentCondition<T> condition : msgObj.contentConditionList){
						if (condition != null && condition.contentValues != null) {
							int count = database.update(tableName, condition.contentValues, condition.whereClause, condition.whereArgs);
							if(count > 0){
								successModels.add(condition.model);
							}else{
								failModels.add(condition.model);
							}
						}
					}
					database.setTransactionSuccessful();// 设置事务处理成功，不设置会自动回滚不提交
				}finally{
					database.endTransaction();
					postToMainLoop(msgObj.listener, DatabaseOptionType.OPTION_UPDATE, msgObj.claz, successModels, failModels);
				}
			}
		}
	}
	
	private <T extends BaseModel> void replace(Message msg){
		if(msg.obj == null){
			return;
		}
		@SuppressWarnings("unchecked")
		DBMsgObject<T> msgObj = (DBMsgObject<T>)msg.obj;
		String tableName = DatabaseTools.getTableName(msgObj.claz);
		if(!TextUtils.isEmpty(tableName) && msgObj.contentConditionList != null && msgObj.contentConditionList.size() > 0){
			SQLiteDatabase database = appAppDatabase
					.getWritableDatabase();
			if (database != null) {
				List<T> successModels = new ArrayList<T>();
				List<T> failModels = new ArrayList<T>();
				database.beginTransaction();
				try{
					for(ContentCondition<T> condition : msgObj.contentConditionList){
						if (condition != null && condition.contentValues != null) {
							long id = database.replace(tableName, null, condition.contentValues);
							if(id != -1){
								successModels.add(condition.model);
							}else{
								failModels.add(condition.model);
							}
						}
					}
					database.setTransactionSuccessful();// 设置事务处理成功，不设置会自动回滚不提交
				}finally{
					database.endTransaction();
					postToMainLoop(msgObj.listener, DatabaseOptionType.OPTION_REPLACE, msgObj.claz, successModels, failModels);
				}
			}
		}
	}
	
	private <T extends BaseModel> void delete(Message msg){
		if(msg.obj == null){
			return;
		}
		@SuppressWarnings("unchecked")
		final DBMsgObject<T> msgObj = (DBMsgObject<T>)msg.obj;
		String tableName = DatabaseTools.getTableName(msgObj.claz);
		if(!TextUtils.isEmpty(tableName) && msgObj.contentConditionList != null && msgObj.contentConditionList.size() > 0){
			SQLiteDatabase database = appAppDatabase
					.getWritableDatabase();
			if (database != null) {
				int rows = 0;
				database.beginTransaction();
				try{
					for(ContentCondition<T> condition : msgObj.contentConditionList){
						if (condition != null) {
							rows += database.delete(tableName, condition.whereClause, condition.whereArgs);
						}
					}
					database.setTransactionSuccessful();// 设置事务处理成功，不设置会自动回滚不提交
				}finally{
					database.endTransaction();
					if(msgObj.deleteListener != null){
						final int tempRows = rows;
						uiHandler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								msgObj.deleteListener.onDeleteCallback(msgObj.claz, tempRows);
							}
						});
					}
				}
			}
		}
	}
	
	private <T extends BaseModel> void postToMainLoop(final DBOperateAsyncListener listener,final DatabaseOptionType type,final Class<T> claz,final List<T> successModels,final List<T> failModels){
		if(listener != null){
			uiHandler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
					listener.onPostExecute(type,claz,successModels,failModels);
					}catch(Throwable t){
						AZusLog.e("DBHandler", t);
					}
				}
			});
		}
	}
}
