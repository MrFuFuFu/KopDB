package mrfu.db;

import java.util.List;

public interface DBOperateAsyncListener {
	/**
	 * database option result 
	 * @param retResult
	 * @param model
	 */
	public <T extends BaseModel> void onPostExecute(DatabaseOptionType optionType, Class<T> claz, List<T> successModels, List<T> failModels);
}
