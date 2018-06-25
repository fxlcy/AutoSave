package com.noober.savehelper;

import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.SparseArray;

@SuppressWarnings("unchecked")
abstract class AbsSaveHelper {
    /**
     * added while need to recover data, used in {@link android.app.Activity#onCreate(Bundle)}
     *
     * @param recover            current Activity or Fragment
     * @param savedInstanceState Bundle
     */
    public <T> void recover(T recover, Bundle savedInstanceState) {
        recover(recover, savedInstanceState, null);
    }

    /**
     * equate to{@link AbsSaveHelper#recover(Object, Bundle)}, used in {@link android.app.Activity#onCreate(Bundle, PersistableBundle)}
     * added in 2.1.0
     *
     * @param recover            current Activity or Fragment
     * @param savedInstanceState Bundle
     */
    public <T> void recover(T recover, Bundle savedInstanceState, PersistableBundle persistentState) {
        if (savedInstanceState != null || persistentState != null) {
            ISaveInstanceStateHelper<T> persistableSaveHelper = (ISaveInstanceStateHelper<T>) findHelper(recover);
            if (persistableSaveHelper != null) {
                persistableSaveHelper.recover(savedInstanceState, persistentState, recover);
            }
        }
    }

    /**
     * added while need to save data, used in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     *
     * @param save     current Activity or Fragment
     * @param outState Bundle
     */
    public <T> void save(T save, Bundle outState) {
        save(save, outState, null);
    }

    /**
     * equate to{@link AbsSaveHelper#save(Object, Bundle)}, used in {@link android.app.Activity#onSaveInstanceState(Bundle, PersistableBundle)}
     * added in 2.1.0
     *
     * @param save     current Activity or Fragment
     * @param outState Bundle
     */
    public <T> void save(T save, Bundle outState, PersistableBundle persistentState) {
        if (outState != null || persistentState != null) {
            ISaveInstanceStateHelper<T> persistableSaveHelper = (ISaveInstanceStateHelper<T>) findHelper(save);
            if (persistableSaveHelper != null) {
                persistableSaveHelper.save(outState, persistentState, save);
            }
        }
    }


    /**
     * added while need to save data, used in custom view
     *
     * @param save      current custom view
     * @param container SparseArray<Parcelable>
     */
    public <T> void save(T save, SparseArray<Parcelable> container) {
        if (container != null) {
            ISaveViewStateHelper<T> viewSaveHelper = (ISaveViewStateHelper<T>) findHelper(save);
            if (viewSaveHelper != null) {
                viewSaveHelper.save(save, container);
            }
        }
    }

    /**
     * recover for custom view
     *
     * @param save      current custom view
     * @param container SparseArray<Parcelable>
     */
    public <T> void recover(T save, SparseArray<Parcelable> container) {
        if (container != null) {
            ISaveViewStateHelper<T> viewSaveHelper = (ISaveViewStateHelper<T>) findHelper(save);
            if (viewSaveHelper != null) {
                viewSaveHelper.recover(save, container);
            }
        }
    }


    protected abstract Object findHelper(Object save);

}
