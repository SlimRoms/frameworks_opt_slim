package com.slim.settings.search;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.SearchIndexablesProvider;

import com.slim.settings.search.Searchable.SearchIndexProvider;
import com.slim.settings.search.SettingsList.SettingInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static android.provider.SearchIndexablesContract.COLUMN_INDEX_NON_INDEXABLE_KEYS_KEY_VALUE;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_ENTRIES;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_ICON_RESID;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_INTENT_ACTION;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_INTENT_TARGET_CLASS;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_INTENT_TARGET_PACKAGE;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_KEY;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_KEYWORDS;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_RANK;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_SCREEN_TITLE;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_SUMMARY_ON;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_TITLE;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_RAW_USER_ID;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_CLASS_NAME;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_ICON_RESID;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_ACTION;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_RANK;
import static android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_RESID;
import static android.provider.SearchIndexablesContract.INDEXABLES_RAW_COLUMNS;
import static android.provider.SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS;
import static android.provider.SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS;

public class SlimSearchIndexablesProvider extends SearchIndexablesProvider {

    private static final String TAG = SlimSearchIndexablesProvider.class.getSimpleName();

    private static final String FIELD_NAME_SEARCH_INDEX_DATA_PROVIDER =
        "SEARCH_INDEX_DATA_PROVIDER";

    @Override
    public Cursor queryXmlResources(String[] strings) {
        MatrixCursor cursor = new MatrixCursor(INDEXABLES_XML_RES_COLUMNS);
        final Set<String> keys = SettingsList.get(getContext()).getSettings();

        for (String key : keys) {
            SettingsInfo info = getSettingsInfo(getContext(), key);
            if (info == null || i.preferenceXml <= 0) {
                continue;
            }

            Object[] ref = new Object[7];
            ref[COLUMN_INDEX_XML_RES_RANK] = 2;
            ref[COLUMN_INDEX_XML_RES_RESID] = i.xmlResId;
            ref[COLUMN_INDEX_XML_RES_CLASS_NAME] = null;
            ref[COLUMN_INDEX_XML_RES_ICON_RESID] = i.iconResId <= 0 ?
                    R.drawable.ic_slim : i.iconResId;
            ref[COLUMN_INDEX_XML_RES_INTENT_ACTION] = null;
            ref[COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE] = "com.slim.settings";
            ref[COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS] = null;
            cursor.addRow(ref);
        }
        return cursor;
    }

    @Override
    public Cursor queryRawData(String[] strings) {
        MatrixCursor cursor = new MatrixCursor(INDEXABLES_RAW_COLUMNS);
        final Set<String> keys = SettingsList.get(getContext()).getSettings();

        // we also submit keywords and metadata for all top-level items
        // which don't have an associated XML resource
        for (String key : keys) {
            PartInfo i = SettingsList.get(getContext()).getSettingInfo(key);
            if (i == null) {
                continue;
            }

            // look for custom keywords
            SearchIndexProvider sip = getSearchIndexProvider(i.getFragmentClass());
            if (sip == null) {
                continue;
            }

            // don't create a duplicate entry if no custom keywords are provided
            // and a resource was already indexed
            List<SearchIndexableRaw> rawList = sip.getRawDataToIndex(getContext());
            if (rawList == null || rawList.size() == 0) {
                if (i.getXmlRes() > 0) {
                    continue;
                }
                rawList = Collections.singletonList(new SearchIndexableRaw(getContext()));
            }

            for (SearchIndexableRaw raw : rawList) {
                Object[] ref = new Object[14];
                ref[COLUMN_INDEX_RAW_RANK] = raw.rank > 0 ?
                        raw.rank : 2;
                ref[COLUMN_INDEX_RAW_TITLE] = raw.title != null ?
                        raw.title : i.getTitle();
                ref[COLUMN_INDEX_RAW_SUMMARY_ON] = i.getSummary();
                ref[COLUMN_INDEX_RAW_KEYWORDS] = raw.keywords;
                ref[COLUMN_INDEX_RAW_ENTRIES] = raw.entries;
                ref[COLUMN_INDEX_RAW_SCREEN_TITLE] = raw.screenTitle != null ?
                        raw.screenTitle : i.getTitle();
                ref[COLUMN_INDEX_RAW_ICON_RESID] = raw.iconResId > 0 ? raw.iconResId :
                        (i.getIconRes() > 0 ? i.getIconRes() : R.drawable.ic_launcher_cyanogenmod);
                ref[COLUMN_INDEX_RAW_INTENT_ACTION] = raw.intentAction != null ?
                        raw.intentAction : i.getAction();
                ref[COLUMN_INDEX_RAW_INTENT_TARGET_PACKAGE] = raw.intentTargetPackage != null ?
                        raw.intentTargetPackage : CMPARTS_ACTIVITY.getPackageName();
                ref[COLUMN_INDEX_RAW_INTENT_TARGET_CLASS] = raw.intentTargetClass != null ?
                        raw.intentTargetClass : CMPARTS_ACTIVITY.getClassName();
                ref[COLUMN_INDEX_RAW_KEY] = raw.key != null ?
                        raw.key : i.getName();
                ref[COLUMN_INDEX_RAW_USER_ID] = -1;
                cursor.addRow(ref);
            }
        }
        return cursor;
    }

    @Override
    public Cursor queryNonIndexableKeys(String[] strings) {
        MatrixCursor cursor = new MatrixCursor(NON_INDEXABLES_KEYS_COLUMNS);

        final Set<String> keys = SettingsList.get(getContext()).getSettings();
        final Set<String> nonIndexables = new ArraySet<>();

        for (String key : keys) {
            PartInfo i = PartsList.get(getContext()).getPartInfo(key);
            if (i == null) {
                continue;
            }

            // look for non-indexable keys
            SearchIndexProvider sip = getSearchIndexProvider(i.getFragmentClass());
            if (sip == null) {
                continue;
            }

            Set<String> nik = sip.getNonIndexableKeys(getContext());
            if (nik == null) {
                continue;
            }

            nonIndexables.addAll(nik);
        }

        for (String nik : nonIndexables) {
            Object[] ref = new Object[1];
            ref[COLUMN_INDEX_NON_INDEXABLE_KEYS_KEY_VALUE] = nik;
            cursor.addRow(ref);
        }
        return cursor;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    private SearchIndexProvider getSearchIndexProvider(final String className) {

        final Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Cannot find class: " + className);
            return null;
        }

        if (clazz == null || !Searchable.class.isAssignableFrom(clazz)) {
            return null;
        }

        try {
            final Field f = clazz.getField(FIELD_NAME_SEARCH_INDEX_DATA_PROVIDER);
            return (SearchIndexProvider) f.get(null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
