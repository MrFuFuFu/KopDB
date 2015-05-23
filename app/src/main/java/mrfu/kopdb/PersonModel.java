package mrfu.kopdb;

import mrfu.db.BaseModel;
import mrfu.db.DatabaseField;

/**
 * @author Mr.傅
 * 2015-5-20 下午2:07:19
 */
public class PersonModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3462436436344054489L;

	public static final String PERSON_ID = "person_id";
	public static final String PERSON_NAME = "person_name";
	public static final String PERSON_AGE = "person_age";
	public static final String PERSON_ADDRESS = "person_address";
	public static final String PERSON_PHONE = "person_phone";
	
	@DatabaseField(columnName = PERSON_ID, index = true, unique = true, canBeNull = false)
	public int id;
	
	@DatabaseField(columnName = PERSON_NAME)
	public String name;
	
	@DatabaseField(columnName = PERSON_AGE)
	public String age;
	
	@DatabaseField(columnName = PERSON_ADDRESS)
	public String address;
	
	@DatabaseField(columnName = PERSON_PHONE)
	public String phone;

	@Override
	public String toString() {
		return "id=" + id + "\r name=" + name + "\r age=" + age + "\r address=" + address + "\r phone=" + phone;
	}
	
}
