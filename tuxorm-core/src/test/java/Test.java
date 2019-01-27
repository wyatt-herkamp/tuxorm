import org.apache.commons.lang3.StringUtils;

public class Test {
    public static void main(String[] args) {
        String[] queryParts = StringUtils.substringsBetween("INSERT INTO tableName (name, email, password) VALUES (?,?,?)", "(", ")");
        for (String s : queryParts) {
            System.out.println(s);
        }
    }
}
