package mrfu.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.List;

public class AppDatabaseHelper extends SQLiteOpenHelper{
	private Context context;
	private String dbName;
	private List<Class<?>> models;
	
	public AppDatabaseHelper(Context context, String name, CursorFactory factory, int version,List<Class<?>> models){
		super(context, name, factory, version);
		this.context = context;
		this.dbName = name;
		this.models = models;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
//		DatabaseTools.onCreate(db,User.class);
		if(models != null && models.size() > 0){
			for(Class<?> claz : models){
				DatabaseTools.onCreate(db,claz);
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		if(newVersion > oldVersion){
//			DatabaseTools.onUpgrade(db, dbName, context, User.class);
			if(models != null && models.size() > 0){
				for(Class<?> claz : models){
					DatabaseTools.onUpgrade(db, dbName, context, claz);
				}
			}
			onCreate(db);
		}else if(newVersion < oldVersion){
			if(context != null && !TextUtils.isEmpty(dbName)){
				context.deleteDatabase(dbName);
			}
		}
	}

	/**
	 * android sdk version 11 以上 降级会调用该函数
	 */
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		if(context != null && !TextUtils.isEmpty(dbName)){
			context.deleteDatabase(dbName);
		}
	}
	
}
