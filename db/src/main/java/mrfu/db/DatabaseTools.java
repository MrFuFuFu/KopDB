package mrfu.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

public class DatabaseTools {
	
	public static void alterTable(SQLiteDatabase db, String TableName,
			List<String> lstColumns) {
		if (db == null || TextUtils.isEmpty(TableName)) {
			return;
		}
		if (lstColumns != null && lstColumns.size() > 0) {
			for (String str : lstColumns) {
				db.execSQL("ALTER TABLE " + TableName + " ADD " + str);
			}
		}
	}

	/**
	 * 根据数据模型获取创建表的sql语句
	 * 
	 * @param claz
	 * @return ������sql���
	 */
	public static String generateCreateTableSQL(Class<?> claz) {
		String sql = "";
		ArrayList<String> fieldsSQL = getFieldsSQL(claz);
		String tableName = getTableName(claz);
		if(fieldsSQL != null && fieldsSQL.size() > 0 && !TextUtils.isEmpty(tableName)){
			sql = "create table if not exists "
					+ tableName + " (";
			for(String temp : fieldsSQL){
				sql += temp;
			}
			int index = sql.lastIndexOf(",");
			if (index != -1) {
				sql = sql.substring(0, index);
			}
			sql += ");";
		}
		return sql;
	}
	
	/**
	 * 根据数据模型返回map : key是有效的列 value是列的数据类型
	 * @param claz
	 * @return
	 */
	public static HashMap<DatabaseField,String> getDatabaseFields(Class<?> claz){
		HashMap<DatabaseField,String> map = null;
		if(claz != null){
			for(Class<?> clazz = claz ; clazz != Object.class ; clazz = clazz.getSuperclass()) {
				Field[] fields = clazz.getDeclaredFields();
				if (fields != null && fields.length > 0) {
					for (Field field : fields) {
						if (field != null) {
							DatabaseField dbField = field.getAnnotation(DatabaseField.class);
							if (dbField != null) {
								String columnName = dbField.columnName();
								if (TextUtils.isEmpty(columnName)) {
									continue;
								}
								String typeSQL = getTypeSQL(field);
								if (TextUtils.isEmpty(typeSQL)) {
									continue;
								}
								if(map == null){
									map = new HashMap<DatabaseField, String>();
								}
								map.put(dbField, typeSQL);
							}
						}
					}
				}
			}
		}
		return map;
	}
	
	private static ArrayList<String> getFieldsSQL(Class<?> claz){
		ArrayList<String> list = null;
		if (claz != null) {
			HashMap<DatabaseField,String> map = getDatabaseFields(claz);
			if(map != null && map.size() > 0){
				Iterator<Entry<DatabaseField,String>> iterator = map.entrySet().iterator();
				if(iterator != null){
					while(iterator.hasNext()){
						Entry<DatabaseField,String> entry = iterator.next();
						if(entry != null){
							DatabaseField key = entry.getKey();
							String typeSQL = entry.getValue();
							String columnName = key.columnName();
							
							String sql = columnName;
							sql += " ";
							sql += typeSQL;
							sql += " ";
							sql += getDatabaseFieldSQL(key);
							sql += ",";
							if(list == null){
								list = new ArrayList<String>(); 
//								String first = "_id integer primary key autoincrement ,";
//								list.add(first);
							}
							list.add(sql);
						}
					}
				}
			}
		}
		return list;
	}

	/**
	 * 根据数据模型获取表的索引
	 * @param claz
	 * @return
	 */
	public static ArrayList<String> generateCreateTableIndexSQL(Class<?> claz) {
		ArrayList<String> indexList = null;
		if (claz != null) {
			String tableName = getTableName(claz);
			if (!TextUtils.isEmpty(tableName)) {
				HashMap<DatabaseField,String> map = getDatabaseFields(claz);
				if(map != null && map.size() > 0){
					Iterator<Entry<DatabaseField,String>> iterator = map.entrySet().iterator();
					if(iterator != null){
						while(iterator.hasNext()){
							Entry<DatabaseField,String> entry = iterator.next();
							if(entry != null){
								DatabaseField key = entry.getKey();
								if(key != null && key.index()){
									if (indexList == null) {
										indexList = new ArrayList<String>();
									}
									String columnName = key.columnName();
									String createIndex = "create index if not exists index_"
											+ columnName
											+ " on "
											+ tableName
											+ "(" + columnName + ");";
									indexList.add(createIndex);
								}
							}
						}
					}
				}
			}
		}
		return indexList;
	}
		
	/**
	 * 创建表和相应的索引
	 * @param claz
	 * @param db
	 */
	public static void onCreate(SQLiteDatabase db,Class<?> claz){
		String tableSQL = generateCreateTableSQL(claz);
		if(!TextUtils.isEmpty(tableSQL) && db != null){
			db.execSQL(tableSQL);
		}
		ArrayList<String> indexSQLList = generateCreateTableIndexSQL(claz);
		if(indexSQLList != null && indexSQLList.size() > 0 && db != null){
			for(String indexSQL : indexSQLList){
				if(!TextUtils.isEmpty(indexSQL)){
					db.execSQL(indexSQL);
				}
			}
		}
	}
	/**
	 * 数据库升级
	 * @param db
	 */
	public static void onUpgrade(SQLiteDatabase db,String dbName,Context context,Class<?> claz){
		String tableName = getTableName(claz);
		if (!TextUtils.isEmpty(tableName) && !TextUtils.isEmpty(dbName) && context != null) {
			String desc = "select * from " + tableName + " limit 1";
			Cursor cursor = null;
			if (db != null) {
				try {
					cursor = db.rawQuery(desc, null);
				} catch (SQLiteException e) {
					if (cursor != null) {
						cursor.close();
						cursor = null;
					}
				}
			}
			if (cursor != null) {
				String[] oldColumns = cursor.getColumnNames();
				ArrayList<String> newColumns = getFieldsSQL(claz);
				if(oldColumns != null && oldColumns.length > 0){
					if(newColumns != null && newColumns.size() > 0){
						for(String temp : newColumns){
							if(!TextUtils.isEmpty(temp)){
								int index = temp.indexOf(" ");
								if(index != -1){
									String newColumnName = temp.substring(0, index);
									if(!TextUtils.isEmpty(newColumnName)){
										boolean isMatch = false;
										for(String old : oldColumns){
											if(newColumnName.equals(old)){
												isMatch = true;
												break;
											}
										}
										if(!isMatch){
											String sql = "ALTER TABLE " + tableName + " ADD " + temp;
											if(sql.indexOf(",") != -1){
												sql = sql.replace(",", ";");
											}
											db.execSQL(sql);
										}
									}
								}
							}
						}
					}
				}
				cursor.close();
			}
		}
	}

	public static String getTypeSQL(Field field) {
		String sql = "";
		if(field != null){
			if (field.getType() == long.class 
					|| field.getType() == int.class
					|| field.getType() == short.class
					|| field.getType() == byte.class
					|| field.getType() == boolean.class
					|| field.getType() == char.class) {
				sql = "integer";
			} else if (field.getType() == float.class
					|| field.getType() == double.class) {
				sql = "real";
			} else if (field.getType() == String.class) {
				sql = "text";
			}else{//全部用gjson转换成字符串
				sql = "text";
			}
		}
		return sql;
	}

	public static String getDatabaseFieldSQL(DatabaseField dbField) {
		String sql = "";
		if (dbField != null) {
			if (dbField.generatedId()) {
				sql += "primary key autoincrement ";
			} else {
				if (!dbField.canBeNull()) {
					sql += "not null ";
				}
				if (dbField.unique()) {
					sql += "unique ";
				}
			}
		}
		return sql;
	}

	/**
	 * 根据数据模型获取表的名字
	 * 
	 * @param claz
	 * @return
	 */
	public static String getTableName(Class<?> claz) {
		String tableName = "";
		if (claz != null) {
			String className = claz.getName();
			if (!TextUtils.isEmpty(className)) {
				int index = className.lastIndexOf(".");
				if (index != -1) {
					tableName = className.substring(index + 1);
				} else {
					tableName = className;
				}
			}
		}
		return tableName;
	}

}
