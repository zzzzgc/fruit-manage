package genmodel;

import com.fruit.manage.model.CheckInventoryDetail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @Author: ZGC
 * @Date Created in 17:10 2018/6/21
 */
public class putClassSetingGeting {
    public static void main(String[] args) throws ClassNotFoundException {
        Class zlass = CheckInventoryDetail.class;
        getSetingStr(zlass);
    }

    private static void getSetingStr(Class zlass) {
        String zlassName = zlass.getSimpleName().substring(0, 1).toLowerCase() + zlass.getSimpleName().substring(1);
        System.out.println(zlass.getSimpleName() + " " + zlassName + " = new " + zlass.getSimpleName() + "();");
        Arrays.stream(zlass.getSuperclass().getMethods()).filter(a -> a.toString().indexOf(zlass.getSimpleName()) != -1).map(Method::getName).filter(a -> a.toString().indexOf("set") != -1).forEach(a ->
                System.out.println(zlassName + "." + a + "(" + a.substring(3).substring(0, 1).toLowerCase() + a.substring(3).substring(1) + ");"));

        HashMap<String, String> map = new HashMap<>(100);
        Arrays.stream(zlass.getSuperclass().getMethods())
                .filter(a -> a.toString().indexOf(zlass.getSimpleName()) != -1)
                .filter(a -> a.toString().indexOf("set") != -1)
                .forEach(a -> map.put(a.getName().substring(3).substring(0, 1).toLowerCase() + a.getName().substring(3).substring(1), a.getParameterTypes()[0].getSimpleName()));
        map.forEach(
                (x, y) ->
                        System.out.print(y + " " + x + ",")
        );
    }
}
