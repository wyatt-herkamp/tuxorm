package me.kingtux.tuxorm;

import me.kingtux.tuxorm.annotations.*;
import me.kingtux.tuxorm.builders.TableBuilder;
import me.kingtux.tuxorm.datatypes.CommonDataTypes;
import me.kingtux.tuxorm.dbo2.DBField;
import me.kingtux.tuxorm.dbo2.items.CollectionField;
import me.kingtux.tuxorm.dbo2.items.CollectionOField;
import me.kingtux.tuxorm.dbo2.items.ObjectColumnField;
import me.kingtux.tuxorm.exceptions.NotTableClassException;
import me.kingtux.tuxorm.exceptions.WeAreFuckedException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The utilities for TuxORM
 */
public class ORMUtils {

    private ORMUtils() {
    }

    public static void createTableIfNotExists(ORMConnection oc, Class clazz) {
        if (tableExists(oc, clazz)) {
            return;
        }
        createTable(oc, clazz);
    }

    public static boolean tableExists(ORMConnection oc, Class clazz) {
        return tableExists(oc, NameGen.getTableName(clazz));
    }

    public static boolean tableExists(ORMConnection oc, String name) {
        return oc.tableExists(name);
    }

    public static void createTable(ORMConnection oc, Class<?> clazz) {
        //Does it follow the structure
        if (clazz.getAnnotation(DatabaseTable.class) == null)
            throw new NotTableClassException(clazz.getSimpleName() + " Does not have a @DatabaseTable annotation");
        //Get all the DB compatible fields
        List<DBField> fields = getDBFields(clazz);

        //Do some sexy stuff with the field
        for (DBField field : fields) {
            if (field instanceof CollectionOField) {
                try {
                    createCollectionTable(((CollectionOField) field).getField(), oc);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else if (field instanceof ObjectColumnField) {
                try {
                    createTableIfNotExists(oc, ((ObjectColumnField) field).getClazz());
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }


        createTable(fields, NameGen.getTableName(clazz), oc);
    }

    public static void createCollectionTable(Field field, ORMConnection oc) {
        Class<?> ths = field.getDeclaringClass();

        if (tableExists(oc, NameGen.getCollectionFieldTableName(field, ths))) {
            return;
        }
        List<DBField> fields = new LinkedList<>();

        //Format ID, this.id, co

        //Get the first type param
        Class<?> firstType = getFirstTypeParam(field);
        if (firstType == null) throw new WeAreFuckedException();

        //Add Fields
        fields.add(new CollectionField("id", true));
        fields.add(new CollectionField(NameGen.getTableName(ths)));
        //Check to see if it a "simpletype" if not get/create table for it
        if (field.getAnnotation(ORMCollection.class).isBasic())
            fields.add(new CollectionField(firstType.getSimpleName().toLowerCase(), CommonDataTypes.getByType(firstType)));
        else {
            createTableIfNotExists(oc, firstType);
            fields.add(new CollectionField(NameGen.getIdName(firstType)));
        }
        //Create Table
        createTable(fields, NameGen.getCollectionFieldTableName(field, ths), oc);
    }

    public static Class<?> getFirstTypeParam(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) field.getGenericType();
            try {
                return Class.forName(ptype.getActualTypeArguments()[0].toString().replace("class ", ""));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private static void createTable(List<DBField> fields, String name, ORMConnection oc) {
        TableBuilder tb = oc.createTableBuilder();
        tb.withName(name);
        for (DBField of : fields) {
            tb.addColumn(of);
        }
        //System.out.println(tb.buildQuery());
        oc.executeSimpleQuery(tb.buildQuery());

    }

    private static boolean containsAnnotations(Field field) {
        return contains(field.getDeclaredAnnotations(), DatabaseField.class, ForeignField.class, Id.class, ORMConnection.class);
    }

    private static boolean contains(Annotation[] declaredAnnotations, Class<?>... annotations) {
        for (Annotation annotation : declaredAnnotations) {
            for (Class<?> c : annotations) {
                if (annotation.getClass() == c) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isDBCompatible(Field field) {
        return isDBCompatible(field.getType());
    }

    public static boolean isDBCompatible(Class<?> type) {
        if (type.isPrimitive() || type == String.class) return true;
        return false;
    }


    public static String dropSpaces(String s, String rp) {
        return s.replaceAll("\\s+", rp);
    }

    public static String deleteUselessCrap(String s, String... noNos) {
        String sd = s;
        for (String st : noNos) {
            sd = sd.replace(st, "");
        }
        return sd;
    }


    public static ResultSet query(ORMConnection oc, String query, Map<Integer, Object> queryItem) throws SQLException {
        PreparedStatement preparedStatement = oc.getConnection().prepareStatement(query);
        for (Map.Entry<Integer, Object> o : queryItem.entrySet()) {
            preparedStatement.setObject(o.getKey(), o.getValue());
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        preparedStatement.close();
        return resultSet;
    }


    public static List<DBField> getDBFields(Class<?> clazz) {
        List<DBField> fields = new LinkedList<>();
        for (Field field : clazz.getDeclaredFields()) {
            //Checks
            if (!containsAnnotations(field)) continue;
            if (!isORMCompatible(field)) continue;
            //Add to fields
            if (field.getAnnotation(ORMCollection.class) != null) {
                fields.add(new CollectionOField(field));
            } else {
                fields.add(new ObjectColumnField(field));
            }
        }
        return fields;
    }

    private static boolean isORMCompatible(Field field) {
        //IDK we will build this later
        return true;
    }

    public static Class<?> getPrimaryKey(Class<?> otherObject) {
        for (Field field : otherObject.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getType();
            }
        }
        return null;
    }

    public static Map<String, Object> getTheValues(Object object) {
        return null;
    }

    public static Object getPrimaryKeyValue(Object object) {
        return null;
    }
}
