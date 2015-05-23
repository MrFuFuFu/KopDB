package mrfu.kopdb;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager.NameNotFoundException;

import java.util.ArrayList;
import java.util.List;

import mrfu.db.BaseModel;
import mrfu.db.DBOperateAsyncListener;
import mrfu.db.DBOperateDeleteListener;
import mrfu.db.DatabaseManager;
import mrfu.db.DatabaseOptionType; 


public class MainActivity extends Activity implements View.OnClickListener {
    private Context mContext = MainActivity.this;
    private TextView tv_model;
    private Button btn_insert_model_callback;
    private Button btn_update_model_callback;
    private Button btn_replace_model_callback;
    private Button btn_delete_model_callback;
    private TextView need_select_model;

    private static final String sampleString = "Person person = new Person();";
    private static int index_id;
    private static int last_rows_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDB();
        tv_model = (TextView) findViewById(R.id.tv_model);
        tv_model.setText(sampleString);
        btn_insert_model_callback = (Button) findViewById(R.id.btn_insert_model_callback);
        btn_insert_model_callback.setOnClickListener(this);
        btn_update_model_callback = (Button) findViewById(R.id.btn_update_model_callback);
        btn_update_model_callback.setOnClickListener(this);
        btn_replace_model_callback = (Button) findViewById(R.id.btn_replace_model_callback);
        btn_replace_model_callback.setOnClickListener(this);
        btn_delete_model_callback = (Button) findViewById(R.id.btn_delete_model_callback);
        btn_delete_model_callback.setOnClickListener(this);
        need_select_model = (TextView) findViewById(R.id.need_select_model);
        callBackSuccess("init");
    }

    private void initDB() {
        if (DatabaseManager.getInstance().isInited())
            return;
        String dbName = "person.db";
        int version = 0;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(PersonModel.class);
        DatabaseManager.getInstance().initDataBase(getApplicationContext(), dbName, version, list);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_insert_model_callback:
                DatabaseManager.getInstance().insert(PersonModel.class, insertData(), new DBOperateAsyncListener() {
                    @Override
                    public <T extends BaseModel> void onPostExecute(DatabaseOptionType optionType, Class<T> claz, List<T> successModels, List<T> failModels) {
                        if (successModels != null) {
                            Toast.makeText(MainActivity.this, "insert Success = " + ((PersonModel) (successModels.get(0))).toString(), Toast.LENGTH_SHORT).show();
                        } else if (failModels != null) {
                            Toast.makeText(MainActivity.this, "insert Failure = " + ((PersonModel) (failModels.get(0))).toString(), Toast.LENGTH_SHORT).show();
                        }
                        callBackSuccess("insert");
                    }
                });
                break;
            case R.id.btn_update_model_callback:
                DatabaseManager.getInstance().update(PersonModel.class, updateData(), new DBOperateAsyncListener() {
                    @Override
                    public <T extends BaseModel> void onPostExecute(DatabaseOptionType optionType, Class<T> claz, List<T> successModels, List<T> failModels) {
                        if (successModels != null) {
                            Toast.makeText(MainActivity.this, "update Success = " + ((PersonModel) (successModels.get(0))).toString(), Toast.LENGTH_SHORT).show();
                        } else if (failModels != null) {
                            Toast.makeText(MainActivity.this, "update Failure = " + ((PersonModel) (failModels.get(0))).toString(), Toast.LENGTH_SHORT).show();
                        }
                        callBackSuccess("update");
                    }
                });
                break;
            case R.id.btn_replace_model_callback:
                DatabaseManager.getInstance().replace(PersonModel.class, replaceData(), new DBOperateAsyncListener() {
                    @Override
                    public <T extends BaseModel> void onPostExecute(DatabaseOptionType optionType, Class<T> claz, List<T> successModels, List<T> failModels) {
                        if (successModels != null) {
                            Toast.makeText(MainActivity.this, "replace Success = " + ((PersonModel) (successModels.get(0))).toString(), Toast.LENGTH_SHORT).show();
                        } else if (failModels != null) {
                            Toast.makeText(MainActivity.this, "replace Failure = " + ((PersonModel) (failModels.get(0))).toString(), Toast.LENGTH_SHORT).show();
                        }
                        callBackSuccess("replace");
                    }
                });
                break;
            case R.id.btn_delete_model_callback:
                DatabaseManager.getInstance().delete(PersonModel.class, PersonModel.PERSON_ID + " = ?", new String[]{String.valueOf(last_rows_id)}, new DBOperateDeleteListener() {
                    @Override
                    public <T extends BaseModel> void onDeleteCallback(Class<T> claz, int rows) {
                        Toast.makeText(MainActivity.this, "rows = " + rows, Toast.LENGTH_SHORT).show();
                        callBackSuccess("delete");
                    }
                });

                break;
            default:
                break;
        }
    }

    private void callBackSuccess(String string) {
        List<PersonModel> list = DatabaseManager.getInstance().select(PersonModel.class);
        if (list == null) {
            return;
        }
        if (list.size() > 0) {
            last_rows_id = list.get(list.size() - 1).id;
        }
        index_id = list.size();
        String operationOverData = "";
        for (PersonModel model : list) {
            operationOverData += model.toString() + " \n ";
        }
        need_select_model.setText(string + " feedback data is : \n " + operationOverData);
    }

    private PersonModel insertData() {
        PersonModel model = new PersonModel();
        index_id++;
        Log.i("MrFu", "index_id =" + index_id);
        model.id = index_id;
        model.name = "MrFu" + index_id;
        model.age = "25" + index_id;
        model.address = "HangZhou" + index_id;
        model.phone = "0000" + index_id;
        return model;
    }

    private PersonModel updateData() {
        PersonModel model = new PersonModel();
        model.id = 1;
        model.name = "MrFu for update";
        model.age = "25 for update";
        model.address = "HangZhou for update";
        model.phone = "0000  for update ";
        return model;
    }

    private PersonModel replaceData() {
        PersonModel model = new PersonModel();
        model.id = 1;
        model.name = "MrFu for replace";
        model.age = "25 for replace";
        model.address = "HangZhou for replace";
        model.phone = "0000  for replace ";
        return model;
    }
}
