package mrfu.db;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(FIELD)
@Retention(RUNTIME)
public @interface DatabaseField {
	
	String columnName() default "";//列名
	
	boolean generatedId() default false; 
	
	boolean index() default false; //是否需要索引
	
	boolean canBeNull() default true;//是否可以为空
	
	boolean unique() default false; //是否唯一
}
