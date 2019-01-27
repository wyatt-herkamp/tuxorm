package me.kingtux.mysql;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static me.kingtux.tuxorm.ORMUtils.deleteUselessCrap;
import static me.kingtux.tuxorm.ORMUtils.dropSpaces;

public class MySQLUtils {

    public static Map<Integer, Object> sortForInsertPreparedStatement(String query, Map<String, Object> map) {
        Map<Integer, Object> sortedMap = new LinkedHashMap<>();
        String[] queryParts = StringUtils.substringsBetween(query, "(", ")");
        String[] columnsToInsertInto = queryParts[0].replaceAll("\\s+", "").split(",");
        String[] questionMarks = queryParts[0].replaceAll("\\s+", "").split(",");
        if (columnsToInsertInto.length != questionMarks.length) return null;
        for (int i = 1; i < columnsToInsertInto.length + 1; i++) {
            for (Map.Entry<String, Object> o : map.entrySet()) {
                if (o.getKey().equals(columnsToInsertInto[i])) {
                    sortedMap.put(i, o.getValue());
                }
            }
        }
        return sortedMap;
    }


    public static Map<Integer, Object> sortWHEREPreparedStatement(String query, Map<String, Object> map) {
        Map<Integer, Object> sortedMap = new LinkedHashMap<>();
        String whereRules = StringUtils.substringAfter(query, "WHERE").replace(";", "").replace("AND", "").replace("OR", "").replace("NOT", "").replaceAll("\\s+", ",");
        String[] whereRulesSplit = whereRules.substring(1, whereRules.length() - 1).split(",");
        int arrayPos = 0;
        for (int i = 1; i < whereRulesSplit.length + 1; i++) {
            String[] where = whereRulesSplit[arrayPos].split("=");
            if (where.length != 2) continue;
            for (Map.Entry<String, Object> o : map.entrySet()) {
                if (o.getKey().equals(where[0])) {
                    sortedMap.put(i, o.getValue());
                }
            }
            arrayPos++;
        }
        return sortedMap;
    }

    public static Map<Integer, Object> sortUPDATEPreparedStatement(String query, Map<String, Object> map) {
        Map<Integer, Object> sortedMap = new LinkedHashMap<>();
        String whereRules = deleteUselessCrap(query, ";", "AND", "OR", "NOT");
        whereRules = (StringUtils.substringBetween(whereRules, "SET", "WHERE") + StringUtils.substringAfter(whereRules, "WHERE")).replace(",", " ");
        whereRules = dropSpaces(whereRules, ",");
        String[] whereRulesSplit = whereRules.substring(1, whereRules.length() - 1).split(",");
        int arrayPos = 0;
        for (int i = 1; i < whereRulesSplit.length + 1; i++) {
            String[] where = whereRulesSplit[arrayPos].split("=");
            System.out.println(where[0]+" " +where[1]);
            if (where.length != 2) continue;
            for (Map.Entry<String, Object> o : map.entrySet()) {
                if (o.getKey().equals(where[0])) {
                    sortedMap.put(i, o.getValue());
                }
            }
            arrayPos++;
        }
        return sortedMap;
    }
}
