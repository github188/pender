package com.vendor.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface FieldMeta {
	
	public String name() 
	  default "";

	public String value()  
	  default "";

	public boolean nullable() 
	  default false;
	
	public int sort() 
	  default 0;
}
