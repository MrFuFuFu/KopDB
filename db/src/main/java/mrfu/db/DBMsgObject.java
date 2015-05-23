package mrfu.db;

import android.content.ContentValues;

import java.util.List;


public class DBMsgObject <T extends BaseModel>{
    public Class<T> claz;
    public List<ContentCondition<T>> contentConditionList;
    DBOperateAsyncListener listener;
    DBOperateDeleteListener deleteListener;
    
    public static class ContentCondition <T extends BaseModel>{
    	public String whereClause;
    	public String[] whereArgs;
    	public ContentValues contentValues;
    	public T model;
    }
}
