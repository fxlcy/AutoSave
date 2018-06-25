package com.noober.utils;

import com.squareup.javapoet.ClassName;

public class TypeUtil {
    public static final ClassName BUNDLE = ClassName.get("android.os", "Bundle");
    public static final ClassName PERSISTABLE_BUNDLE = ClassName.get("android.os", "PersistableBundle");
    public static final ClassName IHELPER = ClassName.get("com.noober.savehelper", "ISaveInstanceStateHelper");

    public static final ClassName ABS_SAVE_HELPER = ClassName.get("com.noober.savehelper", "AbsSaveHelper");
    public static final ClassName SAVE_HELPER = ClassName.get("com.noober.savehelper", "SaveHelper");


    public static final String SUPPLIER = "com.noober.api.Supplier";
}
