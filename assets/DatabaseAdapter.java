package com.brandon.apps.groupstudio.assets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 4/19/2015.
 */
public class DatabaseAdapter {

    private DatabaseHandler helper;
    private Context context;
    private SQLiteDatabase writable;
    private SQLiteDatabase readable;
    public DatabaseAdapter(Context c) {
        context = c;
        helper = new DatabaseHandler(c);
    }

    public void open() {
        writable = helper.getWritableDatabase();
        readable = helper.getReadableDatabase();
    }
    public void close() {
        writable.close();
        readable.close();
    }

    public long insertGroup(DefaultGroup group) {
        ContentValues values = new ContentValues();
        values.put(helper.KEY_NAME, group.getName());
        values.put(helper.KEY_DESC, group.getDesc());
        long id = writable.insert(helper.GROUP_TABLE_NAME, null, values);
        helper.createMemberTable(writable, id);
        return id;
    }

    public long insertMember(DefaultGroup parent, DefaultMember member) {
        ContentValues values = new ContentValues();
        values.put(helper.KEY_NAME, member.getName());
        values.put(helper.KEY_TYPE, member.getTypeId());
        long id = writable.insert(helper.MEMBER_TABLE_PREFIX + parent.getId(), null, values);
        helper.createMemberAttributeTable(writable, parent.getId(), id);
        return id;
    }

    public long insertMemberAttribute(int groupId, int memberId, DefaultAttribute attribute) {
        ContentValues values = new ContentValues();
        values.put(helper.KEY_UID, attribute.getId());
        values.put(helper.KEY_TYPE, attribute.getTypeId());
        values.put(helper.KEY_STAT, attribute.getStatId());
        values.put(helper.KEY_TITLE, attribute.getTitle());
        values.put(helper.KEY_VALUE, attribute.getValue());
        long id = writable.insert(helper.ATTRIBUTE_TABLE_PREFIX + groupId + "_" + memberId, null, values);
        return id;
    }

    public long insertTypeAttribute(int typeId, DefaultAttribute attribute) {
        ContentValues values = new ContentValues();
        values.put(helper.KEY_TYPE, attribute.getTypeId());
        values.put(helper.KEY_STAT, attribute.getStatId());
        values.put(helper.KEY_RANK, attribute.getRankId());
        values.put(helper.KEY_TITLE, attribute.getTitle());
        values.put(helper.KEY_VALUE, attribute.getValue());
        long id = writable.insert(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, null, values);
        return id;
    }

    public long insertType(DefaultType type) {
        ContentValues values = new ContentValues();
        values.put(helper.KEY_NAME, type.getName());
        long id = writable.insert(helper.TYPE_TABLE_PREFIX, null, values);
        helper.createTypeAttributeTable(writable, id);
        helper.createCalculationTable(writable, id);
        return id;
    }

    public long insertCalculation(int typeId, Calculation calculation) {
        ContentValues values = new ContentValues();
        values.put(helper.KEY_NAME, calculation.getName());
        values.put(helper.KEY_TARGET, calculation.getTargetId());
        values.put(helper.KEY_STAT, calculation.getStatId());
        long id = writable.insert(helper.CALCULATION_TABLE_PREFIX + typeId, null, values);
        return id;
    }

    public long insertServer() {
        ContentValues values = new ContentValues();
        values.put(helper.KEY_ID, 0);
        values.put(helper.KEY_IP, "");
        values.put(helper.KEY_PASSWORD, "");
        long id = writable.insert(helper.NET_TABLE_NAME, null, values);
        return id;
    }

    public DefaultGroup selectGroupById(int id) {
        String[] columns = { helper.KEY_NAME, helper.KEY_DESC };
        String condition = helper.KEY_ID + " = " + id;
        Cursor cursor = readable.query(helper.GROUP_TABLE_NAME, columns, condition, null, null, null, null);
        DefaultGroup selectedGroup;
        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex(helper.KEY_NAME));
        String desc = cursor.getString(cursor.getColumnIndex(helper.KEY_DESC));
        selectedGroup = new DefaultGroup(id, name, desc);
        cursor.close();
        return selectedGroup;
    }

    public DefaultMember selectMemberById(int groupId, int id) {
        String[] columns = { helper.KEY_NAME, helper.KEY_TYPE };
        String condition = helper.KEY_ID + " = " + id;
        Cursor cursor = readable.query(helper.MEMBER_TABLE_PREFIX + groupId, columns, condition, null, null, null, null);
        DefaultMember selectedMember;
        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex(helper.KEY_NAME));
        int type = cursor.getInt(cursor.getColumnIndex(helper.KEY_TYPE));
        selectedMember = new DefaultMember(id, name, type, groupId);
        cursor.close();
        return selectedMember;
    }

    public int selectMemberIdByName(int groupId, String name) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_NAME + " = '" + name + "'";
        Cursor cursor = readable.query(helper.MEMBER_TABLE_PREFIX + groupId, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex(helper.KEY_ID));
        System.out.println(cursor.getCount() + ", " + id);
        cursor.close();
        return id;
    }

    public int selectTypeIdByName(String name) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_NAME + " = '" + name + "'";
        Cursor cursor = readable.query(helper.TYPE_TABLE_PREFIX, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex(helper.KEY_ID));
        System.out.println(cursor.getCount() + ", " + id);
        cursor.close();
        return id;
    }

    public String selectAttributeValueById(int groupId, int memberId, int id) {
        //Toast.makeText(context, groupId + ", " + memberId + ", " + id, Toast.LENGTH_SHORT).show();
        String[] columns = { helper.KEY_VALUE };
        String condition = helper.KEY_ID + " = " + id;
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + groupId + "_" + memberId, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        String value = cursor.getString(cursor.getColumnIndex(helper.KEY_VALUE));
        cursor.close();
        return value;
    }

    public String selectAttributeValueByUId(int groupId, int memberId, int uId) {
        //Toast.makeText(context, groupId + ", " + memberId + ", " + id, Toast.LENGTH_SHORT).show();
        String[] columns = { helper.KEY_VALUE };
        String condition = helper.KEY_UID + " = " + uId;
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + groupId + "_" + memberId, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        String value = cursor.getString(cursor.getColumnIndex(helper.KEY_VALUE));
        cursor.close();
        return value;
    }

    public int selectMemberAttributeIdByTitle(int groupId, int memberId, String title) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_TITLE + " = '" + title + "'";
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + groupId + "_" + memberId, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex(helper.KEY_ID));
        System.out.println(cursor.getCount() + ", " + id);
        cursor.close();
        return id;
    }

    public DefaultAttribute selectTypeAttributeById(int typeId, int id) {
        String[] columns = { helper.KEY_TYPE, helper.KEY_STAT, helper.KEY_RANK, helper.KEY_TITLE, helper.KEY_VALUE };
        String condition = helper.KEY_ID + " = " + id;
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        DefaultAttribute selectedAttribute = new DefaultAttribute(
                id,
                typeId,
                cursor.getInt(cursor.getColumnIndex(helper.KEY_RANK)),
                cursor.getString(cursor.getColumnIndex(helper.KEY_TITLE)),
                cursor.getString(cursor.getColumnIndex(helper.KEY_VALUE))
        );
        selectedAttribute.setStatId(cursor.getInt(cursor.getColumnIndex(helper.KEY_STAT)));
        cursor.close();
        return selectedAttribute;
    }

    public int selectTypeAttributeIdByTitle(int typeId, String title) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_TITLE + " = '" + title + "'";
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex(helper.KEY_ID));
        System.out.println(cursor.getCount() + ", " + id);
        cursor.close();
        return id;
    }

    public int getTypeAttributePositionByTarget(int typeId, int id) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_STAT + " = 1";
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getInt(cursor.getColumnIndex(helper.KEY_ID)) == id) {
                cursor.close();
                return cursor.getPosition();
            }
            cursor.moveToNext();
        }
        return -1;
    }

    public int getTypePositionById(int id) {
        String[] columns = { helper.KEY_ID };
        Cursor cursor = readable.query(helper.TYPE_TABLE_PREFIX, columns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getInt(cursor.getColumnIndex(helper.KEY_ID)) == id) {
                cursor.close();
                return cursor.getPosition();
            }
            cursor.moveToNext();
        }
        return -1;
    }

    public DefaultType selectTypeById(int id) {
        String[] columns = { helper.KEY_NAME };
        String condition = helper.KEY_ID + " = " + id;
        Cursor cursor = readable.query(helper.TYPE_TABLE_PREFIX, columns, condition, null, null, null, null);
        DefaultType selectedType;
        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex(helper.KEY_NAME));
        selectedType = new DefaultType(id, name);
        return selectedType;
    }

    public Calculation selectCalculationById(int typeId, int id) {
        String[] columns = { helper.KEY_NAME, helper.KEY_TARGET, helper.KEY_STAT };
        String condition = helper.KEY_ID + " = " + id;
        Cursor cursor = readable.query(helper.CALCULATION_TABLE_PREFIX + typeId, columns, condition, null, null, null, null);
        Calculation selectedCalculation;
        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex(helper.KEY_NAME));
        int target = cursor.getInt(cursor.getColumnIndex(helper.KEY_TARGET));
        int stat = cursor.getInt(cursor.getColumnIndex(helper.KEY_STAT));
        selectedCalculation = new Calculation(id, target, stat, name);
        cursor.close();
        return selectedCalculation;
    }

    public int selectCalculationIdByName(int typeId, String name) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_NAME + " = '" + name + "'";
        Cursor cursor = readable.query(helper.CALCULATION_TABLE_PREFIX + typeId, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex(helper.KEY_ID));
        System.out.println(cursor.getCount() + ", " + id);
        cursor.close();
        return id;
    }

    public String selectServerIp() {
        String[] columns = { helper.KEY_IP };
        String condition = helper.KEY_ID + " = 0";
        Cursor cursor = readable.query(helper.NET_TABLE_NAME, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        String ip = cursor.getString(cursor.getColumnIndex(helper.KEY_IP));
        cursor.close();
        return ip;
    }

    public String selectServerPassword() {
        String[] columns = { helper.KEY_PASSWORD };
        String condition = helper.KEY_ID + " = 0";
        Cursor cursor = readable.query(helper.NET_TABLE_NAME, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        String password = cursor.getString(cursor.getColumnIndex(helper.KEY_PASSWORD));
        cursor.close();
        return password;
    }

    public int selectUserType() {
        String[] columns = { helper.KEY_TYPE };
        String condition = helper.KEY_ID + " = 0";
        Cursor cursor = readable.query(helper.SETTINGS_TABLE_NAME, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        int type = cursor.getInt(cursor.getColumnIndex(helper.KEY_TYPE));
        cursor.close();
        return type;
    }

    public int selectUserTheme() {
        String[] columns = { helper.KEY_STYLE };
        String condition = helper.KEY_ID + " = 0";
        Cursor cursor = readable.query(helper.SETTINGS_TABLE_NAME, columns, condition, null, null, null, null);
        cursor.moveToFirst();
        int type = cursor.getInt(cursor.getColumnIndex(helper.KEY_STYLE));
        cursor.close();
        return type;
    }


    public long updateGroupById(int _id, String newName, String newDesc) {
        String condition = helper.KEY_ID + " = " + _id;
        ContentValues values = new ContentValues();
        if (newName != null) values.put(helper.KEY_NAME, newName);
        if (newDesc != null) values.put(helper.KEY_DESC, newDesc);
        long id = writable.update(helper.GROUP_TABLE_NAME, values, condition, null);
        return id;
    }

    public long updateMemberById(int parentId, int _id, String newName, int newType) {
        String condition = helper.KEY_ID + " = " + _id;
        ContentValues values = new ContentValues();
        values.put(helper.KEY_NAME, newName);
        values.put(helper.KEY_TYPE, newType);
        long id = writable.update(helper.MEMBER_TABLE_PREFIX + parentId, values, condition, null);
        return id;
    }

    public long updateMemberAttributeById(int groupId, int memberId, int _id, int newUniversalId, int newStatId, String newTitle, String newValue) {
        String condition = helper.KEY_ID + " = " + _id;
        ContentValues values = new ContentValues();
        values.put(helper.KEY_UID, newUniversalId);
        values.put(helper.KEY_STAT, newStatId);
        values.put(helper.KEY_TITLE, newTitle);
        values.put(helper.KEY_VALUE, newValue);
        long id = writable.update(helper.ATTRIBUTE_TABLE_PREFIX + groupId + "_" + memberId, values, condition, null);
        return id;
    }

    public long updateMemberAttributeByUId(int groupId, int memberId, int _id, int newStatId, String newTitle, String newValue) {
        String condition = helper.KEY_UID + " = " + _id;
        ContentValues values = new ContentValues();
        values.put(helper.KEY_UID, _id);
        values.put(helper.KEY_STAT, newStatId);
        values.put(helper.KEY_TITLE, newTitle);
        values.put(helper.KEY_VALUE, newValue);
        long id = writable.update(helper.ATTRIBUTE_TABLE_PREFIX + groupId + "_" + memberId, values, condition, null);
        return id;
    }

    public long updateTypeAttributeById(int typeId, int _id, int newStatId, String newTitle, String newValue) {
        String condition = helper.KEY_ID + " = " + _id;
        ContentValues values = new ContentValues();
        values.put(helper.KEY_STAT, newStatId);
        values.put(helper.KEY_TITLE, newTitle);
        values.put(helper.KEY_VALUE, newValue);
        long id = writable.update(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, values, condition, null);
        return id;
    }

    public long updateTypeAttributeById(int typeId, int _id, int newRankId) {
        String condition = helper.KEY_ID + " = " + _id;
        ContentValues values = new ContentValues();
        values.put(helper.KEY_RANK, newRankId);
        long id = writable.update(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, values, condition, null);
        return id;
    }

    public long updateTypeAttributeById(int typeId, int _id, int newRankId, int newStatId, String newTitle, String newValue) {
        String condition = helper.KEY_ID + " = " + _id;
        ContentValues values = new ContentValues();
        values.put(helper.KEY_RANK, newRankId);
        values.put(helper.KEY_STAT, newStatId);
        values.put(helper.KEY_TITLE, newTitle);
        values.put(helper.KEY_VALUE, newValue);
        System.out.println(typeId);
        long id = writable.update(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, values, condition, null);
        return id;
    }

    public long updateTypeById(int _id, String newName) {
        String condition = helper.KEY_ID + " = " + _id;
        ContentValues values = new ContentValues();
        values.put(helper.KEY_NAME, newName);
        long id = writable.update(helper.TYPE_TABLE_PREFIX, values, condition, null);
        return id;
    }

    public long updateCalculationById(int typeId, int _id, int newTarget, int newStat, String newName) {
        String condition = helper.KEY_ID + " = " + _id;
        ContentValues values = new ContentValues();
        values.put(helper.KEY_NAME, newName);
        values.put(helper.KEY_TARGET, newTarget);
        values.put(helper.KEY_STAT, newStat);
        long id = writable.update(helper.CALCULATION_TABLE_PREFIX + typeId, values, condition, null);
        return id;
    }

    public long updateServer(String ip, String password) {
        String condition = helper.KEY_ID + " = 0";
        ContentValues values = new ContentValues();
        values.put(helper.KEY_IP, ip);
        values.put(helper.KEY_PASSWORD, password);
        long id = writable.update(helper.NET_TABLE_NAME, values, condition, null);
        return id;
    }

    public long updateUser(int type, int theme) {
        String condition = helper.KEY_ID + " = 0";
        ContentValues values = new ContentValues();
        values.put(helper.KEY_TYPE, type);
        values.put(helper.KEY_STYLE, theme);
        long id = writable.update(helper.SETTINGS_TABLE_NAME, values, condition, null);
        return id;
    }

    public void deleteGroupById(int id) {
        String condition = helper.KEY_ID + " = " + id;
        writable.delete(helper.GROUP_TABLE_NAME, condition, null);
        List<DefaultMember> list = getAllMembers(id);
        for (int i = 0; i < list.size(); i++) {
            writable.execSQL("DROP TABLE IF EXISTS " + helper.ATTRIBUTE_TABLE_PREFIX + id + "_" + list.get(i).getId());
        }
        helper.deleteTable(writable, id, helper.MEMBER_TABLE_PREFIX);
    }

    public void deleteMemberById(int parentId, int id) {
        String condition = helper.KEY_ID + " = " + id;
        writable.delete(helper.MEMBER_TABLE_PREFIX + parentId, condition, null);
        writable.execSQL("DROP TABLE IF EXISTS " + helper.ATTRIBUTE_TABLE_PREFIX + parentId + "_" + id);
    }

    public void deleteMemberAttributeById(int groupId, int memberId, int id) {
        String condition = helper.KEY_ID + " = " + id;
        writable.delete(helper.ATTRIBUTE_TABLE_PREFIX + groupId + "_" + memberId, condition, null);
    }

    public void deleteTypeAttributeById(int typeId, int id) {
        String condition1 = helper.KEY_ID + " = " + id;
        String condition2 = helper.KEY_TARGET + " = " + selectTypeAttributeById(typeId, id).getId(); // this statement does not function properly
        writable.delete(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, condition1, null);
        writable.delete(helper.CALCULATION_TABLE_PREFIX + typeId, condition2, null);
    }

    public void deleteTypeById(int id) {
        List<DefaultGroup> groups = getAllGroups();
        for (int i = 0; i < groups.size(); i++) {
            DefaultGroup group = groups.get(i);
            List<DefaultMember> members = getAllMembers(group.getId());
            for (int j = 0; j < members.size(); j++) {
                DefaultMember member = members.get(j);
                if (member.getTypeId() == id) {
                    updateMemberById(group.getId(), member.getId(), member.getName(), 0);
                }
            }
        }

        String condition = helper.KEY_ID + " = " + id;
        writable.delete(helper.TYPE_TABLE_PREFIX, condition, null);
        writable.execSQL("DROP TABLE IF EXISTS " + helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + id);
        writable.execSQL("DROP TABLE IF EXISTS " + helper.CALCULATION_TABLE_PREFIX + id);
    }

    public void deleteCalculationById(int typeId, int id) {
        String condition = helper.KEY_ID + " = " + id;
        writable.delete(helper.CALCULATION_TABLE_PREFIX + typeId, condition, null);
    }

    public int getMemberLength(int _parentId) {
        String[] columns = { helper.KEY_ID };
        Cursor cursor = readable.query(helper.MEMBER_TABLE_PREFIX + _parentId, columns, null, null, null, null, null);
        int length = cursor.getCount();
        cursor.close();
        return length;
    }

    public int getMemberAttributeLength(int groupId, int memberId) {
        String[] columns = { helper.KEY_ID };
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + groupId + "_" + memberId, columns, null, null, null, null, null);
        int length = cursor.getCount();
        cursor.close();
        return length;
    }

    public int getTypeAttributeLength(int typeId) {
        if (typeId == 0) return 0;
        String[] columns = { helper.KEY_ID };
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, columns, null, null, null, null, null);
        int length = cursor.getCount();
        cursor.close();
        return length;
    }

    public List<DefaultGroup> getAllGroups() {
        String[] columns = { helper.KEY_ID, helper.KEY_NAME, helper.KEY_DESC };
        Cursor cursor = readable.query(helper.GROUP_TABLE_NAME, columns, null, null, null, null, null);
        List<DefaultGroup> list = new ArrayList<DefaultGroup>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(cursor.getColumnIndex(helper.KEY_ID));
            String name = cursor.getString(cursor.getColumnIndex(helper.KEY_NAME));
            String desc = cursor.getString(cursor.getColumnIndex(helper.KEY_DESC));
            DefaultGroup currentGroup = new DefaultGroup(id, name, desc);
            list.add(currentGroup);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public List<DefaultMember> getAllMembers(int groupId) {
        Cursor cursor = readable.query(helper.MEMBER_TABLE_PREFIX + groupId, null, null, null, null, null, null);
        List<DefaultMember> list = new ArrayList<DefaultMember>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DefaultMember currentMember = new DefaultMember(
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_ID)),
                    cursor.getString(cursor.getColumnIndex(helper.KEY_NAME)),
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_TYPE)),
                    groupId);
            list.add(currentMember);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public List<DefaultAttribute> getAllMemberAttributes(int groupId, int memberId) {
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + groupId + "_" + memberId, null, null, null, null, null, null);
        List<DefaultAttribute> list = new ArrayList<DefaultAttribute>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DefaultAttribute currentAttribute = new DefaultAttribute(
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_ID)),
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_TYPE)),
                    cursor.getString(cursor.getColumnIndex(helper.KEY_TITLE)),
                    cursor.getString(cursor.getColumnIndex(helper.KEY_VALUE)));
            currentAttribute.setStatId(cursor.getInt(cursor.getColumnIndex(helper.KEY_STAT)));
            currentAttribute.setUniversalId(cursor.getInt(cursor.getColumnIndex(helper.KEY_UID)));
            list.add(currentAttribute);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public List<DefaultAttribute> getAllTypeAttributes(int typeId) {
        List<DefaultAttribute> list = new ArrayList<DefaultAttribute>();
        if(typeId == 0) return list;
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DefaultAttribute currentAttribute = new DefaultAttribute(
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_ID)),
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_TYPE)),
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_RANK)),
                    cursor.getString(cursor.getColumnIndex(helper.KEY_TITLE)),
                    cursor.getString(cursor.getColumnIndex(helper.KEY_VALUE)));
            currentAttribute.setStatId(cursor.getInt(cursor.getColumnIndex(helper.KEY_STAT)));
            list.add(currentAttribute);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public List<DefaultAttribute> getAllNumericTypeAttributes(int typeId) {
        List<DefaultAttribute> list = new ArrayList<DefaultAttribute>();
        if(typeId == 0) return list;
        String query = "SELECT * FROM " + helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId +
                " WHERE " + helper.KEY_STAT + " = 1;";
        Cursor cursor = readable.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DefaultAttribute currentAttribute = new DefaultAttribute(
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_ID)),
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_TYPE)),
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_RANK)),
                    cursor.getString(cursor.getColumnIndex(helper.KEY_TITLE)),
                    cursor.getString(cursor.getColumnIndex(helper.KEY_VALUE)));
            currentAttribute.setStatId(cursor.getInt(cursor.getColumnIndex(helper.KEY_STAT)));
            list.add(currentAttribute);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    // problem function

    public List<DefaultAttribute> getAllTargetTypeAttributes(int typeId) {
        List<DefaultAttribute> list = new ArrayList<DefaultAttribute>();
        if(typeId == 0) return list;
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getInt(cursor.getColumnIndex(helper.KEY_RANK)) != 0 && cursor.getInt(cursor.getColumnIndex(helper.KEY_STAT)) == 1) {
                DefaultAttribute currentAttribute = new DefaultAttribute(
                        cursor.getInt(cursor.getColumnIndex(helper.KEY_ID)),
                        cursor.getInt(cursor.getColumnIndex(helper.KEY_TYPE)),
                        cursor.getInt(cursor.getColumnIndex(helper.KEY_RANK)),
                        cursor.getString(cursor.getColumnIndex(helper.KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(helper.KEY_VALUE)));
                currentAttribute.setStatId(cursor.getInt(cursor.getColumnIndex(helper.KEY_STAT)));
                list.add(currentAttribute);
            }
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public List<DefaultType> getAllTypes() {
        String[] columns = { helper.KEY_ID, helper.KEY_NAME };
        Cursor cursor = readable.query(helper.TYPE_TABLE_PREFIX, columns, null, null, null, null, null);
        List<DefaultType> list = new ArrayList<DefaultType>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DefaultType currentType = new DefaultType(
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_ID)),
                    cursor.getString(cursor.getColumnIndex(helper.KEY_NAME)));
            list.add(currentType);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public List<Calculation> getAllCalculations(int typeId) {
        String[] columns = { helper.KEY_ID, helper.KEY_NAME, helper.KEY_TARGET, helper.KEY_STAT };
        Cursor cursor = readable.query(helper.CALCULATION_TABLE_PREFIX + typeId, columns, null, null, null, null, null);
        List<Calculation> list = new ArrayList<Calculation>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Calculation currentCalculation = new Calculation(
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_ID)),
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_TARGET)),
                    cursor.getInt(cursor.getColumnIndex(helper.KEY_STAT)),
                    cursor.getString(cursor.getColumnIndex(helper.KEY_NAME)));
            list.add(currentCalculation);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public boolean groupExists(String name) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_NAME + " = '" + name + "'";
        Cursor cursor = readable.query(helper.GROUP_TABLE_NAME, columns, condition, null, null, null, null);
        return (cursor.getCount() != 0);
    }

    public boolean typeExists(String name) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_NAME + " = '" + name + "'";
        Cursor cursor = readable.query(helper.TYPE_TABLE_PREFIX, columns, condition, null, null, null, null);
        return (cursor.getCount() != 0);
    }

    public boolean calculationExists(int typeId, String name) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_NAME + " = '" + name + "'";
        Cursor cursor = readable.query(helper.CALCULATION_TABLE_PREFIX + typeId, columns, condition, null, null, null, null);
        return (cursor.getCount() != 0);
    }

    public boolean memberExists(int groupId, String name) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_NAME + " = '" + name + "'";
        Cursor cursor = readable.query(helper.MEMBER_TABLE_PREFIX + groupId, columns, condition, null, null, null, null);
        return (cursor.getCount() != 0);
    }

    public boolean memberAttributeExists(int groupId, int memberId, String title) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_TITLE + " = '" + title + "'";
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + groupId + "_" + memberId, columns, condition, null, null, null, null);
        return (cursor.getCount() != 0);
    }

    public boolean typeAttributeExists(int typeId, String title) {
        String[] columns = { helper.KEY_ID };
        String condition = helper.KEY_TITLE + " = '" + title + "'";
        Cursor cursor = readable.query(helper.ATTRIBUTE_TABLE_PREFIX + "TYPE_" + typeId, columns, condition, null, null, null, null);
        return (cursor.getCount() != 0);
    }


    private static class DatabaseHandler extends SQLiteOpenHelper {
        private Context context;
        private static final int DATABASE_VERSION = 10;
        private static final String DATABASE_NAME = "GROUPSTUDIO_DATABASE";
        private static final String MEMBER_TABLE_PREFIX = "MEMBER_TABLE_";
        private static final String ATTRIBUTE_TABLE_PREFIX = "ATTRIBUTE_TABLE_";
        private static final String TYPE_TABLE_PREFIX = "TYPE_TABLE";
        private static final String CALCULATION_TABLE_PREFIX = "CALCULATION_TABLE_";
        private static final String GROUP_TABLE_NAME = "GROUP_TABLE";
        private static final String NET_TABLE_NAME = "NET_TABLE";
        private static final String SETTINGS_TABLE_NAME = "SETTINGS_TABLE";
        private static final String KEY_ID = "_id";
        private static final String KEY_UID = "_universalId";
        private static final String KEY_NAME = "name";
        private static final String KEY_DESC = "desc";
        private static final String KEY_TITLE = "title";
        private static final String KEY_TYPE = "type";
        private static final String KEY_STAT = "stat";
        private static final String KEY_RANK = "rank";
        private static final String KEY_TARGET = "target";
        private static final String KEY_VALUE = "value";
        private static final String KEY_STYLE = "style";
        private static final String KEY_IP = "ip";
        private static final String KEY_PASSWORD = "password";
        private static final String CREATE_TABLE = "CREATE TABLE " +
                        GROUP_TABLE_NAME +
                        " (" +
                        KEY_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_NAME +
                        " VARCHAR(255), " +
                        KEY_DESC +
                        " VARCHAR(255));";
        private static final String DROP_TABLE = "DROP TABLE IF EXISTS " +
                        GROUP_TABLE_NAME;

        public DatabaseHandler(Context c) {
            super(c, DATABASE_NAME, null, DATABASE_VERSION);
            context = c;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE);
                createTypeTable(db);
                createNetTable(db);
                createSettingsTable(db);

                ContentValues values = new ContentValues();
                values.put(KEY_ID, 0);
                values.put(KEY_IP, "");
                values.put(KEY_PASSWORD, "");
                db.insert(NET_TABLE_NAME, null, values);

                values.clear();
                values.put(KEY_ID, 0);
                values.put(KEY_TYPE, 0);    // 0 is basic, 1 is expert
                values.put(KEY_STYLE, 0);   // 0 is dark theme, 1 is light, 2 is skye theme
                db.insert(SETTINGS_TABLE_NAME, null, values);
            } catch(SQLiteException e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL(DROP_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + SETTINGS_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + NET_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + TYPE_TABLE_PREFIX);
                onCreate(db);
            } catch(SQLiteException e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        }

        public void createMemberTable(SQLiteDatabase db, long id) {
            try {
                db.execSQL("CREATE TABLE " +
                        MEMBER_TABLE_PREFIX +
                        id +
                        " (" +
                        KEY_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_NAME +
                        " VARCHAR(255), " +
                        KEY_TYPE +
                        " INTEGER);");
            } catch(SQLiteException e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        }

        public void createMemberAttributeTable(SQLiteDatabase db, long groupId, long memberId) {
            try {
                db.execSQL("CREATE TABLE " +
                        ATTRIBUTE_TABLE_PREFIX +
                        groupId + "_" + memberId +
                        " (" +
                        KEY_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_UID +
                        " INTEGER, " +
                        KEY_TYPE +
                        " INTEGER, " +
                        KEY_STAT +
                        " INTEGER, " +
                        KEY_TITLE +
                        " VARCHAR(255), " +
                        KEY_VALUE +
                        " VARCHAR(255));");
            } catch(SQLiteException e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        }

        public void createTypeAttributeTable(SQLiteDatabase db, long typeId) {
            try {
                db.execSQL("CREATE TABLE " +
                        ATTRIBUTE_TABLE_PREFIX +
                        "TYPE_" + typeId +
                        " (" +
                        KEY_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_TYPE +
                        " INTEGER, " +
                        KEY_STAT +
                        " INTEGER, " +
                        KEY_RANK +
                        " INTEGER, " +
                        KEY_TITLE +
                        " VARCHAR(255), " +
                        KEY_VALUE +
                        " VARCHAR(255));");
            } catch(SQLiteException e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        }

        public void createTypeTable(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE " +
                        TYPE_TABLE_PREFIX +
                        " (" +
                        KEY_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_NAME +
                        " VARCHAR(255));");
            } catch(SQLiteException e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        }

        public void createCalculationTable(SQLiteDatabase db, long typeId) {
            try {
                db.execSQL("CREATE TABLE " +
                        CALCULATION_TABLE_PREFIX + typeId +
                        " (" +
                        KEY_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_NAME +
                        " VARCHAR(255), " +
                        KEY_TARGET +
                        " INTEGER, " +
                        KEY_STAT +
                        " INTEGER);");
            } catch(SQLiteException e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        }

        public void createNetTable(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE " +
                        NET_TABLE_NAME +
                        " (" +
                        KEY_ID +
                        " INTEGER, " +
                        KEY_IP +
                        " VARCHAR(255), " +
                        KEY_PASSWORD +
                        " VARCHAR(255));");
            } catch(SQLiteException e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        }

        public void createSettingsTable(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE " +
                        SETTINGS_TABLE_NAME +
                        " (" +
                        KEY_ID +
                        " INTEGER, " +
                        KEY_TYPE +
                        " INTEGER, " +
                        KEY_STYLE +
                        " INTEGER);");
            } catch(SQLiteException e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        }

        public void deleteTable(SQLiteDatabase db, int id, String table) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + table + id);
            } catch(SQLiteException e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
