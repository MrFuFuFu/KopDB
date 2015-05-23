package mrfu.db;

/**
 * 需要存到数据库的对象继承该类
 */
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1369952229466292190L;
	
	public static final String ROWID = "_id";
	
	@DatabaseField(columnName = ROWID,generatedId = true)
	private long rowId;

	public long getRowId() {
		return rowId;
	}

	public void setRowId(long rowId) {
		this.rowId = rowId;
	}

	public final ContentValues getContentValues() throws IllegalArgumentException, IllegalAccessException{
		ContentValues values = new ContentValues();
		for(Class<?> clazz = getClass() ; clazz != Object.class ; clazz = clazz.getSuperclass()) {
			Field[] fields = clazz.getDeclaredFields();
			if (fields != null && fields.length > 0) {
				for (Field field : fields) {
					if (field != null) {
						DatabaseField dbField = field.getAnnotation(DatabaseField.class);
						if (dbField != null) {
							String columnName = dbField.columnName();
							if (TextUtils.isEmpty(columnName) || ROWID.equals(columnName)) {
								continue;
							}
							field.setAccessible(true);
							Class<?> type = field.getType();
							if (type == long.class ){
								values.put(columnName, (Long)field.get(this));
							}else if(type == int.class){
								values.put(columnName, (Integer)field.get(this));
							}else if(type == short.class){
								values.put(columnName, (Short)field.get(this));
							}else if(type == byte.class){
								values.put(columnName, (Byte)field.get(this));
							} else if (type == float.class){
								values.put(columnName, (Float)field.get(this));
							}else if(type == double.class) {
								values.put(columnName, (Double)field.get(this));
							} else if (type == String.class){
								values.put(columnName, (String)field.get(this));
							} else if(type == char.class){
								char charValue = (Character)field.get(this);
								int intValue = (int)charValue;
								values.put(columnName,intValue);
							}else if (type == boolean.class) {
								values.put(columnName, (Boolean)field.get(this));
							}else{
								Gson gson = new Gson();
								String gsonStr = gson.toJson(field.get(this));
								values.put(columnName, gsonStr);
							}
						}
					}
				}
			}
		}
		
		return values;
	}
	
	@SuppressWarnings("unchecked")
	public final <T extends BaseModel> List<T> getModels(Cursor cursor) throws JsonSyntaxException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		if(cursor != null && cursor.getCount() > 0){
			List<T> list = new ArrayList<T>();
			while(cursor.moveToNext()){
				Object obj = getClass().newInstance();
				list.add((T)obj);
				for(Class<?> clazz = getClass() ; clazz != Object.class ; clazz = clazz.getSuperclass()) {
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
									field.setAccessible(true);
									int columnIndex = cursor.getColumnIndex(columnName);
									Class<?> type = field.getType();
									if (type == long.class ){
										field.set(obj, cursor.getLong(columnIndex));
									}else if(type == int.class){
										field.set(obj, cursor.getInt(columnIndex));
									}else if(type == short.class){
										field.set(obj, cursor.getShort(columnIndex));
									}else if(type == byte.class){
										byte byteValue = (byte)cursor.getInt(columnIndex);
										field.set(obj,byteValue);
									} else if (type == float.class){
										field.set(obj, cursor.getFloat(columnIndex));
									}else if(type == double.class) {
										field.set(obj, cursor.getDouble(columnIndex));
									} else if (type == String.class) {
										field.set(obj, cursor.getString(columnIndex));
									}else if(type == char.class){
										char charValue = (char)cursor.getInt(columnIndex);
										field.set(obj, charValue);
									}else if (type == boolean.class) {
										boolean boolValue = cursor.getInt(columnIndex) > 0 ? true : false;
										field.set(obj,boolValue);
									}else {
										String gsonStr = cursor.getString(columnIndex);
										Gson gson = new Gson();
										field.set(obj, gson.fromJson(gsonStr, type));
									}
								}
							}
						}
					}
				}
			}
			return list;
		}
		return null;
	}
}
